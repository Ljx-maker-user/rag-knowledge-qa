package com.example.aidemo.service;

import com.example.aidemo.entity.ChatHistory;
import com.example.aidemo.entity.ChatRequest;
import com.example.aidemo.entity.ChatResponse;
import com.example.aidemo.mapper.ChatHistoryMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final ChatHistoryMapper historyMapper;

    public ChatResponse chat(ChatRequest request) {
        String userMessage = request.getMessage();
        log.info("收到对话请求: {}", userMessage);

        String aiResponse = chatLanguageModel.generate(userMessage);

        saveHistory(userMessage, aiResponse);
        return new ChatResponse(aiResponse);
    }

    public List<ChatHistory> getHistory(int limit) {
        return historyMapper.findLatest(limit);
    }

    private void saveHistory(String userInput, String aiResponse) {
        ChatHistory history = new ChatHistory();
        history.setUserInput(userInput);
        history.setAiResponse(aiResponse);
        historyMapper.save(history);
        log.info("对话记录已保存，ID: {}", history.getId());
    }
    public String chat(String message) {
        log.info("收到对话请求: {}", message);
        String aiResponse = chatLanguageModel.generate(message);
        saveHistory(message, aiResponse);
        return aiResponse;
    }
}