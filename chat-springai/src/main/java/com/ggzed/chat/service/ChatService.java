package com.ggzed.chat.service;

import reactor.core.publisher.Flux;

/**
 * 基础对话业务接口
 */
public interface ChatService {

    /**
     * 非流式对话
     *
     * @param message   用户消息
     * @param sessionId 会话 ID，用于多轮记忆隔离
     * @return 回答内容
     */
    String chat(String message, String sessionId);

    /**
     * 流式对话
     *
     * @param message   用户消息
     * @param sessionId 会话 ID，用于多轮记忆隔离
     * @return 流式响应
     */
    Flux<String> streamChat(String message, String sessionId);
}
