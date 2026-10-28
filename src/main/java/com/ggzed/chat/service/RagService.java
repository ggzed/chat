package com.ggzed.chat.service;

import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 增强检索业务接口
 */
public interface RagService {

    /**
     * 初始化向量库：读取 PDF -> 切分 -> 向量化 -> 持久化
     *
     * @return 切分后的片段数量
     */
    int initVectorStore();

    /**
     * 语义检索
     *
     * @param query 用户提问
     * @return 匹配的文档片段
     */
    List<Document> search(String query);

    /**
     * RAG 流式问答
     *
     * @param query 用户提问
     * @return 流式响应
     */
    Flux<String> chatStream(String query);

    /**
     * RAG 非流式问答
     *
     * @param query 用户提问
     * @return 回答内容
     */
    String chat(String query);

    /**
     * 读取 PDF 并切分（仅用于预览切分效果）
     *
     * @return 切分后的文档片段
     */
    List<Document> readAndSplit();
}
