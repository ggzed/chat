package com.ggzed.lc4j.model.dto;

/**
 * 统一对话响应体，字段与 chat-springai 的 ChatResponse 完全一致
 */
public record ChatResponse(String sessionId, String content) {
}
