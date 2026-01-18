package com.example.springai.config;

import com.example.springai.service.HashingEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

@Configuration
public class AiConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new HashingEmbeddingModel();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }
}
