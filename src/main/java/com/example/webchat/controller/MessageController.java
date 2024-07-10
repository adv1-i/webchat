package com.example.webchat.controller;

import com.example.webchat.exception.MaxFileSizeExceededException;
import com.example.webchat.exception.MaxFilesExceededException;
import com.example.webchat.service.MessageService;
import com.example.webchat.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.sendMessage(message));
    }


    @PostMapping("/send-with-files")
    public ResponseEntity<?> sendMessageWithFiles(
            @RequestParam(required = false) String content,
            @RequestParam String sender,
            @RequestParam String roomId,
            @RequestParam String recipients,
            @RequestParam(required = false) List<MultipartFile> files
    ) {
        try {
            Message message = messageService.sendMessageWithFiles(content, sender, roomId, recipients, files);
            return ResponseEntity.ok(message);
        } catch (MaxFileSizeExceededException e) {
            return ResponseEntity.badRequest().body("File size exceeds the limit of 15 MB");
        } catch (MaxFilesExceededException e) {
            return ResponseEntity.badRequest().body("Maximum number of files (10) exceeded");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Message>> getMessagesByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(messageService.getMessagesByRoomId(roomId));
    }
}

