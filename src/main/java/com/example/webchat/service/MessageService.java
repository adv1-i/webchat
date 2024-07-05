package com.example.webchat.service;

import com.example.webchat.repository.MessageRepository;
import com.example.webchat.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public Message sendMessage(Message message) {
        LocalDateTime now = LocalDateTime.now();
        Date timestamp = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        message.setTimestamp(timestamp);
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByRoomId(String roomId) {
        return messageRepository.findByRoomId(roomId);
    }
}


