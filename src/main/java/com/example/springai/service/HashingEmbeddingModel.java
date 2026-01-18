package com.example.springai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

public class HashingEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSIONS = 384;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        int index = 0;
        for (String text : request.getInstructions()) {
            embeddings.add(new Embedding(hashToVector(text), index++));
        }
        return new EmbeddingResponse(embeddings);
    }

    private float[] hashToVector(String text) {
        float[] vector = new float[DIMENSIONS];
        byte[] digest = sha256(text);
        for (int i = 0; i < digest.length; i++) {
            int idx = Math.abs(digest[i]) % DIMENSIONS;
            vector[idx] += (digest[i] / 255.0f);
        }
        return normalize(vector);
    }

    private byte[] sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(text.toLowerCase().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private float[] normalize(float[] vector) {
        float sum = 0f;
        for (float value : vector) {
            sum += value * value;
        }
        double magnitude = Math.sqrt(sum);
        if (magnitude == 0) {
            return vector;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / magnitude);
        }
        return vector;
    }
}
