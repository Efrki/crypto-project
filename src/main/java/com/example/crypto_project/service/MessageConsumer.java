package com.example.crypto_project.service;

import com.example.crypto_project.config.RabbitMQConfig;
import com.example.crypto_project.dto.ChatMessage;
import com.example.crypto_project.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ObjectMapper objectMapper; // Для более гибкой работы с уведомлениями

@RabbitListener(queues = RabbitMQConfig.MESSAGES_QUEUE_NAME)
    public void receiveChatMessage(ChatMessage message) {
        System.out.println("Received message from RabbitMQ for chat " + message.getChatRoomId() + " from " + message.getSenderUsername());
        System.out.println("Message content: " + message.getCiphertext());

        chatRoomRepository.findById(message.getChatRoomId()).ifPresentOrElse(
            chatRoom -> {
                // Отправляем сообщение всем участникам чата, кроме отправителя
                chatRoom.getParticipants().stream()
                    .filter(participant -> !participant.getUser().getLogin().equals(message.getSenderUsername()))
                    .forEach(recipient -> {
                        String recipientUsername = recipient.getUser().getLogin();
                        System.out.println("Forwarding message to user: " + recipientUsername);
                        messagingTemplate.convertAndSendToUser(recipientUsername, "/queue/messages", message);
                        messagingTemplate.convertAndSend("/topic/messages." + recipientUsername, message);
                        System.out.println("Message forwarded successfully");
                    });
            },
            () -> System.err.println("Cannot find chat room with ID: " + message.getChatRoomId() + ". Message will be lost.")
        );
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE_NAME)
    public void receiveNotification(Map<String, Object> notification) {
        System.out.println("Received notification from RabbitMQ: " + notification);

        try {
            // Используем ObjectMapper для более безопасного преобразования
            String recipient = objectMapper.convertValue(notification.get("recipient"), String.class);
            String type = objectMapper.convertValue(notification.get("type"), String.class);

            if (recipient == null || type == null) {
                System.err.println("Notification is missing a recipient or type: " + notification);
                return;
            }

            // Все уведомления для пользователя отправляем на один и тот же, хорошо известный канал.
            // Это упрощает логику на стороне клиента - ему нужно слушать только один канал для всех нотификаций.
            final String destination = "/queue/notifications";

            // Проверяем, что тип уведомления нам известен. В будущем можно добавить больше типов.
            if (!"CONTACT_UPDATE".equals(type) && !"CHAT_UPDATE".equals(type) && !"CHAT_CLOSED".equals(type)) {
                System.err.println("Received notification of unknown type: " + type);
            }

            System.out.println("Forwarding notification of type '" + type + "' to user: " + recipient);
            messagingTemplate.convertAndSendToUser(recipient, "/queue/notifications", notification);
            messagingTemplate.convertAndSend("/topic/notifications." + recipient, notification);
            System.out.println("Notification sent to both /user and /topic");

        } catch (Exception e) {
            System.err.println("Failed to process notification: " + notification + ". Error: " + e.getMessage());
        }
    }
}