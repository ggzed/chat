<script setup lang="ts">
/** 概念演示画布：展示一次问答型 Agent 的节点与分支，不含拖拽交互。 */
</script>

<template>
  <div class="flow-canvas">
    <svg viewBox="0 0 900 390" role="img" aria-label="Agent 工作流概念图">
      <defs>
        <marker id="arrow" markerWidth="7" markerHeight="7" refX="6" refY="3.5" orient="auto">
          <path d="M0,0 L7,3.5 L0,7 Z" fill="#8a948c" />
        </marker>
      </defs>

      <g font-family="PingFang SC, sans-serif">
        <rect x="24" y="164" width="112" height="58" rx="8" fill="#eef1ea" stroke="#c9d2c7" />
        <text x="80" y="188" text-anchor="middle" font-size="12" fill="#3a443d" font-weight="600">用户输入</text>
        <text x="80" y="205" text-anchor="middle" font-size="9.5" fill="#8a948c" font-family="ui-monospace, monospace">user_query</text>

        <rect x="188" y="150" width="126" height="86" rx="8" fill="#e5f0ea" stroke="#125d4b" />
        <text x="251" y="178" text-anchor="middle" font-size="12" fill="#0b4235" font-weight="600">意图解析</text>
        <text x="251" y="195" text-anchor="middle" font-size="9.5" fill="#5f6a62">问答 / 统计 分流</text>
        <text x="251" y="212" text-anchor="middle" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">LLM · 126ms</text>

        <rect x="366" y="78" width="126" height="76" rx="8" fill="#ecf2f8" stroke="#3a5f8a" />
        <text x="429" y="104" text-anchor="middle" font-size="12" fill="#3a5f8a" font-weight="600">知识检索</text>
        <text x="429" y="121" text-anchor="middle" font-size="9.5" fill="#5f6a62">RAG · 混合检索</text>
        <text x="429" y="138" text-anchor="middle" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">risk_rules_v3</text>

        <rect x="366" y="214" width="126" height="76" rx="8" fill="#fbf3e4" stroke="#9a5b06" />
        <text x="429" y="240" text-anchor="middle" font-size="12" fill="#9a5b06" font-weight="600">工具调用</text>
        <text x="429" y="257" text-anchor="middle" font-size="9.5" fill="#5f6a62">MCP / RPC</text>
        <text x="429" y="274" text-anchor="middle" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">queryJustControl</text>

        <rect x="548" y="146" width="126" height="86" rx="8" fill="#e5f0ea" stroke="#125d4b" />
        <text x="611" y="174" text-anchor="middle" font-size="12" fill="#0b4235" font-weight="600">LLM 汇总</text>
        <text x="611" y="191" text-anchor="middle" font-size="9.5" fill="#5f6a62">口径校验 + 生成</text>
        <text x="611" y="208" text-anchor="middle" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">DeepSeek-V3</text>

        <rect x="728" y="118" width="126" height="70" rx="8" fill="#fcf0ee" stroke="#b42318" />
        <text x="791" y="143" text-anchor="middle" font-size="12" fill="#b42318" font-weight="600">护栏校验</text>
        <text x="791" y="160" text-anchor="middle" font-size="9.5" fill="#5f6a62">敏感词 / PII / 阻断</text>
        <text x="791" y="176" text-anchor="middle" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">guardrails</text>

        <rect x="728" y="228" width="126" height="58" rx="8" fill="#e9f4ec" stroke="#187a50" />
        <text x="791" y="252" text-anchor="middle" font-size="12" fill="#187a50" font-weight="600">响应输出</text>
        <text x="791" y="269" text-anchor="middle" font-size="9.5" fill="#5f6a62">附引用 + 审计日志</text>

        <path d="M136 193 L182 193" stroke="#8a948c" stroke-width="1.4" marker-end="url(#arrow)" />
        <path d="M314 176 C338 176 340 116 362 116" stroke="#8a948c" stroke-width="1.4" fill="none" marker-end="url(#arrow)" />
        <path d="M314 210 C338 210 340 252 362 252" stroke="#8a948c" stroke-width="1.4" fill="none" marker-end="url(#arrow)" />
        <path d="M492 116 C518 116 522 174 544 174" stroke="#8a948c" stroke-width="1.4" fill="none" marker-end="url(#arrow)" />
        <path d="M492 252 C518 252 522 204 544 204" stroke="#8a948c" stroke-width="1.4" fill="none" marker-end="url(#arrow)" />
        <path d="M674 174 L722 174" stroke="#8a948c" stroke-width="1.4" marker-end="url(#arrow)" />
        <path d="M791 188 L791 222" stroke="#8a948c" stroke-width="1.4" marker-end="url(#arrow)" />

        <text x="338" y="108" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">问答型</text>
        <text x="338" y="268" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">长链路型</text>
        <text x="698" y="168" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">通过</text>
        <text x="798" y="210" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">拦截→兜底</text>
      </g>
    </svg>
  </div>
</template>
