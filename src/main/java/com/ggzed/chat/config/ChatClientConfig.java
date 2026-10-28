package com.ggzed.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient deepSeekChatClient(@Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel) {
        return ChatClient.builder(deepSeekChatModel).build();
    }

    @Bean
    public ChatClient dashScopeChatClient(@Qualifier("dashScopeChatModel") ChatModel dashScopeChatModel) {
        return ChatClient.builder(dashScopeChatModel).build();
    }
}