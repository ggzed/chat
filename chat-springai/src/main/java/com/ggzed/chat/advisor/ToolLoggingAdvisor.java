package com.ggzed.chat.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.model.Generation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolLoggingAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(ToolLoggingAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 打印发出的完整请求（可选）
        log.info(">>>> [Advisor] 发送请求到模型, 携带消息数: {}", chatClientRequest.prompt().getInstructions().size());

        // 放行，执行实际调用
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

        // 1. 强制打印完整的 ChatClientResponse，不管里面是什么，这是最直观的
        log.info("<<<< [Advisor] 模型原始响应 (整体): {}", chatClientResponse);

        if (chatClientResponse != null && chatClientResponse.chatResponse() != null) {
            // 2. 打印 ChatResponse 的内容
            log.info("<<<< [Advisor] 模型原始响应 (ChatResponse): {}", chatClientResponse.chatResponse());

            for (Generation generation : chatClientResponse.chatResponse().getResults()) {
                if (generation.getOutput() != null) {
                    // 检查 toolCalls
                    if (generation.getOutput().getToolCalls() != null && !generation.getOutput().getToolCalls().isEmpty()) {
                        log.info("<<<< [Advisor] 拦截到模型工具调用请求: {}", generation.getOutput().getToolCalls());
                    } else {
                        log.info("<<<< [Advisor] 模型返回纯文本: {}", generation.getOutput().getText());
                    }
                }
            }
        } else {
            log.warn("<<<< [Advisor] chatResponse() 为 null，可能被 Spring AI 内部拦截处理了");
        }

        return chatClientResponse;
    }

    @Override
    public String getName() {
        return "ToolLoggingAdvisor";
    }

    @Override
    public int getOrder() {
        // 设得小一点，确保在 ToolCallingAdvisor (默认 order 通常是 0) 之前执行
        return 100;
    }
}