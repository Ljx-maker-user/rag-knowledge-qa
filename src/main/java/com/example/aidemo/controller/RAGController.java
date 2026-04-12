package com.example.aidemo.controller;

import com.example.aidemo.entity.ChatRequest;
import com.example.aidemo.entity.ChatResponse;
import com.example.aidemo.service.AiChatService;
import com.example.aidemo.service.RAGService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RAGController {

    private final RAGService ragService;
    private final AiChatService aiChatService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            ragService.uploadDocument(file);
            return ResponseEntity.ok("文档上传成功");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> ragChat(@RequestBody RAGChatRequest request) {
        // 1. 检索相关文档
        List<String> relevantDocs = ragService.search(request.getQuestion(), 3);

        // 2. 构建增强 Prompt
        String context = String.join("\n\n", relevantDocs);
        String prompt = buildPrompt(context, request.getQuestion());

        // 3. 调用 AiChatService 的 chat 方法
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage(prompt);
        ChatResponse response = aiChatService.chat(chatRequest);

        return ResponseEntity.ok(response.getAnswer());
    }

    private String buildPrompt(String context, String question) {
        return """
                你是一个知识库助手，请根据以下资料回答问题。
                如果资料中没有相关信息，请如实说"资料中未找到相关内容"。

                资料：
                %s

                问题：%s
                回答：
                """.formatted(context, question);
    }

    @Data
    static class RAGChatRequest {
        private String question;
    }
}