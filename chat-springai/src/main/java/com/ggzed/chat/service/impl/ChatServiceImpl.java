package com.ggzed.chat.service.impl;

import com.ggzed.chat.service.ChatService;
import com.ggzed.chat.tools.InventoryTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /** 非流式对话人设 */
    private static final String SYSTEM_PROMPT = "你是一个专业的银行风控专家...";
    /** 流式对话人设 */
    private static final String STREAM_SYSTEM_PROMPT = "你是一个 Java 架构师。";

    private final ChatClient chatClient;
    private final InventoryTools inventoryTools;

    @Override
    public String chat(String message, String sessionId) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .tools(inventoryTools) // 这里挂载工具
                .advisors(a -> a.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, sessionId))
                .call()
                .content();
    }

    @Override
    public Flux<String> streamChat(String message, String sessionId) {
        return chatClient.prompt()
                .system(STREAM_SYSTEM_PROMPT)
                .user(message)
                .advisors(a -> a.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, sessionId))
                .stream()
                .content();
    }
}
