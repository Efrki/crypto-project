package com.example.crypto_project.repository;

import com.example.crypto_project.model.Contact;
import com.example.crypto_project.model.ContactStatus;
import com.example.crypto_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    // Найти все запросы, где пользователь является получателем и статус PENDING
    List<Contact> findByContactUserAndStatus(User contactUser, ContactStatus status);

    // Найти все подтвержденные контакты пользователя (где он либо отправитель, либо получатель)
    @Query("SELECT c FROM Contact c WHERE (c.user = :user OR c.contactUser = :user) AND c.status = :status")
    List<Contact> findAcceptedContacts(User user, ContactStatus status);

    Optional<Contact> findByUserAndContactUser(User user, User contactUser);

    // Найти подтвержденный контакт между двумя пользователями в любом направлении
    @Query("SELECT c FROM Contact c WHERE ((c.user = :user1 AND c.contactUser = :user2) OR (c.user = :user2 AND c.contactUser = :user1)) AND c.status = :status")
    Optional<Contact> findAcceptedContactBetweenUsers(User user1, User user2, ContactStatus status);
}