package com.example.crypto_project.service;

import com.example.crypto_project.model.Contact;
import com.example.crypto_project.model.ContactStatus;
import com.example.crypto_project.model.User;
import com.example.crypto_project.repository.ContactRepository;
import com.example.crypto_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final NotificationService notificationService;

    @Transactional
    public Contact sendContactRequest(String fromUsername, String toUsername) {
        if (fromUsername.equals(toUsername)) {
            throw new IllegalStateException("You cannot add yourself as a contact.");
        }

        User fromUser = userRepository.findByLogin(fromUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + fromUsername));
        User toUser = userRepository.findByLogin(toUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + toUsername));

        // Проверяем, не был ли запрос уже отправлен в любом направлении
        boolean requestExists = contactRepository.findByUserAndContactUser(fromUser, toUser).isPresent() ||
                                contactRepository.findByUserAndContactUser(toUser, fromUser).isPresent();

        if (requestExists) {
            throw new IllegalStateException("A contact request already exists between these users.");
        }

        Contact newContact = new Contact();
        newContact.setUser(fromUser);
        newContact.setContactUser(toUser);
        newContact.setStatus(ContactStatus.PENDING);

        // Уведомляем получателя о новом запросе
        notificationService.sendContactUpdate(toUsername);

        return contactRepository.save(newContact);
    }

    @Transactional
    public Contact acceptContactRequest(Long contactId, String currentUsername) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalStateException("Contact request not found."));

        // Проверяем, что текущий пользователь является получателем запроса
        if (!contact.getContactUser().getLogin().equals(currentUsername)) {
            throw new IllegalStateException("You are not authorized to accept this request.");
        }

        contact.setStatus(ContactStatus.ACCEPTED);
        Contact savedContact = contactRepository.save(contact);

        // Уведомляем обоих пользователей об изменении
        notificationService.sendContactUpdate(savedContact.getUser().getLogin());
        notificationService.sendContactUpdate(savedContact.getContactUser().getLogin());
        return savedContact;
    }

    @Transactional
    public void declineOrDeleteContact(Long contactId, String currentUsername) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalStateException("Contact request not found."));

        // Убедимся, что текущий пользователь является либо отправителем, либо получателем
        boolean isUserInvolved = contact.getUser().getLogin().equals(currentUsername) ||
                                 contact.getContactUser().getLogin().equals(currentUsername);

        if (!isUserInvolved) {
            throw new IllegalStateException("You are not authorized to modify this contact.");
        }

        // Если запрос был в статусе PENDING, получатель его отклоняет.
        // Если статус был ACCEPTED, любой из участников может его удалить.
        contactRepository.delete(contact);

        // Уведомляем обоих пользователей об изменении
        notificationService.sendContactUpdate(contact.getUser().getLogin());
        notificationService.sendContactUpdate(contact.getContactUser().getLogin());
    }

    @Transactional(readOnly = true)
    public List<Contact> getPendingRequests(String currentUsername) {
        User user = userRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUsername));
        return contactRepository.findByContactUserAndStatus(user, ContactStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<User> getAcceptedContacts(String currentUsername) {
        User user = userRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUsername));

        return contactRepository.findAcceptedContacts(user, ContactStatus.ACCEPTED).stream()
                .flatMap(contact -> Stream.of(contact.getUser(), contact.getContactUser()))
                .filter(contactUser -> !contactUser.getLogin().equals(currentUsername))
                .distinct().toList();
    }

    @Transactional(readOnly = true)
    public boolean areUsersContacts(User user1, User user2) {
        // Ищем подтвержденный контакт между двумя пользователями в любом направлении
        return contactRepository.findAcceptedContactBetweenUsers(user1, user2, ContactStatus.ACCEPTED)
                .isPresent();
    }
}