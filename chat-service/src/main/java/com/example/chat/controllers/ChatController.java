package com.example.chat.controllers;

import com.example.chat.dtos.ChatMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Endpoint apelat prin: /app/private-message
    @MessageMapping("/private-message")
    public ChatMessageDTO receiveMessage(@Payload ChatMessageDTO message) {
        // Aici poți salva mesajul în baza de date (dacă cerința cere persistență)

        // Logica de trimitere:
        // Mesajul ajunge la subscriberii canalului: /user/{receiverId}/private
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/private",
                message
        );

        // Opțional: Trimite înapoi la sender pentru confirmare/afisare
        return message;
    }

    // Endpoint pentru "User is typing..."
    @MessageMapping("/typing")
    public void userTyping(@Payload ChatMessageDTO message) {
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/typing",
                message
        );
    }
}