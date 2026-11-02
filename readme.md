# AI Agent Lab (Java Edition)

基于 Spring Boot 4 + Spring AI 的 LLM 应用实战项目：用 Java 后端工程的成熟手段（自动装配、依赖注入、Advisor 责任链、分层架构）落地一个带 RAG、多轮记忆与安全风控的企业级 Agent。

## 技术栈

| 项目 | 选型 |
| --- | --- |
| 运行时 | JDK 21 / Maven 3.6+ |
| 框架 | Spring Boot 4.1.1 |
| 微服务 | Spring Cloud 2025.1.4（BOM 统一版本管理） |
| AI 框架 | Spring AI 2.1.0-RC1 |
| 工具协议 | Spring AI MCP（Server: WebFlux + SSE / Client: SYNC） |
| 对话模型 | DeepSeek（`deepseek-chat`） |
| 向量模型 | DashScope `text-embedding-v3`（1024 维，走 OpenAI 兼容接口） |
| 向量库 | Elasticsearch 8.13.4（dense_vector + BM25） |
| 会话存储 | H2 文件库（`./data/chat_session`） |

## 核心能力

- **基础对话**：非流式 + SSE 流式输出（Reactor `Flux`）。
- **RAG 增强检索**：PDF 读取 → 切分 → 向量化，写入 ES 后用检索结果增强 Prompt。
- **混合检索**：BM25 关键词召回 + KNN 向量召回，应用层 RRF 融合排序（可观察两路得分，便于调参）。
- **多轮对话记忆**：接入 Spring AI Session API，`SessionMemoryAdvisor` 按 `sessionId` 读写历史；H2 持久化，重启后会话不丢，不同 `sessionId` 完全隔离。
- **Token 用量统计**：`TokenUsageAdvisor` 记录每次调用的 Token 与预估费用。
- **安全风控**：`SecurityAdvisor` 在工具执行前拦截高危工具（如转账）并抛审批异常，配合全局异常处理返回统一错误。
- **工具调用**：`@Tool` 注解暴露本地 Java 方法（理财收益计算、库存查询等）。
- **MCP 工具接入**：`chat-springai` 作为 MCP Client，通过 SSE 连接 `chat-mcp-server`，由后者把 `chat-business` 的业务能力包装成 MCP 工具（`queryRiskLevel`），实现「业务服务与 AI 服务解耦」。
- **统一响应**：所有接口统一 `Result<T>` 包装，分层架构 `ctl → service → model`，参数用 `@Valid` 校验。

## Advisor 责任链

Advisor 按 `getOrder()` 升序执行，`before` 由外到内、`after` 由内到外：

```text
TokenUsageAdvisor      HIGHEST + 100   记录耗时与 Token
SessionMemoryAdvisor   HIGHEST + 100   加载历史消息（先于检索，保证检索只用最新一问）
HybridSearchAdvisor    HIGHEST + 200   召回资料，只替换最后一条 UserMessage
ToolCallingAdvisor     默认             工具循环
SecurityAdvisor        HIGHEST + 400   工具执行前高危拦截
```

## 接口一览

| 模块 | 方法 | 路径 | 请求体 / 参数 | 说明 |
| --- | --- | --- | --- | --- |
| 对话 | POST | `/api/chat/simple` | `{message, sessionId}` | 非流式对话 |
| 对话 | POST | `/api/chat/stream` | `{message, sessionId}` | 流式对话（SSE） |
| RAG | POST | `/api/rag/chat` | `{query, sessionId}` | RAG 非流式问答 |
| RAG | POST | `/api/rag/chat_stream` | `{query, sessionId}` | RAG 流式问答（SSE） |
| 检索 | POST | `/api/search/semantic` | `query`（query 参数） | 纯向量语义检索 |
| 检索 | POST | `/api/search/hybrid` | `{query}` | 混合检索（BM25 + KNN + RRF） |
| 知识库 | POST | `/api/knowledge/init` | 无 | 初始化向量库（幂等） |
| 知识库 | POST | `/api/knowledge/split_preview` | 无 | 预览 PDF 切分结果，不入库 |
| 风控 | POST | `/api/security/test` | `{query}` | 绕过 RAG 直发指令，验证工具拦截 |
| 业务 | GET | `/api/business/risk` | `customerId` | `chat-business`(8081) 模拟客户风控等级查询 |

`sessionId` 必填（最长 64 字符），同一 ID 的多轮对话共享上下文。除最后一行属于 `chat-business`（8081）外，其余均在 `chat-springai`（8080）。

## 模块结构

本仓库是微服务父工程（`com.lol:chat`，`packaging=pom`），只负责 `<modules>` 与依赖版本管理（Spring Boot / Spring AI / Spring Cloud BOM），不含业务代码：

```text
chat/                          # 父工程，packaging=pom
├── pom.xml                    # 聚合 + 统一版本管理
├── chat-springai/   :8080     # AI 服务：对话 / RAG / Agent，同时是 MCP Client
├── chat-mcp-server/ :8082     # MCP Server：把业务能力包装成 MCP 工具
└── chat-business/   :8081     # 业务服务：客户风控等级查询
```

调用链：

```text
用户 → chat-springai(8080) ──MCP/SSE──▸ chat-mcp-server(8082) ──HTTP──▸ chat-business(8081)
              │
              ├─▸ DeepSeek（对话）
              ├─▸ DashScope（Embedding）
              └─▸ Elasticsearch（向量库 / 混合检索）
```

各子模块只写 `<parent>com.lol:chat</parent>` + 依赖，版本一律由父工程 BOM 管理；新增子服务时在根 pom 的 `<modules>` 加一行即可。

## 快速开始

### 1. 准备环境

