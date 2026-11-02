package com.ggzed.chat.advisor;

import com.ggzed.chat.model.dto.TokenUsageRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Token 用量统计 Advisor。
 * <p>
 * 记录每次模型调用的 Token 消耗与预估费用，用于成本核算。
 * Advisor 是单例 Bean，耗时不能存放在成员变量中，需通过请求上下文传递。
 */
@Slf4j
@Component
public class TokenUsageAdvisor implements BaseAdvisor {

    /** 请求开始时间在上下文中的 key */
    private static final String START_TIME_KEY = "tokenUsageStartTime";
    /** 模型定价（以 DeepSeek 为例，单位：元/千 Token） */
    private static final double PROMPT_PRICE_PER_1K = 0.001;
    private static final double COMPLETION_PRICE_PER_1K = 0.002;
    /** 费用保留小数位 */
    private static final int COST_SCALE = 6;

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        return request.mutate()
                .context(START_TIME_KEY, System.currentTimeMillis())
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        if (response == null || response.chatResponse() == null
                || response.chatResponse().getMetadata() == null) {
            return response;
        }

        Usage usage = response.chatResponse().getMetadata().getUsage();
        if (usage == null) {
            return response;
        }

        int promptTokens = Objects.requireNonNullElse(usage.getPromptTokens(), 0);
        int completionTokens = Objects.requireNonNullElse(usage.getCompletionTokens(), 0);
        int totalTokens = Objects.requireNonNullElse(usage.getTotalTokens(), 0);
        // 流式场景下，若协议未返回 usage，这些值可能为 0
        if (totalTokens == 0) {
            log.warn("[TokenUsageAdvisor] Token 用量为 0，当前模型/协议未返回 usage");
            return response;
        }

        double cost = promptTokens / 1000.0 * PROMPT_PRICE_PER_1K
                + completionTokens / 1000.0 * COMPLETION_PRICE_PER_1K;

        TokenUsageRecord record = new TokenUsageRecord(
                getCurrentUserId(),
                response.chatResponse().getMetadata().getModel(),
                promptTokens,
                completionTokens,
                totalTokens,
                BigDecimal.valueOf(cost).setScale(COST_SCALE, RoundingMode.HALF_UP),
                "CALL",
                calculateLatency(response),
                LocalDateTime.now());

        log.info("[TokenUsageAdvisor] user={}, model={}, prompt={}, completion={}, cost={}元",
                record.userId(), record.modelName(), promptTokens, completionTokens,
                String.format("%.4f", cost));
        return response;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 100;
    }

    private String getCurrentUserId() {
        // 后续可从 ThreadLocal / SecurityContext 中获取真实用户
        return "anonymous";
    }

    private long calculateLatency(ChatClientResponse response) {
        Object startTime = response.context().get(START_TIME_KEY);
        return startTime instanceof Long start ? System.currentTimeMillis() - start : 0L;
    }
}
