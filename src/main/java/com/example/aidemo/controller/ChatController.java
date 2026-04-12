package com.example.aidemo.controller;

import com.example.aidemo.entity.ChatHistory;
import com.example.aidemo.entity.ChatRequest;
import com.example.aidemo.entity.ChatResponse;
import com.example.aidemo.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final AiChatService aiService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistory>> history(@RequestParam(defaultValue = "10") int limit) {
        List<ChatHistory> history = aiService.getHistory(limit);
        return ResponseEntity.ok(history);
    }
}