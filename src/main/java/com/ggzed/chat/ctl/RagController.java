package com.ggzed.chat.ctl;

import com.ggzed.chat.common.result.Result;
import com.ggzed.chat.model.request.RagRequest;
import com.ggzed.chat.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 增强检索 API
 */
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    /**
     * RAG 流式问答
     */
    @PostMapping(value = "/chat_stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody @Valid RagRequest request) {
        return ragService.chatStream(request.getQuery());
    }

    /**
     * RAG 非流式问答
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody @Valid RagRequest request) {
        return Result.success(ragService.chat(request.getQuery()));
    }

    /**
     * 初始化向量库（读取 PDF -> 切分 -> 向量化 -> 持久化）
     */
    @PostMapping("/init")
    public Result<String> init() {
        int count = ragService.initVectorStore();
        if (count == 0) {
            return Result.success("向量库已经初始化过了，无需重复加载。");
        }
        return Result.success("向量库初始化成功，共存入 " + count + " 个片段。");
    }

    /**
     * 语义检索
     */
    @PostMapping("/search")
    public Result<List<Document>> search(@RequestParam String query) {
        List<Document> results = ragService.search(query);
        return Result.success(results);
    }
}
