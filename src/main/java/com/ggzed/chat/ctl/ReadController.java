package com.ggzed.chat.ctl;

import com.ggzed.chat.common.result.Result;
import com.ggzed.chat.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文档读取与切分 API（预览切分效果）
 */
@Slf4j
@RestController
@RequestMapping("/api/read")
@RequiredArgsConstructor
public class ReadController {

    private final RagService ragService;

    /**
     * 读取 PDF 并切分，返回切分后的片段信息
     */
    @PostMapping("/simple")
    public Result<String> simple() {
        List<Document> splitDocuments = ragService.readAndSplit();

        // 打印前 3 个切片看看效果
        for (int i = 0; i < Math.min(3, splitDocuments.size()); i++) {
            log.info("\n--- 切片 {} ---\n{}", i + 1, splitDocuments.get(i).getFormattedContent());
        }

        return Result.success("PDF 切分成功，切分后碎片数: " + splitDocuments.size());
    }
}
