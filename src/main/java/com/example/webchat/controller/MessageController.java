package com.example.webchat.controller;

import com.example.webchat.enums.MessageStatus;
import com.example.webchat.enums.MessageType;
import com.example.webchat.exception.MaxFileSizeExceededException;
import com.example.webchat.exception.MaxFilesExceededException;
import com.example.webchat.exception.MessageNotFoundException;
import com.example.webchat.service.MessageService;
import com.example.webchat.model.Message;
import com.example.webchat.service.MessageStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    MessageStatusService messageStatusService;

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

//    @GetMapping("/room/{roomId}")
//    public ResponseEntity<List<Message>> getMessagesByRoomId(@PathVariable String roomId) {
//        return ResponseEntity.ok(messageService.getMessagesByRoomId(roomId));
//    }

    @PutMapping("/{messageId}")
    public ResponseEntity<?> editMessage(
            @PathVariable String messageId,
            @RequestParam String content,
            @RequestParam MessageType messageType,
            @RequestParam(required = false) List<String> existingFiles,
            @RequestParam(required = false) List<MultipartFile> newFiles,
            Principal principal) {
        try {
            Message editedMessage = messageService.editMessage(messageId, content, messageType, existingFiles, newFiles,
                                                                principal.getName());
            return ResponseEntity.ok(editedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Message>> getMessagesByRoomId(@PathVariable String roomId,
                                                             @RequestParam(required = false) String userTimeZone) {
        ZoneId zoneId = userTimeZone != null ? ZoneId.of(userTimeZone) : ZoneId.systemDefault();
        List<Message> messages = messageService.getMessagesByRoomId(roomId);

        if (messages == null) {
            messages = new ArrayList<>();
        }

        messages.forEach(message -> {
            if (message.getTimestamp() != null) {
                String formattedTime = messageService.formatMessageTime(message.getTimestamp(), zoneId);
                message.setFormattedTime(formattedTime);
            } else {
                message.setFormattedTime("");
            }
        });
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/forward")
    public ResponseEntity<?> forwardMessage(
            @RequestParam String messageId,
            @RequestParam String targetRoomId,
            Principal principal) {
        try {
            Message forwardedMessage = messageService.forwardMessage(messageId, targetRoomId, principal.getName());
            return ResponseEntity.ok(forwardedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{messageId}/status")
    public ResponseEntity<Message> updateMessageStatus(
            @PathVariable String messageId,
            @RequestParam MessageStatus status,
            Principal principal) throws MessageNotFoundException {
        Message updatedMessage = messageStatusService.updateMessageStatus(messageId, status, principal.getName());
        return ResponseEntity.ok(updatedMessage);
    }

    @PostMapping("/room/{roomId}/delivered")
    public ResponseEntity<?> markMessagesAsDelivered(
            @PathVariable String roomId,
            Principal principal) throws MessageNotFoundException {
        messageStatusService.markMessagesAsDelivered(roomId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> markMessagesAsRead(
            @PathVariable String roomId,
            Principal principal) throws MessageNotFoundException {
        messageStatusService.markMessagesAsRead(roomId, principal.getName());
        return ResponseEntity.ok().build();
    }
}

