package com.example.springai.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

@Component
public class FaqStoreInitializer implements ApplicationRunner {

    private final VectorStore vectorStore;
    private final Resource faqResource;

    public FaqStoreInitializer(VectorStore vectorStore, @Value("classpath:faq.md") Resource faqResource) {
        this.vectorStore = vectorStore;
        this.faqResource = faqResource;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        String content = new String(FileCopyUtils.copyToByteArray(faqResource.getInputStream()), StandardCharsets.UTF_8);
        List<Document> documents = new ArrayList<>();
        for (String entry : splitEntries(content)) {
            if (!entry.isBlank()) {
                documents.add(new Document(entry.strip(), Map.of("source", "faq")));
            }
        }
        vectorStore.add(documents);
    }

    private List<String> splitEntries(String content) {
        List<String> entries = new ArrayList<>();
        String[] sections = content.split("\\n---\\n");
        for (String section : sections) {
            entries.add(section.trim());
        }
        return entries;
    }
}
