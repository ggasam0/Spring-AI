package com.example.springai.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

@Component
public class ChatMemoryStore {

    private final Map<String, List<Message>> memory = new ConcurrentHashMap<>();

    public List<Message> getHistory(String sessionId) {
        return memory.getOrDefault(sessionId, Collections.emptyList());
    }

    public void append(String sessionId, Message message) {
        memory.compute(sessionId, (key, existing) -> {
            List<Message> messages = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            messages.add(message);
            return messages;
        });
    }

    public void appendAll(String sessionId, List<Message> messages) {
        memory.compute(sessionId, (key, existing) -> {
            List<Message> updated = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            updated.addAll(messages);
            return updated;
        });
    }
}
