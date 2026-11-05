package com.example.chatbot.mysqlstore.entity.repo;


import com.example.chatbot.mysqlstore.entity.ChatConversation;
import com.example.chatbot.mysqlstore.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    // Retrieve latest messages for LLM context.
    List<ChatMessage> findByConversationIdOrderByMessageIdDesc(String conversationId, Pageable pageable);

    // Retrieve complete history for the UI.
    List<ChatMessage> findByConversationIdOrderByMessageIdAsc(String conversationId);
}

