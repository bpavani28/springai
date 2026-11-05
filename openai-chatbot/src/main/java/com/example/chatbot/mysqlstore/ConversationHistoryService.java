package com.example.chatbot.mysqlstore;


import com.example.chatbot.mysqlstore.entity.ChatConversation;
import com.example.chatbot.mysqlstore.entity.ChatMessage;
import com.example.chatbot.mysqlstore.entity.repo.ChatConversationRepository;
import com.example.chatbot.mysqlstore.entity.repo.ChatMessageRepository;
import jakarta.persistence.*;

import java.time.LocalDateTime;


import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ConversationHistoryService {

    private final ChatConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;

    public ConversationHistoryService(ChatConversationRepository conversationRepo, ChatMessageRepository messageRepo) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getRecentHistory(String conversationId) {
        List<ChatMessage> messages = new ArrayList<>(
                messageRepo.findByConversationIdOrderByMessageIdDesc(
                                conversationId,
                                PageRequest.of(0, 10)));
        Collections.reverse(messages);
        return messages;
    }

    @Transactional
    public void saveTurn(String conversationId, String question, String answer) {
        ChatConversation conversation = conversationRepo.findById(conversationId)
                        .orElseGet(() -> {
                            String title = question.length() > 100
                                    ? question.substring(0, 100)
                                    : question;
                            return new ChatConversation(conversationId, title);
                        });

        conversation.touch();
        conversationRepo.save(conversation);
        messageRepo.save(new ChatMessage(conversationId, "user", question));
        messageRepo.save(new ChatMessage(conversationId, "assistant", answer));
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> listConversations() {
        return conversationRepo.findAllByOrderByUpdatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(String conversationId) {
        return messageRepo.findByConversationIdOrderByMessageIdAsc(conversationId);
    }

    @Transactional
    public void deleteConversation(String conversationId) {
        conversationRepo.deleteById(conversationId);
    }
}
