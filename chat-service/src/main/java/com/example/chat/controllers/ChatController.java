package com.example.chat.controllers;

import com.example.chat.entities.ChatMessage;
import com.example.chat.repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@CrossOrigin
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // Endpoint WebSocket: /app/chat
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(new Date());

        // 1. Salvăm în DB
        ChatMessage savedMsg = chatMessageRepository.save(chatMessage);

        // 2. Trimitem către destinatar (ex: Admin)
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId().toString(),
                "/queue/messages",
                savedMsg
        );

        // 3. Trimitem și înapoi către expeditor (ca să vadă confirmarea/mesajul dublat dacă e nevoie,
        // deși React îl afișează de obicei optimistic)
        // Opțional, în funcție de logica frontend-ului.
    }

    // Endpoint REST: Încarcă istoricul
    @GetMapping("/messages/{user1}/{user2}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable UUID user1, @PathVariable UUID user2) {
        return ResponseEntity.ok(chatMessageRepository.findChatHistory(user1, user2));
    }
}