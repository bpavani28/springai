package com.example.chatbot;

import com.example.chatbot.docChunk.DocumentIngestionService;
import org.springframework.ai.document.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentStartupLoader implements ApplicationRunner {

    private final DocumentIngestionService ingestionService;


    public DocumentStartupLoader(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
       ingestionService.processDocuments();
    }
}
