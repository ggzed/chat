<script setup lang="ts">
import { computed, ref } from 'vue'
import { TOOLS } from '@/data/console'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import PanelBox from '@/components/ui/PanelBox.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const ui = useUiStore()

const tabs = [
  { label: '工具分类目录', to: '/hub/tools' },
  { label: 'MCP Server 注册', to: '/hub/servers' },
  { label: '接口调试', to: '/hub/debug' }
]

const tool = ref('queryTransaction')
const method = ref('POST')
const timeout = ref(800)
const params = ref(`{
  "customer_id": "C1029481",
  "start_date": "2026-11-05",
  "end_date": "2026-11-06",
  "page_size": 20
}`)
const loading = ref(false)
const response = ref('')
const elapsed = ref(0)

const current = computed(() => TOOLS.find((item) => item.code === tool.value))
const risk = computed(() => Boolean(current.value?.risk))

function send() {
  if (risk.value) {
    ui.toast('高危写操作需审批通过后才能调试，已拦截本次请求', 'err')
    return
  }
  loading.value = true
  response.value = ''
  const start = performance.now()
  window.setTimeout(() => {
    elapsed.value = Math.round(performance.now() - start)
    response.value = `{
  "code": 0,
  "message": "success",
  "trace_id": "tr_7f3a9c21",
  "data": {
    "total": 12,
    "items": [
      {
        "txn_id": "TXN2026110600231",
        "amount": 128400.00,
        "channel": "mobile_bank",
        "risk_level": "high",
        "created_at": "2026-11-06T18:22:41+08:00"
      },
      {
        "txn_id": "TXN2026110600198",
        "amount": 46800.00,
        "channel": "counter",
        "risk_level": "normal",
        "created_at": "2026-11-06T17:05:12+08:00"
      }
    ]
  }
}`
    loading.value = false
    ui.toast(`调用成功 · ${elapsed.value}ms · trace_id tr_7f3a9c21`, 'ok')
  }, 900)
}
</script>

<template>
  <PageHead
    eyebrow="HUB / 接口调试"
    title="RPC / MCP 调试"
    sub="调试请求会以你的身份鉴权并计入审计日志；高危写操作默认拦截。"
  >
    <template #actions>
      <span class="tag tag-brand">仅管理员</span>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <div class="grid g2">
    <PanelBox title="请求参数" meta="REQUEST">
      <div class="field">
        <label class="field-label" for="debug-tool">选择工具</label>
        <select id="debug-tool" v-model="tool" class="select">
          <option v-for="item in TOOLS" :key="item.code" :value="item.code">
            {{ item.name }} · {{ item.code }}
          </option>
        </select>
      </div>

      <div class="split-2">
        <div class="field">
          <label class="field-label" for="debug-method">调用方式</label>
          <select id="debug-method" v-model="method" class="select">
            <option>POST</option>
            <option>GET</option>
            <option>MCP / tool-call</option>
          </select>
        </div>
        <div class="field">
          <label class="field-label" for="debug-timeout">超时（ms）</label>
          <input id="debug-timeout" v-model.number="timeout" class="input num" type="number" min="100" max="5000" step="100" />
        </div>
      </div>

      <div class="field">
        <label class="field-label" for="debug-params">请求参数（JSON）</label>
        <textarea id="debug-params" v-model="params" class="textarea mono" style="min-height: 190px; font-size: 11.5px"></textarea>
      </div>

      <div v-if="risk" class="notice warn" style="margin-bottom: 14px">
        <AppIcon name="alert" :size="15" />
        <span>当前工具为高危写操作，调试请求将被护栏拦截，需走审批后由平台组代为执行。</span>
      </div>

      <div style="display: flex; gap: 8px; align-items: center">
        <button class="btn btn-primary" type="button" :disabled="loading" @click="send">
          <AppIcon name="play" :size="14" />{{ loading ? '调用中…' : '发送请求' }}
        </button>
        <button class="btn" type="button" @click="response = ''">清空结果</button>
        <span class="hint" style="margin: 0">trace_id 将回写到全链路追踪</span>
      </div>
    </PanelBox>

    <PanelBox title="响应结果" :meta="response ? `${elapsed} MS` : 'RESPONSE'">
      <div v-if="loading" style="padding: 6px 0">
        <div class="skeleton" style="width: 32%"></div>
        <div class="skeleton" style="width: 64%"></div>
        <div class="skeleton" style="width: 48%"></div>
        <div class="skeleton" style="width: 78%"></div>
        <div class="skeleton" style="width: 56%"></div>
        <div class="skeleton" style="width: 40%; margin-bottom: 0"></div>
      </div>
      <div v-else-if="response" class="code-box" style="min-height: 360px">{{ response }}</div>
      <div v-else style="min-height: 360px; display: grid; place-items: center; color: var(--faint); font-size: 12px; text-align: center">
        <div>
          <AppIcon name="code" :size="26" style="margin: 0 auto 10px; color: var(--line-2)" />
          发送请求后，这里会展示结构化响应与耗时。
        </div>
      </div>
    </PanelBox>
  </div>
</template>
