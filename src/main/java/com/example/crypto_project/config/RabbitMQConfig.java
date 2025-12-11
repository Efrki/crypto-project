package com.example.crypto_project.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "app_exchange";
    public static final String MESSAGES_QUEUE_NAME = "messages_queue";
    public static final String NOTIFICATIONS_QUEUE_NAME = "notifications_queue";
    public static final String MESSAGES_ROUTING_KEY = "messages.key";
    public static final String NOTIFICATIONS_ROUTING_KEY = "notifications.key";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue messagesQueue() {
        return new Queue(MESSAGES_QUEUE_NAME);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(NOTIFICATIONS_QUEUE_NAME);
    }

    @Bean
    public Binding messagesBinding(Queue messagesQueue, TopicExchange exchange) {
        return BindingBuilder.bind(messagesQueue).to(exchange).with(MESSAGES_ROUTING_KEY);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notificationsQueue).to(exchange).with(NOTIFICATIONS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}