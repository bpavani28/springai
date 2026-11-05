package com.example.chatbot.documentchatbot;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private final Path folder = Paths.get("documents");
    private final Tika tika = new Tika();


    public String readDocuments() {
        if (!Files.exists(folder)) {
            return "";
        }

        try (var files = Files.list(folder)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(this::isSupported)
                    .map(this::read)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("\n\n"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read documents folder", e);
        }
    }

    private boolean isSupported(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".txt")
                || name.endsWith(".pdf")
                || name.endsWith(".docx");
    }

    private String read(Path path) {
        try {
            System.out.println(path.getFileName());
            String text = tika.parseToString(path.toFile());

            return "DOCUMENT: " + path.getFileName() + "\n\n" + text;

        } catch (Exception e) {
            System.out.println("Failed to parse file: " + path);
            e.printStackTrace();

            return "";
        }
    }
}
