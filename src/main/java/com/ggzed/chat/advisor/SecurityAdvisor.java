package com.ggzed.chat.advisor;

import com.ggzed.chat.common.exception.ApprovalRequiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

/**
 * 工具调用安全拦截器。
 * <p>
 * 在模型返回 toolCalls 之后、实际执行工具之前进行白名单校验，
 * 拦截到高危工具时抛出 {@link ApprovalRequiredException} 中断执行。
 */
@Slf4j
@Component
public class SecurityAdvisor implements CallAdvisor {

    /** 需要人工审批的高危工具白名单 */
    private static final java.util.Set<String> HIGH_RISK_TOOLS = java.util.Set.of("transferMoney");

    @Override
    public int getOrder() {
        // 必须大于 ToolCallingAdvisor 的默认 Order，确保在工具执行前拦截
        return HIGHEST_PRECEDENCE + 400;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);

        if (response.chatResponse() == null) {
            return response;
        }

        for (Generation generation : response.chatResponse().getResults()) {
            if (generation.getOutput() == null || generation.getOutput().getToolCalls() == null) {
                continue;
            }
            for (var toolCall : generation.getOutput().getToolCalls()) {
                String toolName = toolCall.name();
                if (HIGH_RISK_TOOLS.contains(toolName)) {
                    log.error("[SecurityAdvisor] 拦截到高危工具！工具名: {}, 参数: {}", toolName, toolCall.arguments());
                    throw new ApprovalRequiredException(
                            "该操作涉及大额资金变动，请转人工客服或通过手机银行进行人脸识别审批。");
                }
            }
        }
        return response;
    }

    @Override
    public String getName() {
        return "SecurityAdvisor";
    }
}
