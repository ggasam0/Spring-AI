package com.example.springai.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.Message;

public record ToolDecision(String tool, String answer, Map<String, String> arguments, List<Message> historyUpdates) {

    public ToolDecision(String tool, String answer, Map<String, String> arguments) {
        this(tool, answer, arguments, List.of());
    }

    public ToolDecision(String tool, String answer, List<Message> historyUpdates) {
        this(tool, answer, Map.of(), historyUpdates);
    }
}
