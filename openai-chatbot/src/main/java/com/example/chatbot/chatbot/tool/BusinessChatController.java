package com.example.chatbot.tool;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BusinessChatController {

    private final BusinessChatService businessChatService;

    public BusinessChatController(BusinessChatService businessChatService) {
        this.businessChatService = businessChatService;
    }

    @PostMapping("/business-chat")
    public Map<String, String> chat(@RequestBody BusinessChatRequest request) throws Exception {
        String answer = businessChatService.chatWithTool(request.conversationId(), request.question());
        return Map.of("answer", answer);
    }

    public record BusinessChatRequest(String conversationId, String question) { }
}

