package com.example.chatbot.conversational;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationMemoryStore {
    public record ChatTurn(String question, String answer) {}

    private static final int MAX_TURNS = 5;

    private final Map<String, Deque<ChatTurn>> conversations = new ConcurrentHashMap<>();

    public List<ChatTurn> getHistory(String conversationId) {
        Deque<ChatTurn> turns = conversations.get(conversationId);
        if (turns == null) {
            return List.of();
        }
        synchronized (turns) {
            return new ArrayList<>(turns);
        }
    }

    public void save(String conversationId, String question, String answer) {

        Deque<ChatTurn> turns = conversations.computeIfAbsent(conversationId, id -> new ArrayDeque<>());
        synchronized (turns) {
            turns.addLast(new ChatTurn(question, answer));
            while (turns.size() > MAX_TURNS) {
                turns.removeFirst();
            }
        }
    }

}
