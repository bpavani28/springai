package com.example.chatbot.docChunk;

import jakarta.annotation.PostConstruct;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DocumentIngestionService {

    private final DocumentChunkingService chunkingService;
    private final VectorStore vectorStore;

    public DocumentIngestionService(DocumentChunkingService chunkingService, VectorStore vectorStore) {
        this.chunkingService = chunkingService;
        this.vectorStore = vectorStore;
    }


    public void processDocuments() throws Exception {
        List<Document> allChunks = new ArrayList<>();
        Path documentsPath = Paths.get("documents");
        try (Stream<Path> files = Files.list(documentsPath)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {

                String fileName = file.getFileName().toString();
                String extractedText = new Tika().parseToString(file);

                List<Document> chunks = chunkingService.splitDocument(fileName, extractedText);

                allChunks.addAll(chunks);
                System.out.println(fileName + " -> " + chunks.size() + " chunks");
            }
        }
        System.out.println("Total chunks: " + allChunks.size());
        vectorStore.add(allChunks);
    }

}
