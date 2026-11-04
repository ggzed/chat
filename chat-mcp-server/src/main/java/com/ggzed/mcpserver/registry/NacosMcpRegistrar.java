package com.ggzed.mcpserver.registry;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerRemoteServiceConfig;
import com.alibaba.nacos.api.ai.model.mcp.McpServiceRef;
import com.alibaba.nacos.api.ai.model.mcp.McpTool;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;
import com.alibaba.nacos.api.ai.model.mcp.registry.ServerVersionDetail;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerFactory;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 将 MCP Server 注册到 Nacos 3.x 的 MCP Registry。
 *
 * <p>Spring AI Alibaba 的 nacos-mcp-server starter 编译于 Spring AI 1.0，与本项目的
 * Spring AI 2.1 包结构不兼容（McpServerProperties 已迁移包名），因此改为直接调用
 * Nacos SDK（nacos-client / nacos-maintainer-client）完成注册。</p>
 *
 * <p>Nacos 不可用时只记录告警，不影响 MCP Server 以本地 SSE/Streamable 方式提供服务。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NacosMcpRegistrar implements ApplicationListener<WebServerInitializedEvent> {

    private static final String ENDPOINT_TYPE_REF = "REF";
    private static final String KEY_SERVER_ADDR = "serverAddr";
    private static final String KEY_NAMESPACE = "namespace";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private final ToolCallbackProvider toolCallbackProvider;

    private NamingService namingService;
    private String registeredIp;
    private int registeredPort;

    @Value("${nacos.mcp.register.enabled:true}")
    private boolean enabled;

    @Value("${nacos.server-addr:127.0.0.1:8848}")
    private String serverAddr;

    @Value("${nacos.namespace:public}")
    private String namespace;

    @Value("${nacos.username:}")
    private String username;

    @Value("${nacos.password:}")
    private String password;

    @Value("${spring.ai.mcp.server.name:chat-mcp-server}")
    private String mcpName;

    @Value("${spring.ai.mcp.server.version:1.0.0}")
    private String version;

    @Value("${nacos.mcp.service-name:chat-mcp-server}")
    private String serviceName;

    @Value("${nacos.mcp.group-name:DEFAULT_GROUP}")
    private String groupName;

    /** Nacos MCP 协议标识，与 MCP Server 自身的 STREAMABLE/SSE 配置无关 */
    @Value("${nacos.mcp.protocol:mcp-streamable-http}")
    private String protocol;

    @Value("${nacos.mcp.export-path:/mcp}")
    private String exportPath;

    @Value("${nacos.mcp.description:金融业务工具 MCP 服务}")
    private String description;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (!enabled) {
            log.info("Nacos MCP 注册已关闭（nacos.mcp.register.enabled=false）");
            return;
        }
        int port = event.getWebServer().getPort();
        try {
            register(port);
        } catch (Exception e) {
            log.warn("向 Nacos({}) 注册 MCP 服务失败，服务仍可在本地直接访问: {}", serverAddr, e.getMessage());
        }
    }

    private void register(int port) throws NacosException {
        Properties properties = new Properties();
        properties.put(KEY_SERVER_ADDR, serverAddr);
        properties.put(KEY_NAMESPACE, namespace);
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            properties.put(KEY_USERNAME, username);
            properties.put(KEY_PASSWORD, password);
        }

        AiMaintainerService maintainer = AiMaintainerFactory.createAiMaintainerService(properties);
        namingService = NacosFactory.createNamingService(properties);

        if (!waitUntilConnected(namingService)) {
            log.warn("Nacos({}) 连接超时，跳过 MCP 注册，服务仍可在本地直接访问", serverAddr);
            return;
        }

        Instance instance = new Instance();
        instance.setIp(resolveLocalIp());
        instance.setPort(port);
        instance.setEphemeral(true);
        registeredIp = instance.getIp();
        registeredPort = port;
        namingService.registerInstance(serviceName, groupName, instance);

        McpServerBasicInfo basicInfo = buildBasicInfo();
        McpToolSpecification toolSpecification = buildToolSpecification();
        McpEndpointSpec endpointSpec = buildEndpointSpec();

        try {
            maintainer.createMcpServer(namespace, mcpName, basicInfo, toolSpecification, endpointSpec);
        } catch (NacosException e) {
            log.info("Nacos 已存在同名 MCP 服务，改为更新: {}", e.getErrMsg());
            maintainer.updateMcpServer(namespace, mcpName, true, basicInfo, toolSpecification, endpointSpec);
        }

        log.info("MCP 服务已注册到 Nacos: name={} version={} protocol={} endpoint={}:{}{}",
                mcpName, version, protocol, serviceName, groupName, exportPath);
    }

    private McpServerBasicInfo buildBasicInfo() {
        ServerVersionDetail versionDetail = new ServerVersionDetail();
        versionDetail.setVersion(version);
        versionDetail.setIs_latest(true);

        McpServerRemoteServiceConfig remoteServiceConfig = new McpServerRemoteServiceConfig();
        remoteServiceConfig.setExportPath(exportPath);
        remoteServiceConfig.setServiceRef(buildServiceRef());

        McpServerBasicInfo basicInfo = new McpServerBasicInfo();
        basicInfo.setName(mcpName);
        basicInfo.setProtocol(protocol);
        basicInfo.setFrontProtocol(protocol);
        basicInfo.setDescription(description);
        // Nacos 服务端校验要求 serverSpecification 中同时存在 version 字段
        basicInfo.setVersion(version);
        basicInfo.setVersionDetail(versionDetail);
        basicInfo.setRemoteServerConfig(remoteServiceConfig);
        basicInfo.setEnabled(true);
        return basicInfo;
    }

    private McpToolSpecification buildToolSpecification() {
        List<McpTool> mcpTools = new ArrayList<>();
        for (ToolCallback callback : toolCallbackProvider.getToolCallbacks()) {
            ToolDefinition definition = callback.getToolDefinition();
            McpTool tool = new McpTool();
            tool.setName(definition.name());
            tool.setDescription(definition.description());
            tool.setInputSchema(parseSchema(definition.inputSchema()));
            mcpTools.add(tool);
        }
        McpToolSpecification toolSpecification = new McpToolSpecification();
        toolSpecification.setTools(mcpTools);
        return toolSpecification;
    }

    /** 便于控制台展示，把服务坐标同时写入 remoteServerConfig.serviceRef */
    private McpServiceRef buildServiceRef() {
        McpServiceRef serviceRef = new McpServiceRef();
        serviceRef.setNamespaceId(namespace);
        serviceRef.setGroupName(groupName);
        serviceRef.setServiceName(serviceName);
        serviceRef.setTransportProtocol(protocol);
        return serviceRef;
    }

    /** REF 类型下 Nacos 3.3.0 要求 data 中必须同时包含 namespaceId、groupName、serviceName */
    private McpEndpointSpec buildEndpointSpec() {
        McpEndpointSpec endpointSpec = new McpEndpointSpec();
        endpointSpec.setType(ENDPOINT_TYPE_REF);
        endpointSpec.setData(Map.of(
                "namespaceId", namespace,
                "groupName", groupName,
                "serviceName", serviceName));
        return endpointSpec;
    }

    private Map<String, Object> parseSchema(String inputSchema) {
        if (!StringUtils.hasText(inputSchema)) {
            return Map.of();
        }
        Map<String, Object> schema = JacksonUtils.toObj(inputSchema, Map.class);
        return schema == null ? Map.of() : schema;
    }

    /** 解析本机 IP，失败时退回回环地址，保证注册流程不中断 */
    private String resolveLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("获取本机 IP 失败，注册地址退回 127.0.0.1: {}", e.getMessage());
            return "127.0.0.1";
        }
    }

    /** Nacos SDK 异步建连，注册前等待连接就绪，最长约 2 秒 */
    private boolean waitUntilConnected(NamingService naming) {
        try {
            for (int i = 0; i < 10; i++) {
                if ("UP".equalsIgnoreCase(naming.getServerStatus())) {
                    return true;
                }
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /** 应用关闭时主动摘除临时实例，避免 Nacos 侧残留幽灵节点 */
    @PreDestroy
    public void deregister() {
        if (namingService == null || registeredIp == null) {
            return;
        }
        try {
            namingService.deregisterInstance(serviceName, groupName, registeredIp, registeredPort);
            log.info("已从 Nacos 摘除 MCP 服务实例 {}:{}", registeredIp, registeredPort);
        } catch (NacosException e) {
            log.warn("摘除 Nacos 实例失败: {}", e.getErrMsg());
        }
    }
}
