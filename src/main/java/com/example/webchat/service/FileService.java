package com.example.webchat.service;

import com.example.webchat.exception.MaxFileSizeExceededException;
import com.example.webchat.exception.MaxFilesExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;
    private static final int MAX_FILES = 10;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    public void validateFiles(List<MultipartFile> files) throws MaxFileSizeExceededException, MaxFilesExceededException {
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
    }

    public List<String> storeFiles(List<MultipartFile> files) throws IOException {
        List<String> fileIds = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType()).toString();
                fileIds.add(fileId);
            }
        }
        return fileIds;
    }

    public List<String> getFileNames(List<MultipartFile> files) {
        List<String> fileNames = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                fileNames.add(file.getOriginalFilename());
            }
        }
        return fileNames;
    }
}
