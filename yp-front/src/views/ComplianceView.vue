<script setup lang="ts">
import { computed, ref } from 'vue'
import { COMPLIANCE, COMPLIANCE_FILTERS } from '@/data/console'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const ui = useUiStore()

const tabs = [
  { label: 'Token 计费大盘', to: '/audit/billing' },
  { label: '全链路追踪', to: '/audit/trace' },
  { label: '合规审计', to: '/audit/compliance' }
]

const filters = COMPLIANCE_FILTERS
const active = ref('全部')

const rows = computed(() =>
  COMPLIANCE.filter((item) => {
    if (active.value === '高危拦截') return item.highRisk
    if (active.value === '常规对话') return !item.highRisk
    return true
  })
)

function detail(row: (typeof COMPLIANCE)[number]) {
  ui.toast(`${row.time} · ${row.user} · ${row.guard} · ${row.result}（完整记录已写入审计库）`, '')
}

function exportLog() {
  ui.toast('已导出近 30 天合规审计明细（CSV）', 'ok')
}
</script>

<template>
  <PageHead
    eyebrow="AUDIT / 合规审计"
    title="合规审计"
    sub="所有对话与工具调用按操作人留痕，保存期不少于 5 年，可导出供监管检查。"
  >
    <template #actions>
      <button class="btn" type="button" @click="exportLog">
        <AppIcon name="upload" :size="14" />导出审计日志
      </button>
      <span class="tag tag-brand">仅运维/主管可见</span>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <div class="notice warn" style="margin-bottom: 14px">
    <AppIcon name="shield" :size="15" />
    <span>本页仅展示脱敏后的操作摘要；查看包含明文 PII 的原始记录需要二次审批并留痕。</span>
  </div>

  <div class="grid g4" style="margin-bottom: 14px">
    <article class="kpi k-danger">
      <p class="kpi-label">今日高危拦截</p>
      <p class="kpi-value">2<small>次</small></p>
      <div class="kpi-foot"><span class="kpi-delta up">资金操作阻断</span></div>
    </article>
    <article class="kpi k-warn">
      <p class="kpi-label">PII 脱敏</p>
      <p class="kpi-value">1<small>次</small></p>
      <div class="kpi-foot"><span class="kpi-delta flat">身份证号命中</span></div>
    </article>
    <article class="kpi">
      <p class="kpi-label">今日审计事件</p>
      <p class="kpi-value">386<small>条</small></p>
      <div class="kpi-foot"><span class="kpi-delta flat">全部已落盘</span></div>
    </article>
    <article class="kpi k-info">
      <p class="kpi-label">留痕完整率</p>
      <p class="kpi-value">100<small>%</small></p>
      <div class="kpi-foot"><span class="kpi-delta flat">含 TraceID 与操作人</span></div>
    </article>
  </div>

  <section class="panel">
    <header class="panel-head" style="flex-wrap: wrap; gap: 12px">
      <h2 class="panel-title">审计明细</h2>
      <div class="chips">
        <button
          v-for="item in filters"
          :key="item"
          class="chip"
          :class="{ 'is-on': active === item }"
          type="button"
          @click="active = item"
        >
          {{ item }}
        </button>
      </div>
      <span class="panel-meta">{{ rows.length }} EVENTS</span>
    </header>
    <div class="panel-body" style="padding: 0; overflow-x: auto">
      <table class="tbl">
        <thead>
          <tr>
            <th>时间</th>
            <th>操作人</th>
            <th>部门</th>
            <th>Agent</th>
            <th>问题摘要</th>
            <th>护栏</th>
            <th>结果</th>
            <th class="r">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.time">
            <td class="mono" style="font-size: 11px; white-space: nowrap">{{ row.time }}</td>
            <td>{{ row.user }}</td>
            <td style="color: var(--muted)">{{ row.dept }}</td>
            <td>{{ row.agent }}</td>
            <td>
              <span :style="row.highRisk ? 'color: var(--danger); font-weight: 600' : ''">{{ row.query }}</span>
              <span v-if="row.highRisk" class="tag tag-danger" style="margin-left: 6px">高危</span>
            </td>
            <td><span class="tag tag-neutral">{{ row.guard }}</span></td>
            <td>
              <span class="tag" :class="row.result === '正常' ? 'tag-ok' : row.result === '已脱敏' ? 'tag-info' : 'tag-danger'">
                {{ row.result }}
              </span>
            </td>
            <td class="r">
              <button class="btn btn-sm btn-quiet" type="button" @click="detail(row)">查看</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
