package com.example.springai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final ChatMemoryStore memoryStore;
    private final ToolRouter toolRouter;
    private final int topK;

    public ChatService(ChatModel chatModel,
                       VectorStore vectorStore,
                       ChatMemoryStore memoryStore,
                       ToolRouter toolRouter,
                       @Value("${app.rag.top-k:4}") int topK) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.memoryStore = memoryStore;
        this.toolRouter = toolRouter;
        this.topK = topK;
    }

    public ChatResult chat(String sessionId, String message) {
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(systemMessage());
        promptMessages.addAll(memoryStore.getHistory(sessionId));
        promptMessages.add(new UserMessage(message));

        ChatResponse response = chatModel.call(new Prompt(promptMessages));
        String content = response.getResult().getOutput().getContent();

        memoryStore.append(sessionId, new UserMessage(message));
        memoryStore.append(sessionId, new AssistantMessage(content));

        return new ChatResult(content, List.of(), null);
    }

    public ChatResult ask(String sessionId, String question) {
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage("你是企业 FAQ 助手，只能依据资料回答。如果资料没有答案，说明未覆盖。"));
        promptMessages.addAll(memoryStore.getHistory(sessionId));

        List<String> contexts = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(topK))
                .stream()
                .map(doc -> doc.getContent().trim())
                .collect(Collectors.toList());

        String contextBlock = contexts.isEmpty()
                ? ""
                : contexts.stream().map(text -> "- " + text).collect(Collectors.joining("\n"));

        promptMessages.add(new UserMessage("问题: " + question + "\n\n资料:\n" + contextBlock));
        ChatResponse response = chatModel.call(new Prompt(promptMessages));
        String content = response.getResult().getOutput().getContent();

        memoryStore.append(sessionId, new UserMessage(question));
        memoryStore.append(sessionId, new AssistantMessage(content));

        return new ChatResult(content, contexts, null);
    }

    public ChatResult assist(String sessionId, String message) {
        ToolDecision decision = toolRouter.route(sessionId, message, memoryStore.getHistory(sessionId));
        if ("none".equalsIgnoreCase(decision.tool())) {
            memoryStore.appendAll(sessionId, decision.historyUpdates());
            return new ChatResult(decision.answer(), List.of(), "none");
        }
        memoryStore.appendAll(sessionId, decision.historyUpdates());
        return new ChatResult(decision.answer(), List.of(), decision.tool());
    }

    public Flux<String> stream(String sessionId, String message) {
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(systemMessage());
        promptMessages.addAll(memoryStore.getHistory(sessionId));
        promptMessages.add(new UserMessage(message));

        return chatModel.stream(new Prompt(promptMessages))
                .map(response -> response.getResult().getOutput().getContent())
                .doOnComplete(() -> {
                    memoryStore.append(sessionId, new UserMessage(message));
                })
                .doOnNext(chunk -> {
                    if (!chunk.isBlank()) {
                        memoryStore.append(sessionId, new AssistantMessage(chunk));
                    }
                });
    }

    private SystemMessage systemMessage() {
        return new SystemMessage("你是 Spring AI 项目的助手，回答简洁清楚。");
    }
}
