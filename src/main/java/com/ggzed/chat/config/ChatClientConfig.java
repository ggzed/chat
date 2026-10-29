package com.ggzed.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ChatClient 配置
 * <p>
 * DeepSeek 用于对话，DashScope 仅用于 Embedding（向量计算），
 * 因此只需要一个基于 DeepSeek 的 ChatClient。
 */
@Configuration
public class ChatClientConfig {

    @Bean
    @Primary
    public ChatClient chatClient(@Qualifier("deepSeekChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
