package com.example.chatbot.mysqlstore.entity.repo;


import com.example.chatbot.mysqlstore.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, String> {

    List<ChatConversation> findAllByOrderByUpdatedAtDesc();
}

