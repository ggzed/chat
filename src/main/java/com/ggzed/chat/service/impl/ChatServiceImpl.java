package com.ggzed.chat.service.impl;

import com.ggzed.chat.service.ChatService;
import com.ggzed.chat.tools.InventoryTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final InventoryTools inventoryTools;

    // 手动写构造器，精准指定要注入哪个 Bean
    public ChatServiceImpl(
            @Qualifier("deepSeekChatClient") ChatClient chatClient,
            InventoryTools inventoryTools) {
        this.chatClient = chatClient;
        this.inventoryTools = inventoryTools;
    }


    @Override
    public String chat(String message) {
        return chatClient.prompt()
                .system("你是一个专业的银行风控专家...")
                .user(message)
                .tools(inventoryTools) // 这里挂载工具
                .call()
                .content();
    }

    @Override
    public Flux<String> streamChat(String message) {
        return chatClient.prompt()
                .system("你是一个 Java 架构师。")
                .user(message)
                .stream()
                .content();
    }
}