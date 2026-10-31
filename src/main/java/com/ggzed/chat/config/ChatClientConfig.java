package com.ggzed.chat.config;

import com.ggzed.chat.advisor.SecurityAdvisor;
import com.ggzed.chat.advisor.TokenUsageAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;

/**
 * ChatClient 配置
 * <p>
 * DeepSeek 用于对话，DashScope 仅用于 Embedding（向量计算）。
 * 这里集中管理基础客户端与各业务专线客户端，专线在基础能力上按需挂载 Advisor。
 */
@Configuration
public class ChatClientConfig {

    /**
     * 会话记忆拦截器：按 sessionId 读写历史消息，数据落在 H2，跨重启保留。
     * <p>
     * 必须最先执行，先把历史消息拼进 Prompt，后续检索类 Advisor 才能基于最新一句用户提问做召回。
     */
    @Bean
    public SessionMemoryAdvisor sessionMemoryAdvisor(SessionService sessionService) {
        return SessionMemoryAdvisor.builder(sessionService)
                .order(Ordered.HIGHEST_PRECEDENCE + 100)
                .build();
    }

    /**
     * 基础客户端：作为 {@link Primary} 默认注入，只挂载会话记忆，
     * 由业务层在 mutate 后自行补充拦截器（如 RAG 检索）。
     */
    @Bean
    @Primary
    public ChatClient chatClient(@Qualifier("deepSeekChatModel") ChatModel chatModel,
                                 SessionMemoryAdvisor sessionMemoryAdvisor) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(sessionMemoryAdvisor)
                .build();
    }

    /** 纯聊天专线（带 Token 统计） */
    @Bean("basicChatClient")
    public ChatClient basicChatClient(@Qualifier("deepSeekChatModel") ChatModel model, TokenUsageAdvisor usage) {
        return ChatClient.builder(model)
                .defaultAdvisors(usage)
                .build();
    }

    /** RAG 专线（带知识库检索） */
    @Bean("ragChatClient")
    public ChatClient ragChatClient(@Qualifier("deepSeekChatModel") ChatModel model,
                                    VectorStore store,
                                    TokenUsageAdvisor usage) {
        return ChatClient.builder(model)
                .defaultAdvisors(usage, QuestionAnswerAdvisor.builder(store).build())
                .build();
    }

    /** 风控专线（带高危工具拦截） */
    @Bean("riskChatClient")
    public ChatClient riskChatClient(@Qualifier("deepSeekChatModel") ChatModel model,
                                     TokenUsageAdvisor usage,
                                     SecurityAdvisor security) {
        return ChatClient.builder(model)
                .defaultAdvisors(usage, security)
                .build();
    }
}
