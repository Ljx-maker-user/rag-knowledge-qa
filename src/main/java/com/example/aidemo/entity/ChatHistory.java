package com.example.aidemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistory {
    private Long id;
    private String userInput;
    private String aiResponse;
    private LocalDateTime createTime;
}