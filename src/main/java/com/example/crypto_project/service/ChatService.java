package com.example.crypto_project.service;

import com.example.crypto_project.dto.ChatRoomDto;
import com.example.crypto_project.dto.CreateChatRequest;
import com.example.crypto_project.dto.JoinChatRequest;
import com.example.crypto_project.model.*;
import com.example.crypto_project.repository.ChatParticipantRepository;
import com.example.crypto_project.repository.ChatRoomRepository;
import com.example.crypto_project.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ContactService contactService;
    private final NotificationService notificationService;
    private final EntityManager entityManager;

    @Transactional
    public ChatRoom createChatRoom(CreateChatRequest request, String initiatorUsername) {
        // 1. Находим пользователей
        User initiator = userRepository.findByLogin(initiatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Initiator not found: " + initiatorUsername));
        User contact = userRepository.findByLogin(request.getContactUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Contact not found: " + request.getContactUsername()));

        // 2. Проверяем, являются ли они подтвержденными контактами
        boolean areContacts = contactService.areUsersContacts(initiator, contact);
        if (!areContacts) {
            throw new IllegalStateException("You can only start a chat with an accepted contact.");
        }

        // 3. Проверяем валидность алгоритма
        if (!Arrays.asList("RC6", "Twofish").contains(request.getEncryptionAlgorithm())) {
            throw new IllegalArgumentException("Invalid encryption algorithm specified.");
        }

        // 4. Создаем комнату
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setStatus(ChatRoomStatus.PENDING);
        chatRoom.setEncryptionAlgorithm(request.getEncryptionAlgorithm());

        // 5. Создаем участников и связываем с комнатой
        ChatParticipant initiatorParticipant = new ChatParticipant();
        initiatorParticipant.setUser(initiator);
        initiatorParticipant.setChatRoom(chatRoom);
        initiatorParticipant.setDhPublicKey(request.getDhPublicKey());

        ChatParticipant contactParticipant = new ChatParticipant();
        contactParticipant.setUser(contact);
        contactParticipant.setChatRoom(chatRoom);
        // Публичный ключ второго участника пока null

        chatRoom.setParticipants(Set.of(initiatorParticipant, contactParticipant));

        ChatRoom saved = chatRoomRepository.save(chatRoom);
        
        // Уведомляем обоих участников о новом чате
        notificationService.sendChatUpdate(initiator.getLogin());
        notificationService.sendChatUpdate(contact.getLogin());
        
        return saved;
    }

    @Transactional
    public ChatRoom joinChatRoom(Long roomId, JoinChatRequest request, String joiningUsername) {
        // 1. Находим комнату и пользователя
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Chat room not found."));
        User joiningUser = userRepository.findByLogin(joiningUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + joiningUsername));

        // 2. Проверяем, что комната ожидает подключения
        if (chatRoom.getStatus() != ChatRoomStatus.PENDING) {
            throw new IllegalStateException("This chat room is not awaiting a participant.");
        }

        // 3. Находим участника, который присоединяется
        ChatParticipant joiningParticipant = chatRoom.getParticipants().stream()
                .filter(p -> p.getUser().equals(joiningUser))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("You are not a participant of this chat room."));

        // 4. Проверяем, что он еще не присоединился (его ключ пуст)
        // Ключ участника, который присоединяется, должен быть null.
        // Если он не null, значит, этот пользователь либо уже присоединился,
        // либо он является инициатором чата. В обоих случаях - ошибка.
        // Инициатором является тот, кто НЕ равен contactUser из запроса на создание чата.
        // Но проще проверить, чей ключ НЕ null.
        ChatParticipant initiator = chatRoom.getParticipants().stream()
                .filter(p -> p.getDhPublicKey() != null)
                .findFirst().orElse(null);
        if (initiator != null && initiator.getUser().equals(joiningUser)) {
            throw new IllegalStateException("You have already joined this chat.");
        }

        // 5. Сохраняем его публичный ключ и активируем комнату
        joiningParticipant.setDhPublicKey(request.getDhPublicKey());
        chatRoom.setStatus(ChatRoomStatus.ACTIVE);

        ChatRoom saved = chatRoomRepository.save(chatRoom);
        
        // Уведомляем обоих участников об активации чата
        chatRoom.getParticipants().forEach(p -> 
            notificationService.sendChatUpdate(p.getUser().getLogin())
        );
        
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatsForUser(String username) {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<ChatRoom> chatRooms = entityManager.createQuery(
                        "SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p.user = :user", ChatRoom.class)
                .setParameter("user", user)
                .getResultList();

        return chatRooms.stream().map(chatRoom -> new ChatRoomDto(
                chatRoom.getId(),
                chatRoom.getStatus().name(),
                chatRoom.getEncryptionAlgorithm(),
                chatRoom.getParticipants().stream()
                        .map(p -> p.getUser().getLogin())
                        .collect(Collectors.toList())
        )).collect(Collectors.toList());
    }

    @Transactional
    public void closeChatRoom(Long roomId, String username) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Chat room not found."));

        boolean isParticipant = chatRoom.getParticipants().stream()
                .anyMatch(p -> p.getUser().getLogin().equals(username));

        if (!isParticipant) {
            throw new SecurityException("You are not a participant of this chat room.");
        }

        // Уведомляем участников о закрытии чата через WebSocket
        List<String> participantUsernames = new ArrayList<>();
        chatRoom.getParticipants().forEach(p -> participantUsernames.add(p.getUser().getLogin()));

        // Удаляем комнату. Благодаря `cascade = CascadeType.ALL` участники удалятся автоматически.
        chatRoomRepository.delete(chatRoom);

        // Отправляем уведомление после успешного удаления
        for (String participantUsername : participantUsernames) {
            notificationService.sendChatClosed(participantUsername, roomId);
        }
    }

    @Transactional(readOnly = true)
    public String getOtherPartyPublicKey(Long roomId, String username) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Chat room not found."));

        return chatRoom.getParticipants().stream()
                .filter(p -> !p.getUser().getLogin().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Other participant not found."))
                .getDhPublicKey();
    }
}