package com.ggzed.chat.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class InventoryTools {

    // description 非常关键！AI 是根据这个描述来决定什么时候调用这个方法的
    @Tool(description = "根据商品名称查询当前实时库存数量。")
    public String getStock(
            @ToolParam(description = "商品名称，例如：iPhone 15, 机械键盘") String productName) {

        // 这里模拟你的 Java 业务逻辑（查数据库、调 RPC 等）
        System.out.println(">>>> [Java 底层执行] 正在查询商品：" + productName);

        if (productName.contains("iPhone")) {
            return "iPhone 15 当前库存为 120 台。";
        } else if (productName.contains("键盘")) {
            return "机械键盘当前库存为 0 台，已售罄。";
        }
        return productName + " 暂时缺货，未找到该商品。";
    }
}