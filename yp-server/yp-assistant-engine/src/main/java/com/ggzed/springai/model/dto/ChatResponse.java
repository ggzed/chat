package com.ggzed.springai.model.dto;

/**
 * 统一对话响应体：与 yp-workflow-engine 保持同一结构
 *
 * @param sessionId 会话 ID
 * @param content   模型回复内容
 */
public record ChatResponse(String sessionId, String content) {
}
