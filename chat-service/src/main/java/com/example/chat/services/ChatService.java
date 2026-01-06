package com.example.chat.services;

import com.example.chat.dtos.ChatMessageDTO;
import com.example.chat.entities.ChatMessage;
import com.example.chat.repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessageDTO saveMessage(ChatMessageDTO messageDTO) {
        // Mapare DTO -> Entity
        ChatMessage message = ChatMessage.builder()
                .senderId(messageDTO.getSenderId())
                .recipientId(messageDTO.getRecipientId())
                .content(messageDTO.getContent())
                .timestamp(messageDTO.getTimestamp())
                .build();

        // Salvare
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Mapare Entity -> DTO (pentru a returna obiectul cu ID-ul generat)
        return mapToDTO(savedMessage);
    }

    public List<ChatMessageDTO> getConversation(UUID user1, UUID user2) {
        // Apelează metoda din Repository (asigură-te că numele e corect în Repo)
        List<ChatMessage> messages = chatMessageRepository.findChatHistory(user1, user2);

        return messages.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Helper pentru mapare
    private ChatMessageDTO mapToDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .recipientId(message.getRecipientId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}