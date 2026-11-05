package com.example.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ChatbotUtility {
    private final RestClient client;
    @Value("${model}")
    private String model;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatbotUtility(@Value("${openai.api-key}") String key){
        this.client = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String callOpenAI(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", prompt
                                        )
                                )
                        )
                )
        );

        String raw = client.post()
                .uri("/v1/responses")
                .body(body)
                .retrieve()
                .body(String.class);
        try {
            JsonNode json = mapper.readTree(raw);
            StringBuilder answer = new StringBuilder();

            for (JsonNode item : json.path("output")) {
                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(
                            content.path("type").asText())) {
                        answer.append(
                                content.path("text").asText());
                    }
                }
            }

            if (answer.length() == 0) {
                throw new IllegalStateException(
                        "No output text returned by OpenAI");
            }
            return answer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse OpenAI response", e);
        }
    }
}
