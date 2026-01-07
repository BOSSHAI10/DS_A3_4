package com.example.chat.services;

import com.example.chat.dtos.ChatMessageDTO;
import com.example.chat.entities.ChatMessage;
import com.example.chat.repositories.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    /**
     * Logica de salvare a mesajului
     */
    public ChatMessage saveMessage(ChatMessageDTO dto, String actualSenderId) {
        // Aici am putea folosi un Builder separat, dar Lombok @Builder e suficient inline
        ChatMessage entity = ChatMessage.builder()
                .senderId(actualSenderId)
                .receiverId(dto.getReceiverId())
                .content(dto.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        return chatRepository.save(entity);
    }

    /**
     * Logica de obținere a istoricului cu verificări de securitate
     */
    public List<ChatMessage> getConversation(String user1, String user2, String requesterId, boolean isAdmin) {
        // Validare Business: Are voie să vadă mesajele?
        if (!isAdmin && !requesterId.equals(user1) && !requesterId.equals(user2)) {
            throw new RuntimeException("Access Denied"); // Sau o excepție custom
        }

        return chatRepository.findChatHistory(user1, user2);
    }

    public List<String> getAdminConversations(String adminId) {
        return chatRepository.findActiveChatUsers(adminId);
    }
}