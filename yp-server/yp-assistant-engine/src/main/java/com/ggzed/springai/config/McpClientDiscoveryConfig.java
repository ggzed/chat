package com.ggzed.springai.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * 通过 Nacos 服务发现动态连接 MCP Server。
 *
 * <p>Spring AI 的 {@code spring.ai.mcp.client.streamable-http.connections.*.url} 只支持静态地址，
 * 一旦 MCP Server 未启动，8080 会因初始化失败而无法启动。这里改为运行时从 Nacos 拉取
 * {@code yp-agent-mcp-hub} 的实例地址，MCP Server 未注册时只告警、不阻断启动。</p>
 *
 * <p>本类创建出的 {@link McpSyncClient} 会被 Spring AI 的
 * {@code McpToolCallbackAutoConfiguration} 自动聚合成 {@code ToolCallbackProvider}。</p>
 */
@Slf4j
@Configuration
public class McpClientDiscoveryConfig {

    private final DiscoveryClient discoveryClient;
    private final JsonMapper jsonMapper;

    public McpClientDiscoveryConfig(DiscoveryClient discoveryClient, JsonMapper jsonMapper) {
        this.discoveryClient = discoveryClient;
        this.jsonMapper = jsonMapper;
    }

    @Value("${mcp.client.service-name:yp-agent-mcp-hub}")
    private String serviceName;

    @Value("${mcp.client.endpoint:/mcp}")
    private String endpoint;

    @Value("${mcp.client.request-timeout:30s}")
    private Duration requestTimeout;

    @Bean(destroyMethod = "close")
    public McpSyncClient mcpSyncClient() {
        String baseUrl = resolveBaseUrl();
        if (baseUrl == null) {
            log.warn("Nacos 中未发现 {} 的可用实例，MCP 工具暂不可用（不影响应用启动）", serviceName);
            return null;
        }

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(baseUrl)
                .endpoint(endpoint)
                .clientBuilder(HttpClient.newBuilder())
                .jsonMapper(new JacksonMcpJsonMapper(jsonMapper))
                .build();

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(requestTimeout)
                .build();
        try {
            client.initialize();
            log.info("MCP 客户端已连接 {}，可用工具数: {}", baseUrl + endpoint, client.listTools().tools().size());
        } catch (Exception e) {
            log.warn("MCP 客户端连接 {} 失败，工具暂不可用: {}", baseUrl + endpoint, e.getMessage());
        }
        return client;
    }

    /** 从 Nacos 取第一个健康实例；Nacos 缓存可能尚未就绪，做有限次重试 */
    private String resolveBaseUrl() {
        for (int i = 0; i < 3; i++) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (!instances.isEmpty()) {
                ServiceInstance instance = instances.get(0);
                return "http://" + instance.getHost() + ":" + instance.getPort();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }
}
