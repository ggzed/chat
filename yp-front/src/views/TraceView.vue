<script setup lang="ts">
import { ref } from 'vue'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import PanelBox from '@/components/ui/PanelBox.vue'
import AppIcon from '@/components/ui/AppIcon.vue'
import TraceWaterfall from '@/components/business/TraceWaterfall.vue'

const ui = useUiStore()

const tabs = [
  { label: 'Token 计费大盘', to: '/audit/billing' },
  { label: '全链路追踪', to: '/audit/trace' },
  { label: '合规审计', to: '/audit/compliance' }
]

const traceId = ref('tr_7f3a9c21')
const loading = ref(false)
const waterfall = ref<InstanceType<typeof TraceWaterfall> | null>(null)

function query() {
  if (!traceId.value.trim()) {
    ui.toast('请输入 TraceID', 'err')
    return
  }
  loading.value = true
  window.setTimeout(() => {
    loading.value = false
    waterfall.value?.replay()
    ui.toast('已加载链路详情', 'ok')
  }, 600)
}

function replayWithAi() {
  loading.value = true
  window.setTimeout(() => {
    loading.value = false
    waterfall.value?.replay()
    ui.toast('AI 回放已开始：正在解释慢点与降级原因', 'ok')
  }, 800)
}

const metrics = [
  { key: '总耗时', value: '1,274 ms', width: 100, color: 'var(--brand)' },
  { key: 'LLM 推理', value: '482 ms', width: 38, color: 'var(--brand)' },
  { key: 'MCP 工具', value: '318 ms', width: 25, color: 'var(--warn)' },
  { key: 'RAG 检索', value: '98 ms', width: 8, color: 'var(--info)' }
]

const log = `[19:42:11.203] gateway            trace=tr_7f3a9c21 user=linlan role=admin
[19:42:11.277] agent-orchestrator intent=analytics plan=5_steps
[19:42:11.375] rag.search         index=risk_rules_v3 hits=4 elapsed=98ms
[19:42:11.857] llm.invoke         model=Qwen2.5-72B tokens_in=2861 tokens_out=742
[19:42:12.175] mcp.queryJustControl server=risk-rpc-core status=DEGRADED retry=1
[19:42:12.493] rpc.aggregate      rows=1284 groups=4 elapsed=161ms
[19:42:12.571] guardrail.check    sensitive=pass pii=pass block=pass
[19:42:12.649] response           status=200 audit=written`
</script>

<template>
  <PageHead
    eyebrow="AUDIT / 全链路追踪"
    title="调用链路回放"
    sub="TraceID 贯穿网关、编排、RAG、LLM 与工具调用，可定位时延毛刺与降级来源。"
  >
    <template #actions>
      <span class="tag tag-ok">本次调用成功</span>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <section class="panel">
    <div class="panel-body">
      <div class="ci-row" style="align-items: center; flex-wrap: wrap">
        <div class="searchbox" style="flex: 1; min-width: 260px; padding: 8px 12px">
          <AppIcon name="trace" :size="14" />
          <input
            v-model="traceId"
            type="text"
            aria-label="TraceID"
            placeholder="输入 TraceID，例如 tr_7f3a9c21"
            style="border: 0; background: transparent; outline: none; width: 100%; font-size: 12.5px"
            @keydown.enter="query"
          />
        </div>
        <button class="btn btn-primary" type="button" :disabled="loading" @click="query">
          <AppIcon name="search" :size="14" />{{ loading ? '查询中…' : '查询' }}
        </button>
        <button class="btn" type="button" :disabled="loading" @click="replayWithAi">
          <AppIcon name="zap" :size="14" />AI 回放
        </button>
      </div>

      <div class="chips" style="margin-top: 12px">
        <span class="tag tag-brand">Agent · 风控数据智算</span>
        <span class="tag tag-info">模型 · Qwen2.5-72B</span>
        <span class="tag tag-neutral">部门 · 智能体平台组</span>
        <span class="tag tag-neutral">trace_id · {{ traceId }}</span>
        <span class="tag tag-warn">包含 1 次降级重试</span>
      </div>
    </div>
  </section>

  <div class="grid g-main mt14">
    <PanelBox title="耗时瀑布" meta="1,274 MS">
      <div v-if="loading" style="padding: 10px 0">
        <div class="skeleton" style="width: 92%"></div>
        <div class="skeleton" style="width: 68%"></div>
        <div class="skeleton" style="width: 76%"></div>
        <div class="skeleton" style="width: 54%"></div>
        <div class="skeleton" style="width: 62%"></div>
      </div>
      <TraceWaterfall v-else ref="waterfall" />
      <div style="display: flex; gap: 8px; margin-top: 12px; align-items: center">
        <button class="btn btn-sm" type="button" @click="waterfall?.replay()">
          <AppIcon name="refresh" :size="12" />重新播放
        </button>
        <span class="hint" style="margin: 0">黄色为降级重试，已自动 fallback 到本地缓存</span>
      </div>
    </PanelBox>

    <PanelBox title="耗时构成" meta="BREAKDOWN">
      <div v-for="item in metrics" :key="item.key" class="barline">
        <span class="bl-name">{{ item.key }}</span>
        <span class="bl-track"><span class="bl-fill" :style="{ width: `${item.width}%`, background: item.color }"></span></span>
        <span class="bl-val">{{ item.value }}</span>
      </div>
      <div class="notice warn mt14">
        <AppIcon name="alert" :size="15" />
        <span>risk-rpc-core 处于降级状态，工具调用重试 1 次，额外增加 158ms；建议关注该服务恢复情况。</span>
      </div>
    </PanelBox>
  </div>

  <PanelBox title="结构化日志" meta="AUDIT LOG" class="mt14">
    <div class="code-box">{{ log }}</div>
  </PanelBox>
</template>
