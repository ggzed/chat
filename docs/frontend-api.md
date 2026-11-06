# yp-front 接口需求文档

> 统计范围：`yp-front` 当前 17 个路由页面（含 403 页）实际展示与操作所需的数据。
> 现状：页面数据全部来自 `src/data/console.ts` 的静态 Mock，本文档定义替换 Mock 时后端需要提供的接口契约。
> 日期：2026-11-07 · 共 45 个接口；对话、RAG、检索、知识库、MCP、业务模拟 6 类能力已有后端实现可供复用。

## 1. 通用约定

### 1.1 路径与网关

| 类别 | 前缀 | 说明 |
|---|---|---|
| 控制台管理接口 | `/api/admin/**` | 由 `yp-agent-admin`（8085）承载，网关 `agent-admin-route` 透传 |
| AI 对话接口 | `/api/chat/assistant/**`、`/api/chat/workflow/**` | 已存在，经 `yp-agent-gateway` 8080 |
| MCP 协议端点 | `POST /mcp` | 已存在，由 `yp-agent-mcp-hub` 提供，前端不直接调用 |

开发环境 `VITE_PROXY_TARGET=http://localhost:8080`，`VITE_CHAT_PREFIX=/api/chat/assistant`。

### 1.2 统一响应

除 SSE 与文件导出外，均返回与后端现有契约一致的包裹结构：

```json
{ "code": 200, "message": "操作成功", "data": {} }
```

| HTTP / code | 含义 | 前端处理 |
|---|---|---|
| 200 | 成功 | 直接取 `data` |
| 400 | 参数校验失败 | 表单内联错误 |
| 401 | 未登录或 Token 失效 | 跳登录 / 提示重新登录 |
| 403 | 角色无权访问 | 跳 `/denied?from=<路径>` |
| 404 | 资源不存在 | 空态提示 |
| 500 | 服务异常 | Toast + 可重试 |

### 1.3 鉴权与角色

- 请求头：`Authorization: Bearer <token>`
- 角色枚举：`sales`（业务员）、`admin`（管理员）、`ops`（运维/主管）
- 前端 `router.beforeEach` 只做页面级拦截，**后端必须对每个接口再次校验角色与数据范围**，不能仅依赖前端。
- 响应头建议返回 `X-Trace-Id`，便于「全链路追踪」页直接回放。

### 1.4 分页、时间与导出

- 分页参数：`page`（从 1 开始）、`size`（默认 20），响应 `{ total, page, size, items }`。
- 时间：ISO 8601 带时区（如 `2026-11-06T19:42:11+08:00`）；前端负责展示为 `MM-DD HH:mm`。
- 导出：`text/csv; charset=utf-8` + `Content-Disposition: attachment`。

## 2. 接口总览

| 模块 | 页面 | 数量 | 接口编号 |
|---|---|---|---|
| 认证与角色 | 全部 | 2 | F-01 ~ F-02 |
| 系统总览 | `/dashboard/overview` | 1 | F-03 |
| 告警 | `/dashboard/alerts` | 3 | F-04 ~ F-06 |
| 应用中心 | `/apps` | 2 | F-07 ~ F-08 |
| 对话 | `/apps/rule-qa`、`/apps/analytics` | 6 | F-09 ~ F-14 |
| Agent 管理 | `/agents`、`/agents/new` | 8 | F-15 ~ F-22 |
| 工具与 MCP | `/hub/tools`、`/hub/servers`、`/hub/debug` | 7 | F-23 ~ F-29 |
| 知识库 | `/kb/docs`、`/kb/recall` | 8 | F-30 ~ F-37 |
| 计费 | `/audit/billing` | 3 | F-38 ~ F-40 |
| 全链路追踪 | `/audit/trace` | 2 | F-41 ~ F-42 |
| 合规审计 | `/audit/compliance` | 3 | F-43 ~ F-45 |

`/agents/flow` 为概念示意（画布不可保存），`/denied` 为纯前端页，均不需要接口。

## 3. 接口明细

### 3.1 认证与角色

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-01 | GET | `/api/admin/auth/me` | 当前用户、可用角色、权限点、默认激活角色 | 全部 |
| F-02 | POST | `/api/admin/auth/role` | 切换激活角色，返回生效后的角色与权限 | 全部 |

`GET /auth/me` 响应：

```ts
interface MeResponse {
  userId: string
  name: string
  dept: string
  roles: Array<'sales' | 'admin' | 'ops'>
  activeRole: 'sales' | 'admin' | 'ops'
  permissions: string[]
  short: string        // 头像文字，如「林」
}
```

