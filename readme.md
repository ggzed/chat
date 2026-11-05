# chat

基于 **Spring Boot 4.1.1 + Spring AI 2.1.0-RC1 + Spring Cloud Alibaba Nacos + LangChain4j** 的 AI Agent 微服务示例工程。

工程由父工程统一管理版本，下辖 5 个可独立启动的服务：AI 对话/RAG、业务系统、MCP 工具服务、LangChain4j 对话、API 网关。所有 AI 服务通过 Nacos 注册与发现，网关作为唯一对外入口；AI 侧访问业务系统统一经 MCP Server 做防腐层，不再直连业务接口。

## 架构

```
                 ┌──────────────────────────────────────────────┐
   HTTP 调用      │  chat-gateway :8084                          │
  ──────────────▶│  /api/springai/**      /api/langchain4j/**   │
                 └───────┬───────────────────────┬──────────────┘
                         │ lb://chat-springai     │ lb://chat-langchain4j
                         ▼                        ▼
              ┌────────────────────┐   ┌──────────────────────┐
              │ chat-springai      │   │ chat-langchain4j     │
              │ :8080              │   │ :8083                │
              │ 对话/RAG/混合检索/  │   │ LangChain4j AI       │
              │ 会话记忆/Advisor    │   │ Service + 工具        │
              └─────────┬──────────┘   └──────────────────────┘
                        │ MCP Client（按服务名从 Nacos 动态解析）
                        ▼
              ┌────────────────────┐
              │ chat-mcp-server    │  MCP 协议 /mcp (STREAMABLE)
              │ :8082              │  工具：queryRiskLevel
              └─────────┬──────────┘
                        │ REST
                        ▼
              ┌────────────────────┐
              │ chat-business      │  GET /api/business/risk
              │ :8081              │  （模拟业务系统）
              └────────────────────┘
```

## 模块

| 模块 | 端口 | Nacos 服务名 | 包名 | 职责 |
|---|---|---|---|---|
| `chat-springai` | 8080 | `chat-springai` | `com.ggzed.springai` | Spring AI 对话、RAG、混合检索、会话记忆、MCP Client |
| `chat-business` | 8081 | 不注册 | `com.ggzed.business` | 模拟业务系统，供 MCP Server 调用 |
| `chat-mcp-server` | 8082 | `chat-mcp-server` | `com.ggzed.mcpserver` | MCP Server（WebMVC + STREAMABLE），暴露业务工具 |
| `chat-langchain4j` | 8083 | `chat-langchain4j` | `com.ggzed.lc4j` | LangChain4j AI Service + 本地工具 |
| `chat-gateway` | 8084 | `chat-gateway` | `com.ggzed.gateway` | Spring Cloud Gateway 统一入口、跨域 |

父工程坐标 `com.ggzed:chat`，`packaging=pom`，只做 `<modules>` 与依赖版本管理（Spring Boot / Spring AI / Spring Cloud BOM、`java.version`），子模块依赖不写版本。

## 统一的对话契约

`chat-springai` 与 `chat-langchain4j` 使用**完全相同**的路径、入参、出参，客户端可无缝切换。

| 项 | 约定 |
|---|---|
| 非流式 | `POST /api/chat/simple` |
| 流式 | `POST /api/chat/stream`（SSE，目前仅 `chat-springai` 提供） |
| 网关闭环节 | `POST /api/springai/chat/simple`、`POST /api/langchain4j/chat/simple` |
| 请求体 | `{"message": "问题内容", "sessionId": "会话 ID"}` |
| 成功响应 | `{"code": 200, "message": "操作成功", "data": {"sessionId": "...", "content": "..."}}` |
| 校验失败 | `{"code": 400, "message": "消息内容不能为空", "data": null}` |

约束：`message` 必填且 ≤4000 字符，`sessionId` 必填且 ≤64 字符。

## 接口一览

### chat-springai（8080）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/chat/simple` | 基础对话（统一契约） |
| POST | `/api/chat/stream` | 流式对话，`text/event-stream` |
| POST | `/api/rag/chat` | RAG 对话（`{query, sessionId}`） |
| POST | `/api/rag/chat_stream` | RAG 流式对话 |
| POST | `/api/search/semantic?query=` | 向量检索 |
| POST | `/api/search/hybrid` | 语义 + 关键词混合检索（RRF） |
| POST | `/api/knowledge/init` | 初始化向量库 |
| POST | `/api/knowledge/split_preview` | 文档切分预览 |
| POST | `/api/security/test` | 安全 Advisor 演示 |

### chat-langchain4j（8083）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/chat/simple` | AI Service 对话（统一契约） |

