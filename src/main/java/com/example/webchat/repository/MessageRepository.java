package com.example.webchat.repository;

import com.example.webchat.enums.MessageStatus;
import com.example.webchat.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByRoomId(String roomId);

    List<Message> findByRoomIdAndMessageStatus(String roomId, MessageStatus messageStatus);

    List<Message> findByRoomIdAndMessageStatusIn(String roomId, List<MessageStatus> sent);
}


