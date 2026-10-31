package com.ggzed.chat.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Token 使用记录（不可变 DTO）
 */
public record TokenUsageRecord(
        String userId,
        String modelName,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        BigDecimal estimatedCost, // 金额用 BigDecimal 避免精度丢失
        String requestType,       // "CALL" 或 "STREAM"
        long latencyMs,           // 耗时(毫秒)
        LocalDateTime createTime) {
}
