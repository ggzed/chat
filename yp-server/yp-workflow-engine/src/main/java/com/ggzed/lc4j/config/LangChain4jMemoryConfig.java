package com.ggzed.lc4j.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jMemoryConfig {

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        // 返回一个 lambda 表达式：为每个传入的 memoryId 创建一个独立的记忆实例
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)          // 绑定该记忆所属的 memoryId
                .maxMessages(10)       // 每个用户最多保留 10 条消息
                .build();
    }
}