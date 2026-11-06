<script setup lang="ts">
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import PanelBox from '@/components/ui/PanelBox.vue'
import AppIcon from '@/components/ui/AppIcon.vue'
import FlowCanvas from '@/components/business/FlowCanvas.vue'

const ui = useUiStore()

const tabs = [
  { label: 'Agent 列表', to: '/agents' },
  { label: '工作流编排', to: '/agents/flow' }
]

const nodes = [
  { name: '意图解析', desc: '判断问题属于问答、统计还是操作类，决定后续分支', tag: 'LLM' },
  { name: '知识检索', desc: '混合检索知识库，注入规则原文与页码', tag: 'RAG' },
  { name: '工具调用', desc: '按计划调用 MCP / RPC 工具，写操作需护栏放行', tag: 'MCP' },
  { name: '护栏校验', desc: '敏感词、PII、资金类阻断，全部写入审计日志', tag: 'GUARD' }
]
</script>

<template>
  <PageHead
    eyebrow="AGENTS / 工作流编排"
    title="工作流概念视图"
    sub="当前版本以配置式编排为主，画布用于说明节点关系，拖拽编排将在后续版本开放。"
  >
    <template #actions>
      <button class="btn" type="button" @click="ui.toast('画布为概念示意，暂不支持拖拽保存', 'err')">
        <AppIcon name="eye" :size="14" />只读预览
      </button>
      <button class="btn btn-primary" type="button" @click="ui.toast('已加入画布内测名单', 'ok')">
        <AppIcon name="flow" :size="14" />申请内测
      </button>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <div class="notice" style="margin-bottom: 14px">
    <AppIcon name="shield" :size="15" />
    <span>概念稿：展示问答型与长链路型 Agent 的分支路径，帮助理解配置项之间的依赖关系，不参与实际运行。</span>
  </div>

  <PanelBox title="节点关系" meta="CONCEPT CANVAS">
    <FlowCanvas />
    <div class="flow-legend">
      <span><i style="background: #e5f0ea; border: 1px solid #125d4b"></i>LLM 节点</span>
      <span><i style="background: #ecf2f8; border: 1px solid #3a5f8a"></i>检索节点</span>
      <span><i style="background: #fbf3e4; border: 1px solid #9a5b06"></i>工具节点</span>
      <span><i style="background: #fcf0ee; border: 1px solid #b42318"></i>护栏节点</span>
      <span><i style="background: #e9f4ec; border: 1px solid #187a50"></i>输出节点</span>
    </div>
  </PanelBox>

  <div class="grid g2 mt14">
    <PanelBox title="节点说明" meta="4 NODES">
      <div v-for="node in nodes" :key="node.name" class="health-row">
        <span class="tag tag-brand">{{ node.tag }}</span>
        <span class="h-name">
          <b style="display: block; font-size: 12px">{{ node.name }}</b>
          <span style="font-size: 11px; color: var(--muted)">{{ node.desc }}</span>
        </span>
      </div>
    </PanelBox>

    <PanelBox title="设计约束" meta="GOVERNANCE">
      <div class="summary-box">
        <div class="summary-row"><span class="sk">发布方式</span><span class="sv">配置式编排 + Nacos 热更新</span></div>
        <div class="summary-row"><span class="sk">高危工具</span><span class="sv">必须启用「高危阻断」护栏</span></div>
        <div class="summary-row"><span class="sk">调用留痕</span><span class="sv">TraceID 贯穿网关 → 工具 → LLM</span></div>
        <div class="summary-row"><span class="sk">计费口径</span><span class="sv">按归属部门归集输入/输出 Token</span></div>
      </div>
      <p class="hint" style="margin-top: 10px">如需新增自定义节点（条件路由、并行聚合），请在需求池提报并注明审批流程。</p>
    </PanelBox>
  </div>
</template>
