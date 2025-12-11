package com.example.crypto_project.service;

import com.example.crypto_project.config.RabbitMQConfig;
import com.example.crypto_project.dto.ChatMessage;
import com.example.crypto_project.model.ChatRoom;
import com.example.crypto_project.model.ChatRoomStatus;
import com.example.crypto_project.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RabbitTemplate rabbitTemplate;
    private final ChatRoomRepository chatRoomRepository;

    public void sendMessage(ChatMessage message, String senderUsername) {
        // 1. Проверяем, что отправитель - тот, за кого себя выдает
        if (!message.getSenderUsername().equals(senderUsername)) {
            throw new SecurityException("Sender username does not match authenticated user.");
        }

        // 2. Проверяем, существует ли чат и активен ли он
        ChatRoom chatRoom = chatRoomRepository.findById(message.getChatRoomId())
                .orElseThrow(() -> new IllegalStateException("Chat room not found."));

        if (chatRoom.getStatus() != ChatRoomStatus.ACTIVE) {
            throw new IllegalStateException("Chat room is not active.");
        }

        // 3. Проверяем, является ли отправитель участником этого чата
        boolean isParticipant = chatRoom.getParticipants().stream()
                .anyMatch(p -> p.getUser().getLogin().equals(senderUsername));
        if (!isParticipant) {
            throw new SecurityException("You are not a participant of this chat room.");
        }

        // 4. Отправляем сообщение в RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.MESSAGES_ROUTING_KEY, message);
    }
}