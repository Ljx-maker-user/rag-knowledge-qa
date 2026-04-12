package com.example.aidemo.mapper;

import com.example.aidemo.entity.ChatHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ChatHistoryMapper {

    @Insert("INSERT INTO chat_history(user_input, ai_response, create_time) " +
            "VALUES(#{userInput}, #{aiResponse}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(ChatHistory history);

    @Select("SELECT * FROM chat_history ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatHistory> findLatest(@Param("limit") int limit);
}