`POST /auth/role` 请求 `{ "role": "ops" }`，响应同 `MeResponse`。切换后前端立即复核当前路由，无权限则跳 `/denied`。

### 3.2 系统总览

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-03 | GET | `/api/admin/dashboard/overview?date=2026-11-06` | KPI、24h 趋势、服务健康度、Agent 排行、未读告警 | 全部 |

```ts
interface OverviewResponse {
  date: string
  kpis: {
    calls: Kpi              // 今日调用量
    latency: Kpi            // 平均响应时延（秒）
    tokens: Kpi             // 今日 Token 消耗
    errorRate: Kpi          // 错误率（%）
  }
  trend24h: { primary: number[]; secondary: number[] }   // 24 个点，调用量 / 失败量
  health: Array<{ name: string; status: 'ok' | 'warn' | 'down'; latency: string }>
  topAgents: Array<{ name: string; value: number }>
  alerts: Array<{ id: string; level: 'P1' | 'P2' | 'P3'; text: string; time: string }>
  availability: string     // 如「99.97%」
  notice: string
}

interface Kpi {
  value: string | number
  unit?: string
  delta: string
  trend: 'up' | 'down' | 'flat'
  spark: number[]
}
```

业务员视角不展示告警条与「最近告警」面板；后端可按 `activeRole` 返回 `alerts: []`，前端也会隐藏。

### 3.3 告警

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-04 | GET | `/api/admin/alerts?state=&level=&page=&size=` | 告警列表与筛选 | admin、ops |
| F-05 | POST | `/api/admin/alerts/{id}/claim` | 认领告警（未处理 → 处理中） | admin |
| F-06 | POST | `/api/admin/alerts/read-all` | 全部标记已读，用于侧边栏角标清零 | admin |

```ts
interface AlertItem {
  id: string
  time: string
  level: 'P1' | 'P2' | 'P3'
  title: string
  service: string
  impact: string
  state: '未处理' | '处理中' | '已恢复'
}
```

`ops` 只读：F-05 返回 403，前端也会禁用「处理」按钮。
「查看值班」当前为演示跳转，若需真实值班表，另加 `GET /api/admin/alerts/duty-roster`（可选，不计入 45 个）。

### 3.4 应用中心

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-07 | GET | `/api/admin/apps` | 当前角色已授权的 Agent 卡片 | sales、admin |
| F-08 | POST | `/api/admin/apps/{code}/subscriptions` | 关注未上线 Agent 的上线通知 | sales、admin |

```ts
interface AppCard {
  code: string          // 端点，如 rule_qa_v3
  name: string
  icon: string
  desc: string
  calls: number         // 今日会话
  latency: string       // 如 0.9s
  satisfaction: string  // 如 96%
  status: 'online' | 'gray' | 'offline'
  route: string         // 前端路由，如 /apps/rule-qa
}
```

F-08 为可选增强，当前点击仅 Toast；若不实现可保留本地提示。

### 3.5 对话（双屏页）

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-09 | POST | `/api/admin/chat/sessions` | 新建会话，返回 `sessionId` | sales、admin |
| F-10 | GET | `/api/admin/chat/sessions/{sessionId}/messages` | 历史消息回显 | sales、admin |
| F-11 | DELETE | `/api/admin/chat/sessions/{sessionId}` | 清空/重置会话 | sales、admin |
| F-12 | POST | `/api/chat/assistant/stream` | SSE 流式对话（**复用现有接口**） | sales、admin |
| F-13 | GET | `/api/admin/chat/sessions/{sessionId}/references?messageId=` | RAG 参考来源（规则模式） | sales、admin |
| F-14 | GET | `/api/admin/chat/traces/{traceId}/steps` | 执行时间线（数据模式） | sales、admin |

会话创建请求：

```json
{ "agentCode": "rule_qa_v3", "mode": "rule" }
```

`data` 返回 `{ "sessionId": "sess_..." }`。`mode` 可选值 `rule`、`data`，与两个路由一一对应。

F-12 沿用现有统一契约并建议扩展两个可选字段：

```json
{ "message": "单笔超过 50 万的转账需要什么审批？", "sessionId": "sess_...", "agentCode": "rule_qa_v3", "mode": "rule" }
```

前端 SSE 期望事件（若后端暂时只能返回纯文本，前端降级为仅正文流式，右侧面板隐藏）：

