package com.reactivespring.dto;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomMessage implements ChatMessage {
    
    private String content;
    private ChatMessageType type;
    private String role;
    
    @Override
    public String text() {
        return content;
    }
    
    @Override
    public ChatMessageType type() {
        return type != null ? type : ChatMessageType.USER;
    }
    
    public static CustomMessage userMessage(String content) {
        return CustomMessage.builder()
                .content(content)
                .type(ChatMessageType.USER)
                .role("user")
                .build();
    }
    
    public static CustomMessage systemMessage(String content) {
        return CustomMessage.builder()
                .content(content)
                .type(ChatMessageType.SYSTEM)
                .role("system")
                .build();
    }
    
    public static CustomMessage assistantMessage(String content) {
        return CustomMessage.builder()
                .content(content)
                .type(ChatMessageType.AI)
                .role("assistant")
                .build();
    }
}