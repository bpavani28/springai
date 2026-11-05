package com.example.chatbot.conversational;

import com.example.chatbot.documentchatbot.DocumentOpenAIService;
import com.example.chatbot.mysqlstore.ConversationHistoryService;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private final ConversationalOpenAIService service;
    private final ConversationHistoryService historyService;

    public ConversationController(ConversationalOpenAIService service, ConversationHistoryService historyService) {
        this.historyService = historyService;
        this.service = service;
    }


    public record ConversationRequest(String conversationId, String question) {}


    @PostMapping("/conversations")
    public Map<String, String> chat(@RequestBody ConversationRequest request) {

        String answer = service.conversationalRagChat(request.conversationId(), request.question());
        return Map.of("answer", answer);
    }

 /*   .... Mysql persist Logic ....
 * */
    public record ConversationDto(
            String conversationId,
            String title,
            LocalDateTime updatedAt) {}

    public record MessageDto(
            Long messageId,
            String role,
            String content,
            LocalDateTime createdAt) {}

    @GetMapping("/conversations")
    public List<ConversationDto> list() {
        return historyService.listConversations()
                .stream()
                .map(c -> new ConversationDto(
                        c.getConversationId(),
                        c.getTitle(),
                        c.getUpdatedAt()))
                .toList();
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<MessageDto> messages(
            @PathVariable String conversationId) {

        return historyService.getMessages(conversationId)
                .stream()
                .map(m -> new MessageDto(
                        m.getMessageId(),
                        m.getRole(),
                        m.getContent(),
                        m.getCreatedAt()))
                .toList();
    }

    @DeleteMapping("/conversations/{conversationId}")
    public void delete(@PathVariable String conversationId) {
        historyService.deleteConversation(conversationId);
    }
}