| event | data | 说明 |
|---|---|---|
| `meta` | `{ sessionId, traceId }` | 首帧，用于追踪与时间线 |
| `reference` | `{ refs: RefItem[] }` | 规则模式参考来源 |
| `step` | `{ index, title, desc, ms, status }` | 数据模式执行步骤，可多次 |
| `delta` | `{ content }` | 文本增量 |
| `done` | `{ content, usage: { input, output } }` | 结束帧 |
| `error` | `{ code, message }` | 异常帧 |

```ts
interface ChatMessage {
  id: string
  role: 'user' | 'ai'
  content: string
  time: string
  traceId?: string
}

interface RefItem {
  src: string      // 文档 · 页码
  score: string
  txt: string      // 允许包含 <mark> 高亮
  bm25: string
  knn: string
}

interface TimelineStep {
  title: string
  desc: string
  ms: string
  status?: 'done' | 'running' | 'failed'
}
```

时间线的 `traceId` 由 `meta` 帧下发；也可以直接由 F-12 响应头 `X-Trace-Id` 提供。

### 3.6 Agent 管理

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-15 | GET | `/api/admin/agents?status=&keyword=` | Agent 列表与筛选 | admin |
| F-16 | GET | `/api/admin/agents/{code}` | 编辑回填完整配置 | admin |
| F-17 | POST | `/api/admin/agents` | 新建并保存（草稿） | admin |
| F-18 | PUT | `/api/admin/agents/{code}` | 更新配置 | admin |
| F-19 | POST | `/api/admin/agents/{code}/publish` | 发布，写入 Nacos 配置 | admin |
| F-20 | POST | `/api/admin/agents/{code}/offline` | 下线，端点停止调用 | admin |
| F-21 | GET | `/api/admin/agents/export` | 导出 Agent 清单 CSV | admin |
| F-22 | GET | `/api/admin/agents/options` | 向导可选项：模型、部门、知识库、工具分类、护栏 | admin |

```ts
interface AgentItem {
  name: string
  code: string
  type: '问答型' | '长链路型'
  model: string
  status: '已发布' | '草稿' | '已下线'
  tools: number
  calls: number
  by: string
  time: string
}
```

向导表单（新建/编辑共用一个接口体）：

```ts
interface WizardForm {
  name: string
  code: string
  type: '问答型' | '长链路型'
  desc: string
  dept: string
  model: string
  temp: number
  topP: number
  prompt: string
  kb: string
  topK: number
  threshold: number
  hybrid: boolean
  memory: boolean
  rounds: number
  tools: string[]
  guards: string[]
  quota: number
  note: string
}
```

发布校验（后端必须实现，前端已有同等拦截）：

- 挂载 `freeze_quota`（冻结额度）或 `big_debit`（大额扣款）时，`guards` 必须包含 `block`（高危阻断），否则返回 400，`message` 为「已挂载高危工具，必须启用「高危阻断」护栏后才能发布」。
- `code` 以小写字母开头，仅含小写字母、数字、下划线，且全局唯一。

`F-19` 响应建议包含 `dataId`、`nacosDataId`、`endpoint` 与预计生效时间。

`F-22` 响应：

```ts
interface AgentOptions {
  models: string[]
  depts: string[]
  knowledgeBases: Array<{ code: string; name: string }>
  toolCategories: Array<{ category: string; items: Array<{ id: string; name: string; desc: string; risk: boolean }> }>
  guards: Array<{ id: string; name: string; desc: string }>
}
```

### 3.7 工具与 MCP

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-23 | GET | `/api/admin/tools?category=&keyword=&status=` | 工具分类目录 | admin、ops |
| F-24 | GET | `/api/admin/tools/categories` | 分类筛选项 | admin、ops |
| F-25 | POST | `/api/admin/tools/{code}/invoke` | 接口调试（正文 + 耗时 + TraceID） | admin |
| F-26 | GET | `/api/admin/mcp-servers` | 已注册服务列表 | admin、ops |
| F-27 | POST | `/api/admin/mcp-servers/{name}/test` | 连通性测试与握手耗时 | admin |
| F-28 | POST | `/api/admin/mcp-servers` | 新增注册申请 | admin |
| F-29 | PUT | `/api/admin/mcp-servers/{name}` | 编辑地址、超时、降级话术 | admin |

```ts
interface ToolItem {
  name: string
  code: string
  cat: string
  proto: 'RPC' | 'MCP'
  status: '在线' | '降级'
  timeout: string
  latency: string
  risk: boolean
}

interface ServerItem {
  name: string
  address: string
  protocol: string
  timeout: string
  fallback: string
  status: '在线' | '降级'
}
```

