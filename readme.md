# AI Agent Lab (Java Edition)

> 一个基于 Java 17 + Spring Boot 3.5 + Spring AI 构建的 LLM 应用与 Agent 学习实战项目。后端工程师零 Python 基础无缝转型 AI 应用开发的工程化实践。

## 📖 项目简介
本项目旨在探索并实践大模型（DeepSeek）在企业级 Java 后端架构中的落地方式。摒弃传统 Python 技术栈的束缚，完全复用 Spring 生态的自动装配、依赖注入、AOP 切面等成熟工程思想，构建高可用、可扩展的 AI 智能体（Agent）。

## 🛠️ 核心技术栈
*   **基础环境**: JDK 17, Maven 3.6+
*   **核心框架**: Spring Boot 3.5.x
*   **AI 集成**: Spring AI 1.1.x (DeepSeek Starter)
*   **大模型**: DeepSeek API (`deepseek-chat`)
*   **架构设计**: 构造器注入、Advisor 责任链、Tool Calling 反射机制

## ✨ 已实现核心特性

- [x] **基础对话**：无缝对接 DeepSeek，支持 System Prompt 人设设定。
- [x] **流式输出 (SSE)**：基于 Reactor `Flux` 实现打字机效果，适合 C 端交互。
- [x] **工具调用 (Function Calling)**：利用 `@Tool` 注解将 Java 方法暴露给大模型（如：模拟查询实时库存）。
- [x] **Agent 循环 (ReAct)**：底层支持大模型自主决策、多次调用工具链直至生成最终答案。
- [x] **可观测性 (Observability)**：自定义 `ToolLoggingAdvisor` 拦截并打印大模型工具调用的原始报文，透视底层交互。
- [ ] **RAG 增强检索**（进行中）：文档读取、切分与向量化存储。

## 📂 工程目录结构
```text
src/main/java/com/ggzed/chat/
├── ChatApplication.java                 # 启动类
├── common/                              # 通用层
│   ├── exception/                       # 全局异常处理 (如 AI 超时、Token 超限的自定义异常)
│   ├── result/                          # 统一返回结构 (Result<T>)
│   └── utils/                           # 工具类 (JsonUtil, TokenCalculateUtil)
├── config/                              # 配置层 (组装 Bean)
│   ├── ChatClientConfig.java            # 核心：组装带 Advisor 的 ChatClient
│   ├── VectorStoreConfig.java           # (后续用) 向量库配置
│   └── AsyncConfig.java                 # 线程池配置 (流式输出需要)
├── advisor/                             # 切面层 (拦截、增强、横切逻辑)
│   ├── ToolLoggingAdvisor.java          # 日志/工具调用审计
│   └── TokenUsageAdvisor.java           # 统计每次请求的 Token 消耗
├── tools/                               # 工具层 (Agent 的“手脚”)
│   ├── InventoryTools.java              # 库存查询工具
│   ├── EmailTools.java                  # 发邮件工具 (让 AI 帮你发邮件)
│   └── DatabaseTools.java               # 查数据库工具
├── model/                               # 模型层 (DTO)
│   ├── request/                         # 前端传参 (如 ChatRequest)
│   └── response/                        # 返回对象 (如 ChatResponseVO)
├── service/                             # 业务逻辑层 (编排 AI 能力)
│   ├── ChatService.java                 # 接口
│   ├── AgentService.java                # 接口
│   └── impl/
│       ├── ChatServiceImpl.java         # 实现类 (调用 ChatClient)
│       └── AgentServiceImpl.java        # 实现类 (复杂 Agent 逻辑编排)
└── controller/                          # 接口层 (对外暴露 HTTP)
    ├── ChatController.java              # 基础对话 API
    └── AgentController.java             # 智能体 API

src/main/resources/
├── application.yml                      # 全局配置
├── prompts/                             # 📁 提示词模板目录 (重要实践)
│   ├── system-assistant.st              # 系统人设模板 (Spring AI 支持从 classpath 加载)
│   └── rag-qa.st                        # RAG 问答模板
└── documents/                           # 📁 本地知识库文件 (后续 RAG 用)