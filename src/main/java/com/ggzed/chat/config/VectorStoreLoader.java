package com.ggzed.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class VectorStoreLoader {
    private final VectorStore vectorStore;

    @EventListener(ApplicationReadyEvent.class)
    public void loadVectorStore() {
        File file = new File("vector-store.json");
        if (file.exists()) {
            // 如果本地文件存在，直接加载进内存，再也不调用阿里云 API 了
            ((SimpleVectorStore) vectorStore).load(file);
            System.out.println("✅ 成功从本地文件加载向量库，跳过重复计算！");
        } else {
            System.out.println("⚠️ 未找到本地向量文件，请访问 /api/rag/init 进行初始化。");
        }
    }
}
