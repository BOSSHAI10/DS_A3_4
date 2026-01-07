package com.example.chat.controllers;

import com.example.chat.dtos.ChatMessageDTO;
import com.example.chat.entities.ChatMessage;
import com.example.chat.repositories.ChatRepository;
import com.example.chat.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.security.Principal; // <--- Asigură-te că ai acest import

import java.time.LocalDateTime;
import java.util.List;
/*
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired // <-- Injectăm Repository-ul
    private ChatRepository chatRepository;

    // Endpoint apelat prin: /app/private-message
    @MessageMapping("/private-message")
    public ChatMessageDTO receiveMessage(@Payload ChatMessageDTO message, Principal principal) {
        // Aici poți salva mesajul în baza de date (dacă cerința cere persistență)
        /*
        // Logica de trimitere:
        // Mesajul ajunge la subscriberii canalului: /user/{receiverId}/private
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/private",
                message
        );

        // Opțional: Trimite înapoi la sender pentru confirmare/afisare
        return message;
        */
/*
        String userIdReal = principal.getName();

        // Suprascriem senderId din mesaj cu cel real, garantat de sistem
        message.setSenderId(userIdReal);

        // 1. Creăm entitatea din DTO
        ChatMessage entity = ChatMessage.builder()
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .timestamp(LocalDateTime.now()) // Setăm ora serverului
                .build();

        // 2. Salvăm în baza de date
        chatRepository.save(entity);

        // 3. Trimitem mesajul către destinatar (prin WebSocket)
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/private",
                message
        );

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

    /*@GetMapping("/history/{user1}/{user2}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String user1, @PathVariable String user2) {
        return ResponseEntity.ok(chatRepository.findChatHistory(user1, user2));
    }*/
/*
    // Adaugă asta în ChatController dacă nu există deja,
    // pentru ca regula din SecurityConfig să aibă sens.
    @GetMapping("/admin/conversations")
    public ResponseEntity<List<String>> getConversations(Principal principal) {
        // 1. Obținem ID-ul adminului conectat (ex: "admin" sau UUID-ul lui)
        String adminId = principal.getName();

        // 2. Îl transmitem către repository pentru a găsi mesajele primite de EL
        return ResponseEntity.ok(chatRepository.findActiveChatUsers(adminId));
    }

    @GetMapping("/history/{user1}/{user2}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String user1,
            @PathVariable String user2,
            Principal principal,
            Authentication authentication) { // Injectăm Authentication pentru a verifica rolurile

        String callingUser = principal.getName(); // ID-ul celui care face cererea (ex: "Ion")

        // Verificăm dacă cel care cere este ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        // REGULA DE AUR:
        // Ai voie să vezi datele DOAR DACĂ:
        // 1. Ești Admin (vezi tot)
        // 2. SAU ești unul dintre participanții la conversație (user1 sau user2)
        if (!isAdmin && !callingUser.equals(user1) && !callingUser.equals(user2)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }

        return ResponseEntity.ok(chatRepository.findChatHistory(user1, user2));
    }
}*/

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService; // Injectăm Service-ul, NU Repository-ul direct

    @MessageMapping("/private-message")
    public void receiveMessage(@Payload ChatMessageDTO message, Principal principal) {
        String userIdReal = principal.getName();

        // 1. Apelăm logica din service
        ChatMessage savedMsg = chatService.saveMessage(message, userIdReal);

        // 2. Facem mapare înapoi către DTO pentru a trimite la client (sau trimitem entitatea direct dacă e simplă)
        message.setSenderId(userIdReal);
        message.setTimestamp(savedMsg.getTimestamp());

        // 3. Trimitem notificarea (asta ține de transport, deci poate rămâne în controller sau mutat în service)
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/private",
                message
        );
    }

    @GetMapping("/history/{user1}/{user2}")
    public ResponseEntity<?> getChatHistory(
            @PathVariable String user1,
            @PathVariable String user2,
            Principal principal,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        try {
            // Controller-ul doar deleagă responsabilitatea
            List<ChatMessage> history = chatService.getConversation(user1, user2, principal.getName(), isAdmin);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}