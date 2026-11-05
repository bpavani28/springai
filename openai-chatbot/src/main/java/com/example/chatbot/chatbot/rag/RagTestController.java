package com.example.chatbot.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagTestController {

    private final VectorStore vectorStore;

    public RagTestController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String question) {

        List<Document> results =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(question)
                                .topK(5)
                                .build()
                );

        return results.stream()
                .map(doc -> {
                    Map<String, Object> result =
                            new LinkedHashMap<>();

                    result.put("text", doc.getText());
                    result.put("metadata", doc.getMetadata());
                    result.put("score", doc.getScore());

                    return result;
                })
                .toList();
    }
}
