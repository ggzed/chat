package com.ggzed.mcpserver.tools;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BusinessMcpTools {

    private final RestClient restClient = RestClient.create();

    @Tool(description = "根据客户号查询客户的风险承受等级。")
    public String queryRiskLevel(@ToolParam(description = "客户号，例如 VIP001") String customerId) {
        System.out.println("BusinessMcpTools queryRiskLevel");
        // 通过 HTTP 调用 8081 业务微服务
        String response = restClient.get()
                .uri("http://localhost:8081/api/business/risk?customerId={id}", customerId)
                .retrieve()
                .body(String.class);
        System.out.println("风控查询结果：" + response);
        return "风控查询结果：" + response;
    }
}