```powershell
$env:DEEPSEEK_API_KEY="<你的 DeepSeek Key>"
$env:DASHSCOPE_API_KEY="<你的 DashScope Key>"
```

Elasticsearch 需可访问（默认 `http://192.168.101.100:9200`，可在 `application.yml` 的 `spring.elasticsearch.uris` 修改），并预先建好索引 `financial_rag`：

- `content`：text
- `embedding`：dense_vector，dimensions = 1024

### 2. 启动（注意顺序）

```powershell
# 终端 1：业务服务 8081
mvn -pl chat-business spring-boot:run

# 终端 2：MCP Server 8082（需在 AI 服务之前就绪）
mvn -pl chat-mcp-server spring-boot:run

# 终端 3：AI 服务 8080
mvn -pl chat-springai spring-boot:run
```

`chat-springai` 启动时会**同步初始化 MCP Client 并拉取工具列表**，8082 未就绪会直接导致启动失败（`mcpSyncClients ... Client failed to initialize`）。只调试对话、不测 MCP 工具时，可临时把 `spring.ai.mcp.client.enabled` 设为 `false`。

首次启动会在 `chat-springai/data/` 下生成 H2 会话库，并自动建表（`AI_SESSION` / `AI_SESSION_EVENT` 等）。

> 也可在 IDEA 中直接运行各模块的启动类：需把 working directory 设为对应模块目录（否则 H2 文件会生成在父目录）。

### 3. 初始化知识库

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/knowledge/init
```

### 4. 对话

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/rag/chat `
  -ContentType "application/json" `
  -Body '{"query":"这款理财产品的业绩比较基准是多少？","sessionId":"demo-1"}'
```

## 工程结构

```text
chat-springai/src/main/java/com/ggzed/chat/
├── ChatApplication.java              # 启动类
├── ctl/                              # 接口层：只做路由与参数校验
│   ├── ChatController.java           # 基础对话
│   ├── RagController.java            # RAG 问答
│   ├── SearchController.java         # 检索调试
│   ├── KnowledgeController.java      # 知识库管理
│   └── SecurityController.java       # 风控验证
├── service/                          # 业务层
│   ├── ChatService.java / RagService.java / HybridSearchService.java
│   └── impl/                         # 对应实现
├── advisor/                          # Advisor 责任链
│   ├── TokenUsageAdvisor.java        # Token 与费用统计
│   ├── SessionMemoryAdvisor.java     # 框架提供，会话记忆（Bean 在 config 中注册）
│   ├── HybridSearchAdvisor.java      # 混合检索注入上下文
│   ├── SecurityAdvisor.java          # 高危工具拦截
│   └── ToolLoggingAdvisor.java       # 工具调用审计
├── config/
│   ├── ChatClientConfig.java         # ChatClient 与 Advisor 装配
│   ├── ElasticsearchCompatConfig.java# 9.x 客户端兼容 8.x 服务端
│   └── AiAsyncConfig.java            # 异步线程池
├── tools/                            # 工具层
│   ├── FinanceTools.java             # 理财收益计算、转账、申购
│   └── InventoryTools.java           # 库存查询
├── model/
│   ├── request/                      # ChatRequest / RagRequest / RagChatRequest
│   └── dto/                          # TokenUsageRecord
└── common/
    ├── result/                       # Result<T>、ResultCode
    └── exception/                    # 全局异常处理、ApprovalRequiredException

chat-springai/src/main/resources/
├── application.yml                   # 模型、ES、H2、Session、MCP Client 配置
└── doc/xy92.pdf                      # RAG 示例文档

chat-business/src/main/java/com/ggzed/chat/
├── BusinessApplication.java          # 启动类（8081）
└── controller/BusinessController.java# GET /api/business/risk

chat-mcp-server/src/main/java/com/ggzed/mcpserver/
├── McpServerApplication.java         # 启动类（8082）
├── config/McpServerConfig.java       # 注册 ToolCallbackProvider
└── tools/BusinessMcpTools.java       # @McpTool queryRiskLevel，内部 RestClient 调 8081
```

## 已知注意事项

- **ES 版本兼容**：服务端为 8.13.4，而 Spring AI 的 ES Starter 依赖 9.x 客户端。`ElasticsearchCompatConfig` 会在 HTTP 层把媒体类型改写为 `compatible-with=8`，否则 8.x 服务端返回 400。
- **KNN 参数**：8.x 的 query DSL kNN 不接受 `k` 参数（`k` 是 9.x 语法），因此混合检索在应用层自行融合，不依赖服务端 `rank.rrf`。
- **流式 Token 统计**：DeepSeek 的流式响应不返回 usage，`TokenUsageAdvisor` 的统计仅对非流式调用准确。
- **Spring Cloud 版本**：父工程导入的是最新 GA `2025.1.4`（官方对应 Boot 4.0.x），而当前 Boot 为 4.1.1，暂无匹配的 Cloud 正式版本。BOM 只做版本管理、未引入任何 starter，所以不影响启动；真正接入注册中心/网关前需要对齐 Boot 与 Spring Cloud（或等 `2026.0` GA）。
- **MCP 传输方式**：`spring-ai-starter-mcp-server-webflux` 默认走 **SSE**（`GET /sse` 建流 + `POST /mcp/message` 发消息），Client 端必须配 `spring.ai.mcp.client.sse.connections`；若误配 `streamable-http` 会因 `POST /` 返回 404 而报 `Server Not Found`。
- **工具来源不一致**：`RagService.chat()` 已改为挂载 MCP 工具（`ToolCallbackProvider`），而 `chatStream()` 与 `testSecurity()` 仍挂本地 `FinanceTools`，流式链路暂时用不到 MCP 工具，后续需统一。
- **会话数据**：`data/` 已加入 `.gitignore`，H2 数据文件不入库。
