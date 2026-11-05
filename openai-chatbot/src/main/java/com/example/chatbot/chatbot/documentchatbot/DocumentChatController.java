package com.example.chatbot.documentchatbot;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document-chat")
public class DocumentChatController {
    private final DocumentOpenAIService service;

    public DocumentChatController(DocumentOpenAIService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return new ChatResponse(service.ask(request.message()));
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String response) {}
}
