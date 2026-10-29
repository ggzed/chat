package com.ggzed.chat.service.impl;

import com.ggzed.chat.advisor.SecurityAdvisor;
import com.ggzed.chat.service.RagService;
import com.ggzed.chat.tools.FinanceTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RAG 业务实现
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {

    /** 系统人设提示词 */
    private static final String SYSTEM_PROMPT = """
            你是一个专业的银行理财经理。你的任务是基于提供的参考资料回答用户问题。
            【铁律】
            1. 遇到任何涉及金额计算（如收益、利息）的问题，绝对禁止你自己心算！必须调用 calculateProfit 工具！
            2. 如果资料中有'业绩比较基准 2.50-2.70%'，请直接把 0.027 作为参数传给工具！
            3. 如果资料里找不到答案，请回答'我不知道'。
            4. 不要输出任何免责声明，不要废话，直接给出计算结果或答案！""";

    /** PDF 文档路径 */
    private static final String PDF_PATH = "doc/xy92.pdf";
    /** 向量库持久化文件 */
    private static final String VECTOR_STORE_FILE = "vector-store.json";

    /** RAG 问答检索阈值 */
    private static final double RAG_SIMILARITY_THRESHOLD = 0.3;
    /** RAG 问答召回数量 */
    private static final int RAG_TOP_K = 6;
    /** 语义检索召回数量 */
    private static final int SEARCH_TOP_K = 3;
    /** 语义检索阈值 */
    private static final double SEARCH_SIMILARITY_THRESHOLD = 0.5;

    private final VectorStore vectorStore;
    private final FinanceTools financeTools;
    private final ChatClient ragChatClient;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public RagServiceImpl(
            VectorStore vectorStore,
            FinanceTools financeTools,
            ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.financeTools = financeTools;
        // 基于原始 chatClient，动态挂载 RAG 拦截器
        this.ragChatClient = chatClient.mutate()
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(RAG_SIMILARITY_THRESHOLD)
                                        .topK(RAG_TOP_K)
                                        .build())
                                .build(),
                        new SecurityAdvisor()
                )
                .build();
    }

    @Override
    public int initVectorStore() {
        // CAS 保证只初始化一次
        if (!initialized.compareAndSet(false, true)) {
            log.info("向量库已经初始化过了，无需重复加载。");
            return 0;
        }

        List<Document> chunks = readAndSplit();
        log.info("开始向量化，共 {} 个片段...", chunks.size());
        vectorStore.add(chunks);
        log.info("向量库初始化完成！");

        // 将内存中的向量库持久化到项目根目录
        File file = new File(VECTOR_STORE_FILE);
        ((SimpleVectorStore) vectorStore).save(file);
        log.info("向量库已持久化到 {}", file.getAbsolutePath());

        return chunks.size();
    }

    @Override
    public List<Document> search(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(SEARCH_TOP_K)
                .similarityThreshold(SEARCH_SIMILARITY_THRESHOLD)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("========== 用户提问: {} ==========", query);
        if (results != null && !results.isEmpty()) {
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                log.info("【匹配片段 {}】 来自页码: {}", i + 1, doc.getMetadata().get("page_number"));
                log.info("{}", doc.getFormattedContent());
                log.info("---------------------------------");
            }
        } else {
            log.info("没有找到相关片段...");
        }
        return results;
    }

    @Override
    public Flux<String> chatStream(String query) {
        return ragChatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(query)
                .tools(financeTools)
                .stream()
                .content();
    }

    @Override
    public String chat(String query) {
        return ragChatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(query)
                .tools(financeTools)
                .call()
                .content();
    }

    @Override
    public List<Document> readAndSplit() {
        ClassPathResource resource = new ClassPathResource(PDF_PATH);
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(2)
                                .withNumberOfBottomTextLinesToDelete(2)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        List<Document> rawDocs = reader.read();
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(rawDocs);

        log.info("========== 切分前页数: {} ==========", rawDocs.size());
        log.info("========== 切分后碎片数: {} ==========", chunks.size());

        return chunks;
    }

    @Override
    public String testSecurity(String query) {
        // 注意：这里不需要 RAG，直接给客户端发指令，逼它调工具
        return ragChatClient.prompt()
                .user(query)
                .tools(financeTools) // 挂载了你那个高危的 buyFinancialProduct
                .call()
                .content();
    }
}
