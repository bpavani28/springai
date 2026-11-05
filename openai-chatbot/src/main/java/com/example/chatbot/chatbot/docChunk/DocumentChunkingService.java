package com.example.chatbot.docChunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

@Service
public class DocumentChunkingService {
    private final Path folder = Paths.get("documents");

    public List<Document> splitDocument(String fileName, String extractedText) {
        Document document = new Document(extractedText,
                Map.of("source", fileName));

        TokenTextSplitter splitter = new TokenTextSplitter();

        List<Document> chunks = splitter.apply(List.of(document));

        return chunks;
    }



}
