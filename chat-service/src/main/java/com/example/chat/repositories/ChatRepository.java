package com.example.chat.repositories;

import com.example.chat.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatMessage, UUID> {
    // Aici vei putea adăuga metode pentru a extrage istoricul, de exemplu:
    List<ChatMessage> findBySenderIdAndReceiverId(String senderId, String receiverId);

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.senderId = :user1 AND m.receiverId = :user2) OR " +
            "(m.senderId = :user2 AND m.receiverId = :user1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT DISTINCT m.senderId FROM ChatMessage m WHERE m.receiverId = :adminId")
    List<String> findActiveChatUsers(@Param("adminId") String adminId);
}