package com.example.chatbot.documentchatbot;

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
public class DocumentOpenAIService {
    private final RestClient client;
    private final DocumentService documents;
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${model}")
    private String model;

    public DocumentOpenAIService(@Value("${openai.api-key}") String key,
                                 DocumentService documents) {
        this.documents = documents;
        this.client = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String ask(String question) {
        String context = documents.readDocuments();

        if (context.isBlank()) {
            return "No .txt documents were found in the documents folder.";
        }
/*   "I could not find that information in the documents."
                 Do not make up information.*/
        String prompt = """
                You are a helpful assistant.
                Answer the user's question using ONLY the information in the documents below.
                If the answer cannot be found in the documents, do web search
             

                DOCUMENTS:
                %s

                USER QUESTION:
                %s
                """.formatted(context, question);

        Map<String, Object> body = Map.of(
                "model", model,
                "input", List.of(Map.of(
                        "role", "user",
                        "content", List.of(Map.of(
                                "type", "input_text",
                                "text", prompt
                        ))
                ))
        );

        String raw = client.post()
                .uri("/v1/responses")
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode json = mapper.readTree(raw);
            JsonNode outputText = json.get("output_text");
            if (outputText != null && !outputText.isNull()) {
                return outputText.asText();
            }

            for (JsonNode item : json.path("output")) {
                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) {
                        return content.path("text").asText();
                    }
                }
            }

            return "No text was returned by OpenAI.";
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse OpenAI response", e);
        }
    }
}
