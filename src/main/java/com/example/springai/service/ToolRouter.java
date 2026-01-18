package com.example.springai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class ToolRouter {

    private final ChatModel chatModel;
    private final ToolService toolService;
    private final ObjectMapper objectMapper;

    public ToolRouter(ChatModel chatModel, ToolService toolService, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.toolService = toolService;
        this.objectMapper = objectMapper;
    }

    public ToolDecision route(String sessionId, String message, List<Message> history) {
        List<Message> toolPrompt = new ArrayList<>();
        toolPrompt.add(new SystemMessage(toolSystemPrompt()));
        toolPrompt.addAll(history);
        toolPrompt.add(new UserMessage(message));

        ChatResponse decisionResponse = chatModel.call(new Prompt(toolPrompt));
        String decisionPayload = decisionResponse.getResult().getOutput().getContent();

        ToolDecision decision = parseDecision(decisionPayload, message);
        List<Message> updates = new ArrayList<>();
        updates.add(new UserMessage(message));

        if ("none".equalsIgnoreCase(decision.tool())) {
            updates.add(new AssistantMessage(decision.answer()));
            return new ToolDecision(decision.tool(), decision.answer(), updates);
        }

        String toolResult = toolService.execute(decision.tool(), decision.arguments());
        String finalAnswer = generateToolAnswer(history, message, decision.tool(), toolResult);

        updates.add(new AssistantMessage(finalAnswer));
        return new ToolDecision(decision.tool(), finalAnswer, updates);
    }

    private String generateToolAnswer(List<Message> history, String message, String tool, String toolResult) {
        List<Message> prompt = new ArrayList<>();
        prompt.add(new SystemMessage("你是客服助手，必须基于工具结果回答，并告知关键字段。"));
        prompt.addAll(history);
        prompt.add(new UserMessage("用户问题: " + message + "\n工具: " + tool + "\n结果: " + toolResult));

        ChatResponse response = chatModel.call(new Prompt(prompt));
        return response.getResult().getOutput().getContent();
    }

    private String toolSystemPrompt() {
        return "你是工具路由器。根据用户问题选择是否调用工具。" +
                "\n可用工具: getOrderStatus(orderId), createTicket(issue, contact), getStoreHours(store)" +
                "\n如果需要工具，返回 JSON: {\"tool\":\"工具名\",\"arguments\":{...},\"answer\":\"\"}" +
                "\n如果不需要工具，返回 JSON: {\"tool\":\"none\",\"arguments\":{},\"answer\":\"直接回复\"}";
    }

    private ToolDecision parseDecision(String content, String originalMessage) {
        try {
            ToolSelection selection = objectMapper.readValue(content, ToolSelection.class);
            return new ToolDecision(selection.tool(), selection.answer(), selection.arguments());
        } catch (JsonProcessingException ex) {
            return new ToolDecision("none", content, Map.of());
        }
    }

    private record ToolSelection(String tool, Map<String, String> arguments, String answer) {
    }
}
