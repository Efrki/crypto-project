package com.example.crypto_project.repository;

import com.example.crypto_project.model.LocalMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalMessageRepository extends JpaRepository<LocalMessage, Long> {
    List<LocalMessage> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);
}