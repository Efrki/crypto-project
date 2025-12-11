package com.example.crypto_project.service;

import com.example.crypto_project.model.LocalMessage;
import com.example.crypto_project.repository.LocalMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalMessageService {
    
    private final LocalMessageRepository localMessageRepository;
    
    public void saveMessage(Long chatRoomId, String senderUsername, String content, boolean isOutgoing) {
        LocalMessage message = new LocalMessage();
        message.setChatRoomId(chatRoomId);
        message.setSenderUsername(senderUsername);
        message.setContent(content);
        message.setOutgoing(isOutgoing);
        localMessageRepository.save(message);
    }
    
    public List<LocalMessage> getChatHistory(Long chatRoomId) {
        return localMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }
}