模型为 DeepSeek 的 OpenAI 兼容接口，当前配置为非流式 `OpenAiChatModel`，因此不提供 `/stream`。

### chat-mcp-server（8082）

- 协议端点：`POST /mcp`，MCP `STREAMABLE`（JSON-RPC，非 REST）
- 工具：`queryRiskLevel(customerId)`，内部调用 `chat-business` 的 `/api/business/risk`，超时与异常均做自然语言降级，不向模型抛出堆栈
- 启动即注册到 Nacos；`chat-springai` 侧不再配置静态 `url`，由 `McpClientDiscoveryConfig` 按服务名解析实例

### chat-business（8081）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/business/risk?customerId=` | 模拟客户风险等级查询（`VIP` 开头为低风险） |

## 配置说明

公共项（各 AI 服务一致）：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
```

### chat-springai 关键配置

| 配置 | 值 | 说明 |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/chat_session` | 会话持久化，首次启动自动建表 |
| `spring.elasticsearch.uris` | `http://192.168.101.100:9200` | ES 8.13 服务端 |
| `spring.ai.vectorstore.elasticsearch.index-name` | `financial_rag` | 需预先建好索引与 Mapping |
| `spring.ai.deepseek.*` | `deepseek-chat` | 对话模型，Key 取 `DEEPSEEK_API_KEY` |
| `spring.ai.openai.*` | `text-embedding-v3` | 仅用于 Embedding（DashScope 兼容端点），Chat 已禁用 |
| `spring.ai.mcp.client` | `type=SYNC`，无静态连接 | 连接由 Nacos 服务发现动态构建 |

### chat-mcp-server 关键配置

| 配置 | 值 | 说明 |
|---|---|---|
| `spring.ai.mcp.server.protocol` | `STREAMABLE` | 必须全大写，端点固定 `/mcp` |
| `spring.ai.mcp.server.name` | `financial-tools-server` | MCP 握手返回的 serverInfo |
| `spring.ai.mcp.server.type` | `async` | WebMVC 异步传输 |

### chat-gateway 路由

```yaml
- id: spring-ai-route
  uri: lb://chat-springai
  predicates: [ Path=/api/springai/** ]
  filters: [ RewritePath=/api/springai/(?<segment>.*), /api/${segment} ]
- id: langchain4j-route
  uri: lb://chat-langchain4j
  predicates: [ Path=/api/langchain4j/** ]
  filters: [ RewritePath=/api/langchain4j/(?<segment>.*), /api/${segment} ]
```

网关只做前缀剥离，两个服务内部路径完全一致，因此新增同类服务时只需加一条路由。

## 环境变量

| 变量 | 必填 | 用途 |
|---|---|---|
| `DEEPSEEK_API_KEY` | 是 | Spring AI / LangChain4j 对话模型 |
| `DASHSCOPE_API_KEY` | 是 | Embedding（OpenAI 兼容端点） |

## 本地启动

前置：JDK 21、Maven 3.9+、Nacos（8848）、Elasticsearch 8.13（9200）可用。

```bash
# 构建
mvn -T 1C clean package

# 按依赖顺序启动
mvn -pl chat-business     spring-boot:run   # 8081
mvn -pl chat-mcp-server   spring-boot:run   # 8082
mvn -pl chat-springai     spring-boot:run   # 8080
mvn -pl chat-langchain4j  spring-boot:run   # 8083
mvn -pl chat-gateway      spring-boot:run   # 8084
```

`chat-springai` 不再强依赖 `chat-mcp-server` 的启动顺序：Nacos 查不到工具服务时只告警，工具无法调用但应用可正常启动。

## 技术栈

| 组件 | 版本 |
|---|---|
| JDK | 21 |
| Spring Boot | 4.1.1 |
| Spring AI | 2.1.0-RC1 |
| Spring Cloud | 2025.1.4 |
| Spring Cloud Alibaba Nacos Discovery | 由 Spring Cloud BOM 管理 |
| LangChain4j | 由 `chat-langchain4j` 模块管理 |
| Elasticsearch（服务端 / 客户端） | 8.13.4 / BOM 管理 |
| H2 | runtime |

## 设计要点

- **AI 与业务解耦**：大模型不允许直接访问业务接口，统一经 MCP Server；工具不可用时返回自然语言降级话术，避免异常暴露给模型。
- **工具动态发现**：`chat-springai` 通过 Nacos 解析 `chat-mcp-server` 实例，工具清单由 MCP 握手动态获取，不再硬编码地址。
- ** Advisor 责任链**：Token 用量统计、安全护栏、混合检索、会话记忆按 order 串联，记忆类 Advisor 排在检索之后。
- **会话隔离**：Session ID + JDBC（H2）持久化，重启后仍可续聊，多个会话互不串扰。