F-25 请求：

```json
{ "method": "POST", "timeout": 800, "params": { "customer_id": "C1029481", "start_date": "2026-11-05", "end_date": "2026-11-06", "page_size": 20 } }
```

响应：

```ts
interface InvokeResult {
  elapsedMs: number
  traceId: string
  response: string   // 原始 JSON 字符串，前端直接展示
}
```

命中 `risk=true` 的写操作时，后端应直接拒绝并返回 403（需审批后才可调试），前端已有同等拦截。

### 3.8 知识库

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-30 | GET | `/api/admin/kb/documents?index=&status=&page=&size=` | 文档列表 | admin、ops |
| F-31 | POST | `/api/admin/kb/documents` | 上传文档（multipart） | admin |
| F-32 | GET | `/api/admin/kb/documents/{id}` | 解析/向量化进度轮询 | admin、ops |
| F-33 | POST | `/api/admin/kb/documents/{id}/retry` | 重新解析失败文档 | admin |
| F-34 | GET | `/api/admin/kb/documents/{id}/chunks` | 切片预览 | admin、ops |
| F-35 | GET | `/api/admin/kb/indexes` | 知识库/索引下拉项 | admin、ops |
| F-36 | POST | `/api/admin/kb/recall` | 召回测试 | admin、ops |
| F-37 | POST | `/api/admin/kb/recall/drafts` | 保存调参草稿（可选） | admin、ops |

```ts
interface DocItem {
  id: string
  name: string
  type: string        // PDF / WORD / CSV...
  size: string
  chunks: number
  status: '已向量化' | '解析中' | '解析失败'
  index: string
  by: string
}
```

F-31 使用 `multipart/form-data`：`file` + `index`；返回 `DocItem`（`status=解析中`），前端再按 F-32 轮询（建议 2s 一次，直到 `已向量化` 或 `解析失败`）。

F-36 请求：

```json
{ "query": "单笔超过 50 万的转账需要什么审批？", "index": "risk_rules_v3", "model": "bge-m3", "hybrid": true, "threshold": 0.72, "topK": 5 }
```

响应：

```ts
interface RecallResponse {
  latencyMs: number
  hits: Array<{
    rank: number
    src: string
    txt: string      // 含 <mark> 高亮
    bm25: number
    knn: number
    score: number
  }>
}
```

阈值过滤由后端按 `threshold` 执行，前端滑杆变化时只重算已有结果条数，重新检索需再次调用 F-36。

### 3.9 计费

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-38 | GET | `/api/admin/billing/summary?month=2026-11` | KPI、部门占比、Agent 占比、对账明细 | admin、ops |
| F-39 | GET | `/api/admin/billing/export?month=2026-11` | 导出对账单 CSV | admin、ops |
| F-40 | POST | `/api/admin/billing/reports` | 生成月度计费报告 | admin、ops |

```ts
interface BillingSummary {
  month: string
  range: string
  kpis: {
    totalTokens: Kpi
    cost: Kpi
    collectionRate: Kpi
    abnormalBills: Kpi
  }
  deptShare: Array<{ key: string; value: number }>   // 百分比
  agentShare: Array<{ label: string; value: number; color?: string }>
  rows: Array<{ dept: string; agent: string; input: string; output: string; amount: string }>
  totals: { input: string; output: string; amount: string }
  rateNote: string
}
```

### 3.10 全链路追踪

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-41 | GET | `/api/admin/traces/{traceId}` | 链路详情：指标、Span 瀑布、原始日志 | admin、ops |
| F-42 | POST | `/api/admin/traces/{traceId}/explain` | AI 解释慢点与降级原因 | admin、ops |

```ts
interface TraceDetail {
  traceId: string
  status: number
  totalMs: number
  metrics: Array<{ key: string; value: string; color?: string }>
  spans: Array<{
    name: string
    offset: number   // 相对起点百分比 0-100
    width: number    // 宽度百分比 0-100
    ms: string
    tone?: '' | 'b-info' | 'b-warn'
    status?: 'ok' | 'degraded' | 'failed'
  }>
  log: string        // 原始多行日志
  aiExplanation?: string
}
```

`POST /explain` 返回 `{ "explanation": "..." }`；前端当前以 Toast + 瀑布重放展示，可逐步改为在面板内渲染。

### 3.11 合规审计

