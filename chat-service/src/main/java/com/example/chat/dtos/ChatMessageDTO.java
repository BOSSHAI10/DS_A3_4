package com.example.chat.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDTO {
    private String senderId;   // ID-ul utilizatorului sau 'admin'
    private String receiverId; // ID-ul destinatarului
    private String content;    // Textul mesajului
    private boolean seen;      // Status citire (optional)
    private boolean typing;    // Pentru notificări de "typing..." (optional)
    private LocalDateTime timestamp;
}