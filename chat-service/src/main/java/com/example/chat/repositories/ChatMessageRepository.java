package com.example.chat.repositories;

import com.example.chat.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    // Returnează mesajele dintre doi useri, ordonate cronologic
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :user1 AND m.recipientId = :user2) " +
            "OR (m.senderId = :user2 AND m.recipientId = :user1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("user1") UUID user1, @Param("user2") UUID user2);
}