| 编号 | 方法 | 路径 | 用途 | 角色 |
|---|---|---|---|---|
| F-43 | GET | `/api/admin/compliance?type=&page=&size=` | 审计列表，`type` 为 `all`/`high-risk`/`normal` | ops |
| F-44 | GET | `/api/admin/compliance/{id}` | 单条审计详情 | ops |
| F-45 | GET | `/api/admin/compliance/export?days=30` | 导出审计日志 CSV | ops |

```ts
interface ComplianceItem {
  id: string
  time: string
  user: string
  dept: string
  agent: string
  query: string
  guard: string
  result: string
  highRisk: boolean
}
```

`ops` 是唯一可访问 `/audit/compliance` 的角色；`admin` 直接访问应得到 403。

## 4. 与现有 Mock 的对照

| Mock 常量（`src/data/console.ts`） | 对应接口 |
|---|---|
| `ROLES`、`NAV` | F-01、F-02 |
| `TREND_A`、`TREND_B`、`HEALTH`、`TOP_AGENTS`、总览内联 KPI | F-03 |
| 告警页内联 `rows` | F-04 ~ F-06 |
| 应用中心内联 `apps` | F-07 |
| `CHAT_SEEDS`、`REFS`、`TL_STEPS`、`DEPT_BARS` | F-09 ~ F-14 |
| `AGENTS`、`WIZARD_*`、`GUARDS`、`HIGH_RISK_TOOL_IDS` | F-15 ~ F-22 |
| `TOOLS`、`TOOL_CAT_FILTERS` | F-23 ~ F-25 |
| `SERVERS` | F-26 ~ F-29 |
| `DOCS` | F-30 ~ F-35 |
| `RECALL` | F-36 |
| `BILLING_DEPTS`、`BILLING_PARTS`、`BILLING_ROWS` | F-38 ~ F-40 |
| `TRACE_SPANS` | F-41、F-42 |
| `COMPLIANCE`、`COMPLIANCE_FILTERS` | F-43 ~ F-45 |

## 5. 可复用的现有后端接口

| 现有接口 | 服务 | 用途 |
|---|---|---|
| `POST /api/chat/simple` | yp-assistant-engine :8081 | 非流式对话 |
| `POST /api/chat/stream` | yp-assistant-engine :8081 | 流式对话，F-12 直接复用 |
| `POST /api/chat/assistant/simple`、`/api/chat/workflow/simple` | gateway :8080 | 网关版本 |
| `POST /api/rag/chat`、`/api/rag/chat_stream` | yp-assistant-engine :8081 | RAG 对话 |
| `POST /api/search/semantic`、`/api/search/hybrid` | yp-assistant-engine :8081 | 语义/混合检索，可作为 F-36 的底层 |
| `POST /api/knowledge/init`、`/api/knowledge/split_preview` | yp-assistant-engine :8081 | 向量库初始化、切分预览，可作为 F-34 的底层 |
| `POST /mcp`（JSON-RPC） | yp-agent-mcp-hub :8083 | MCP 工具协议，F-25 的底层由平台服务转发，前端不直连 |
| `GET /api/business/risk` | yp-mock-business :8082 | 模拟业务系统，仅由 MCP Server 调用 |

其余 `/api/admin/**` 均为新增，已在 `yp-agent-gateway` 配置对应路由（`yp-agent-admin` 模块承载）：

```yaml
- id: agent-admin-route
  uri: lb://yp-agent-admin    # 管理后台引擎（yp-agent-admin :8085）
  predicates: [ Path=/api/admin/** ]   # 直接透传，不剥离前缀
```

## 6. 落地优先级

| 优先级 | 范围 | 说明 |
|---|---|---|
| P0 | F-01、F-03、F-07、F-09 ~ F-12、F-15 | 让登录、总览、应用中心、对话、Agent 列表脱离 Mock |
| P1 | F-04 ~ F-06、F-13、F-14、F-16 ~ F-27 | 告警处理、参考来源/时间线、向导、工具与 MCP |
| P2 | F-28 ~ F-45 | 知识库、计费、追踪、合规，可在演示环境中保持 Mock |

前端接入建议：新增 `src/api/` 客户端与各模块请求函数，视图层保持现有数据结构不变，仅把 `src/data/console.ts` 的常量替换为接口返回值；`stores/chat.ts` 的模拟流式替换为 F-12 的 SSE 消费，`stores/platform.ts` 的本地增删改写为 F-17 ~ F-33。
