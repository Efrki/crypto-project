package com.example.crypto_project.service;

import com.example.crypto_project.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Отправляет уведомление об обновлении контактов в очередь RabbitMQ.
     */
    public void sendContactUpdate(String username) {
        Map<String, String> notification = Map.of("type", "CONTACT_UPDATE", "recipient", username);
        System.out.println("Sending CONTACT_UPDATE notification to RabbitMQ for user: " + username);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.NOTIFICATIONS_ROUTING_KEY, notification);
    }

    /**
     * Отправляет уведомление об обновлении чатов в очередь RabbitMQ.
     */
    public void sendChatUpdate(String username) {
        Map<String, String> notification = Map.of("type", "CHAT_UPDATE", "recipient", username);
        System.out.println("Sending CHAT_UPDATE notification to RabbitMQ for user: " + username);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.NOTIFICATIONS_ROUTING_KEY, notification);
    }

    /**
     * Отправляет уведомление о закрытии чата в очередь RabbitMQ.
     */
    public void sendChatClosed(String username, Long closedChatId) {
        Map<String, Object> notification = Map.of("type", "CHAT_CLOSED", "recipient", username, "chatId", closedChatId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.NOTIFICATIONS_ROUTING_KEY, notification);
    }
}