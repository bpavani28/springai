package com.example.chatbot.tool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class ToolService {

    @Autowired
    private LoadBusinessTools businessTools;
    private final RestClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${model}")
    private String model;

    public ToolService(@Value("${openai.api-key}") String key) {
        this.client = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String callOpenAIWithTools(String prompt)
            throws Exception {

        List<Map<String, Object>> input = new ArrayList<>();
        input.add(Map.of(
                "role", "user",
                "content", prompt
        ));

        for (int round = 0; round < 3; round++) {
            Map<String, Object> request = new LinkedHashMap<>();

            request.put("model", model);
            request.put("input", input);
            request.put("tools", businessTools.getToolDefinitions());
            request.put("tool_choice", "auto");
            request.put("parallel_tool_calls", false);
            request.put("store", false);

            // STEP 1: Ask OpenAI to answer or request a tool
            JsonNode response = client.post()
                    .uri("/v1/responses")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            // STEP 2: Verify response generation completed
            if (response == null ||
                    !"completed".equals(
                            response.path("status").asText())) {
                throw new IllegalStateException(
                        "OpenAI response incomplete");
            }

            JsonNode output = response.path("output");

            // STEP 3: Preserve model output for continuation
            for (JsonNode item : output) {
                input.add(mapper.convertValue(
                        item,
                        new TypeReference<Map<String, Object>>() {}
                ));
            }

            boolean toolCalled = false;

            // STEP 4: Check whether a tool was requested
            for (JsonNode item : output) {

                if (!"function_call".equals(
                        item.path("type").asText())) {
                    continue;
                }
                toolCalled = true;
                String toolName = item.path("name").asText();
                JsonNode arguments = mapper.readTree(item.path("arguments").asText("{}"));
                // STEP 5: Execute tool only when requested
                Object toolResult = businessTools.executeTool(
                        toolName, arguments
                );

                // STEP 6: Add tool result for next API call
                input.add(Map.of(
                        "type", "function_call_output",
                        "call_id", item.path("call_id").asText(),
                        "output", mapper.writeValueAsString(toolResult)
                ));
            }

            // STEP 7: If no tool call, return final answer
            if (!toolCalled) {
                return extractText(response);
            }

            // Otherwise loop and send tool result to OpenAI.
        }

        throw new IllegalStateException("Maximum tool-call rounds exceeded");
    }



    private String extractText(JsonNode response) {

        StringBuilder answer = new StringBuilder();
        JsonNode output = response.path("output");
        for (JsonNode item : output) {
            if (!"message".equals(item.path("type").asText())) {
                continue;
            }

            for (JsonNode content : item.path("content")) {
                if ("output_text".equals(content.path("type").asText())) {

                    String text = content.path("text").asText();
                    if (!text.isBlank()) {
                        if (!answer.isEmpty()) {
                            answer.append("\n");
                        }
                        answer.append(text);
                    }
                }
            }
        }

        if (answer.isEmpty()) {
            throw new IllegalStateException("OpenAI returned no text answer");
        }
        return answer.toString();
    }


}
