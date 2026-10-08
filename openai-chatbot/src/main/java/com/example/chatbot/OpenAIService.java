package com.example.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${model}")
    private String model;

    public OpenAIService(@Value("${openai.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String ask(String message) {
        if (message == null || message.isBlank()) {
            return "Please enter a message.";
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", message
                                        )
                                )
                        )
                )
        );

        String response = restClient.post()
                .uri("/v1/responses")
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode json = objectMapper.readTree(response);
            JsonNode outputText = json.get("output_text");

            if (outputText != null && !outputText.isNull()) {
                return outputText.asText();
            }

            JsonNode output = json.path("output");
            for (JsonNode item : output) {
                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) {
                        return content.path("text").asText();
                    }
                }
            }

            return "OpenAI returned a response, but no text was found.";
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse OpenAI response", e);
        }
    }
}
