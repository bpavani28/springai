package com.example.chatbot.documentchatbot;

import com.example.chatbot.docChunk.DocumentChunkingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentOpenAIService {
    private final RestClient client;
    private final DocumentService documents;
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${model}")
    private String model;

    @Autowired
    private DocumentChunkingService documentChunkingService;
    @Autowired
    private VectorStore vectorStore;

    public DocumentOpenAIService(@Value("${openai.api-key}") String key,
                                 DocumentService documents) {
        this.documents = documents;
        this.client = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String plainDocReading(String question) {
        String context = documents.readDocuments();

        if (context.isBlank()) {
            return "No .txt documents were found in the documents folder.";
        }
/*   "I could not find that information in the documents."
                 Do not make up information.*/
        String prompt_with_tool = """
                You are a helpful assistant.
                 Follow these rules:
                    1. First, try to answer using the provided documents.
                    2. If the documents contain the answer,
                       answer using the documents.
                    3. If the documents do not contain the answer,
                       use the available web search tool.
                    4. Do not ask the user for permission to search.
                    5. Clearly mention whether the answer came
                       from documents or web search.
                    6. Never invent information.

                DOCUMENTS:
                %s

                USER QUESTION:
                %s
                """.formatted(context, question);

        String prompt = """
                You are a helpful assistant.
                  Follow these rules:
                    1. First, try to answer using the provided documents.
                    2. If the documents contain the answer, answer using
                       the documents only.
                    3. If the documents do not contain the answer, use
                       your pretrained knowledge to answer.
                    4. Do not use web search or external tools.
                    5. Clearly identify the source of the answer:
                       - "Source: Provided documents"
                       - "Source: Pretrained model knowledge"
                       - "Source: Partially from documents and
                          partially from pretrained model knowledge"
                    6. Never invent information.
                    7. If you are uncertain or do not know the answer,
                       explicitly say so.
                    8. Do not claim that information comes from the
                       documents unless it is actually supported by them.

                DOCUMENTS:
                %s

                USER QUESTION:
                %s
                """.formatted(context, question);

        Map<String, Object> body = Map.of(
                "model", model,
//                "tools", List.of(Map.of("type", "web_search")),
//                "tool_choice", "auto",
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

    public String ask(String question) {
        return ragDocumentChat(question);
    }



    public String ragDocumentChat(String question) {

        // STEP 1: Search existing Vector DB
        List<Document> relevantChunks =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(question)
                                .topK(5).similarityThreshold(0.55)
                                .build()
                );

        // STEP 2: Check retrieved results
        if (relevantChunks == null || relevantChunks.isEmpty()) {
            System.out.println("No relevant information found in Vector DB.");
        }

        for (Document doc : relevantChunks) {
            System.out.println("Source: " + doc.getMetadata().get("source"));
            System.out.println("Similarity Score: " + doc.getScore());
            System.out.println("-----------------------");
        }

        // STEP 3: Build context using retrieved chunks
        String context = relevantChunks.stream()
                .map(doc -> {
                    String source = String.valueOf(
                            doc.getMetadata()
                                    .getOrDefault("source", "Unknown"));

                    return "Source: " + source
                            + "\nContent: " + doc.getText();
                })
                .collect(Collectors.joining("\n\n"));

        // STEP 4: Prepare prompt for GPT
        String prompt = """
                You are a helpful assistant.
                  Follow these rules:
                    1. First, try to answer using the provided documents.
                    2. If the documents contain the answer, answer using
                       the documents only.
                    3. If the documents do not contain the answer, use
                       your pretrained knowledge to answer.
                    4. Do not use web search or external tools.
                    5. Clearly identify the source of the answer:
                       - "Source: Provided documents"
                       - "Source: Pretrained model knowledge"
                       - "Source: Partially from documents and
                          partially from pretrained model knowledge"
                    6. Never invent information.
                    7. If you are uncertain or do not know the answer,
                       explicitly say so.
                    8. Do not claim that information comes from the
                       documents unless it is actually supported by them.

                DOCUMENTS:
                %s

                USER QUESTION:
                %s
                """.formatted(context, question);

        // STEP 5: Call your existing OpenAI Responses API
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

        // STEP 6: Extract GPT response
        try {
            JsonNode json = mapper.readTree(raw);
            StringBuilder answer = new StringBuilder();
            for (JsonNode item : json.path("output")) {
                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(
                            content.path("type").asText())) {
                        answer.append(content.path("text").asText());
                    }
                }
            }

            return answer.isEmpty() ? "No response received from OpenAI." : answer.toString();

        } catch (Exception e) {
            throw new RuntimeException("Unable to parse RAG response", e);
        }
    }
}
