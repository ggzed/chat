package com.ggzed.chat.advisor;

import com.ggzed.chat.service.HybridSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 混合检索拦截器：在请求发给大模型之前，用最新一句用户提问召回资料并注入上下文。
 * <p>
 * 必须晚于会话记忆拦截器执行，否则历史消息会被改写。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HybridSearchAdvisor implements BaseAdvisor {

    /** 最终注入上下文的片段数量 */
    private static final int TOP_N = 5;

    private final HybridSearchService hybridSearchService;

    @Override
    public String getName() {
        return "HybridSearchAdvisor";
    }

    @Override
    public int getOrder() {
        // 放在会话记忆（HIGHEST_PRECEDENCE + 100）之后，保证检索基于用户最新一句话，同时保留历史消息
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        List<Message> instructions = request.prompt().getInstructions();
        int lastUserIndex = lastUserMessageIndex(instructions);
        if (lastUserIndex < 0) {
            return request;
        }

        String userQuery = instructions.get(lastUserIndex).getText();
        if (userQuery == null || userQuery.isBlank()) {
            return request;
        }

        try {
            List<String> results = hybridSearchService.hybridSearch(userQuery, TOP_N);
            if (results.isEmpty()) {
                log.warn("[HybridSearchAdvisor] 混合检索未找到相关资料，直接放行");
                return request;
            }

            String context = results.stream().collect(Collectors.joining("\n\n---\n\n"));
            String augmentedPrompt = "请根据以下参考资料回答用户问题。如果参考资料中没有答案，请直接回答'我不知道'，严禁编造。\n\n" +
                    "【参考资料】:\n" + context + "\n\n" +
                    "【用户问题】: " + userQuery;

            log.info("[HybridSearchAdvisor] 检索到 {} 个片段，已注入上下文", results.size());

            // 只替换最后一条用户消息，保留系统提示词与历史对话
            List<Message> newMessages = new ArrayList<>(instructions);
            newMessages.set(lastUserIndex, new UserMessage(augmentedPrompt));
            Prompt newPrompt = request.prompt().mutate().messages(newMessages).build();

            return request.mutate().prompt(newPrompt).build();
        } catch (Exception e) {
            log.error("[HybridSearchAdvisor] 混合检索发生异常，降级放行", e);
            // 兜底策略：如果 ES 挂了，不要影响用户聊天，直接放行原请求
            return request;
        }
    }

    /** 找到最后一条用户消息的下标，未找到返回 -1 */
    private int lastUserMessageIndex(List<Message> instructions) {
        for (int i = instructions.size() - 1; i >= 0; i--) {
            if (instructions.get(i) instanceof UserMessage) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        // 检索是在 before 做的，after 不需要做额外处理
        return response;
    }
}
