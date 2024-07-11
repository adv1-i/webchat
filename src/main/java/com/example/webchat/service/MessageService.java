package com.example.webchat.service;

import com.example.webchat.enums.MessageType;
import com.example.webchat.exception.MaxFileSizeExceededException;
import com.example.webchat.exception.MaxFilesExceededException;
import com.example.webchat.repository.MessageRepository;
import com.example.webchat.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MessageService {

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB
    private static final int MAX_FILES = 10;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public Message sendMessage(Message message) {
        LocalDateTime now = LocalDateTime.now();
        Date timestamp = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        message.setTimestamp(timestamp);
        return messageRepository.save(message);
    }

    public Message sendMessageWithFiles(String content, String sender, String roomId, String recipients, List<MultipartFile> files)
            throws MaxFileSizeExceededException, MaxFilesExceededException {
        if (files != null) {
            if (files.size() > MAX_FILES) {
                throw new MaxFilesExceededException("Maximum number of files (10) exceeded");
            }

            for (MultipartFile file : files) {
                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new MaxFileSizeExceededException("File size exceeds the limit of 15 MB");
                }
            }
        }

        Message message = new Message();
        message.setContent(content != null ? content : "");
        message.setSender(sender);
        message.setRoomId(roomId);
        try {
            message.setRecipients(objectMapper.readValue(recipients, List.class));
        } catch (IOException e) {
            throw new RuntimeException("Error parsing recipients", e);
        }

        LocalDateTime now = LocalDateTime.now();
        Date timestamp = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        message.setTimestamp(timestamp);

        if (files != null && !files.isEmpty()) {
            List<String> fileIds = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();
            for (MultipartFile file : files) {
                try {
                    String fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType()).toString();
                    fileIds.add(fileId);
                    fileNames.add(file.getOriginalFilename());
                } catch (IOException e) {
                    throw new RuntimeException("Error storing file", e);
                }
            }
            message.setFileIds(fileIds);
            message.setFileNames(fileNames);
            message.setMessageType(MessageType.FILE);
        }

        return messageRepository.save(message);
    }

    public List<Message> getMessagesByRoomId(String roomId) {
        return messageRepository.findByRoomId(roomId);
    }

    public Message editMessage(String messageId, String updatedContent, MessageType messageType, List<String> existingFiles, List<MultipartFile> newFiles) throws IOException {
        Message existingMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        existingMessage.addEditHistory(existingMessage.getContent(), new Date());
        existingMessage.setContent(updatedContent);
        existingMessage.setMessageType(messageType);

        List<String> updatedFileIds = new ArrayList<>();
        List<String> updatedFileNames = new ArrayList<>();

        // Keep existing files
        if (existingFiles != null && !existingFiles.isEmpty()) {
            for (int i = 0; i < existingFiles.size(); i++) {
                String fileId = existingFiles.get(i);
                String fileName = existingMessage.getFileNames().get(existingMessage.getFileIds().indexOf(fileId));
                updatedFileIds.add(fileId);
                updatedFileNames.add(fileName);
            }
        }

        // Add new files
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                String fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType()).toString();
                updatedFileIds.add(fileId);
                updatedFileNames.add(file.getOriginalFilename());
            }
        }

        existingMessage.setFileIds(updatedFileIds);
        existingMessage.setFileNames(updatedFileNames);

        return messageRepository.save(existingMessage);
    }

}


