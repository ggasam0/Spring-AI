package com.example.springai.controller;

import com.example.springai.service.ChatResult;
import com.example.springai.service.ChatService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@Validated
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String sessionId = request.sessionId();
        ChatResult result = chatService.chat(sessionId, request.message());
        return new ChatResponse(sessionId, result.answer(), result.contexts(), result.tool());
    }

    @PostMapping("/ask")
    public ChatResponse ask(@RequestBody ChatRequest request) {
        String sessionId = request.sessionId();
        ChatResult result = chatService.ask(sessionId, request.message());
        return new ChatResponse(sessionId, result.answer(), result.contexts(), result.tool());
    }

    @PostMapping("/assist")
    public ChatResponse assist(@RequestBody ChatRequest request) {
        String sessionId = request.sessionId();
        ChatResult result = chatService.assist(sessionId, request.message());
        return new ChatResponse(sessionId, result.answer(), result.contexts(), result.tool());
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@RequestBody ChatRequest request) {
        return chatService.stream(request.sessionId(), request.message())
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    public record ChatRequest(@NotBlank String sessionId, @NotBlank String message) {
    }

    public record ChatResponse(String sessionId, String answer, List<String> contexts, String toolUsed) {
    }
}
