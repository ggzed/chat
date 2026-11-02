package com.ggzed.chat.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ggzed.chat.service.HybridSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 混合检索实现：BM25 + KNN 双路召回，应用层 RRF 融合。
 * <p>
 * 走应用层融合而非服务端 rank.rrf，便于调试每路得分，
 * 且对 ES 8.13 / 9.x 服务端语法均兼容。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HybridSearchServiceImpl implements HybridSearchService {

    /** 目标索引名（与 spring.ai.vectorstore.elasticsearch.index-name 保持一致） */
    private static final String INDEX_NAME = "financial_rag";
    /** 文本字段 */
    private static final String FIELD_CONTENT = "content";
    /** 向量字段 */
    private static final String FIELD_EMBEDDING = "embedding";
    /** RRF 平滑常数，通常取 60 */
    private static final int RRF_K = 60;
    /** 两路各自召回的数量 */
    private static final int RECALL_SIZE = 20;
    /** KNN 候选集大小 */
    private static final int NUM_CANDIDATES = 100;
    /** 最终返回条数 */
    private static final int DEFAULT_TOP_N = 5;

    private final ElasticsearchClient esClient;
    private final EmbeddingModel embeddingModel;

    @Override
    public List<String> hybridSearch(String query, int topN) throws IOException {
        // 1. BM25 关键词召回
        SearchResponse<Map<String, Object>> bm25Response = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .size(RECALL_SIZE)
                        .query(q -> q.match(m -> m.field(FIELD_CONTENT).query(query))),
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        // 2. KNN 向量召回
        float[] queryVector = embeddingModel.embed(query);
        SearchResponse<Map<String, Object>> knnResponse = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .size(RECALL_SIZE)
                        .knn(k -> k
                                .field(FIELD_EMBEDDING)
                                .queryVector(convertToFloatList(queryVector))
                                .k(RECALL_SIZE)
                                .numCandidates(NUM_CANDIDATES)
                        ),
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        // 3. 应用层 RRF 融合：score = Σ 1 / (RRF_K + rank)
        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, Map<String, Object>> docSources = new HashMap<>();
        accumulateRrf(bm25Response.hits().hits(), rrfScores, docSources);
        accumulateRrf(knnResponse.hits().hits(), rrfScores, docSources);

        // 4. 按 RRF 总分降序取 TopN，提取文档内容
        List<String> results = rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> {
                    Map<String, Object> source = docSources.get(entry.getKey());
                    Object content = source != null ? source.get(FIELD_CONTENT) : null;
                    return content != null ? content.toString() : "";
                })
                .filter(content -> !content.isEmpty())
                .collect(Collectors.toList());

        log.info("混合检索: query='{}', BM25 命中 {}, KNN 命中 {}, 融合后返回 {}",
                query, bm25Response.hits().hits().size(), knnResponse.hits().hits().size(), results.size());
        return results;
    }

    /** 将单路召回结果按排名累加 RRF 分数，并缓存文档内容 */
    private void accumulateRrf(List<Hit<Map<String, Object>>> hits,
                               Map<String, Double> rrfScores,
                               Map<String, Map<String, Object>> docSources) {
        for (int i = 0; i < hits.size(); i++) {
            Hit<Map<String, Object>> hit = hits.get(i);
            // RRF 公式: 1 / (k + rank)，rank 从 1 开始
            double score = 1.0 / (RRF_K + (i + 1));
            rrfScores.merge(hit.id(), score, Double::sum);
            docSources.putIfAbsent(hit.id(), hit.source());
        }
    }

    private List<Float> convertToFloatList(float[] floats) {
        List<Float> list = new ArrayList<>(floats.length);
        for (float f : floats) {
            list.add(f);
        }
        return list;
    }
}
