<script setup lang="ts">
import { BILLING_DEPTS, BILLING_PARTS, BILLING_ROWS } from '@/data/console'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import PanelBox from '@/components/ui/PanelBox.vue'
import KpiCard from '@/components/ui/KpiCard.vue'
import BarChart from '@/components/ui/charts/BarChart.vue'
import DonutChart from '@/components/ui/charts/DonutChart.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const ui = useUiStore()

const tabs = [
  { label: 'Token 计费大盘', to: '/audit/billing' },
  { label: '全链路追踪', to: '/audit/trace' },
  { label: '合规审计', to: '/audit/compliance' }
]
</script>

<template>
  <PageHead
    eyebrow="AUDIT / 运营与审计"
    title="Token 计费大盘"
    sub="按部门与 Agent 归集输入/输出 Token，与财务月度对账口径保持一致。"
  >
    <template #actions>
      <button class="btn" type="button" @click="ui.toast('已导出 11 月对账单（CSV）', 'ok')">
        <AppIcon name="upload" :size="14" />导出对账单
      </button>
      <button class="btn btn-primary" type="button" @click="ui.toast('已生成月度计费报告（演示）', 'ok')">
        <AppIcon name="chart" :size="14" />生成月报
      </button>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <div class="grid g4">
    <KpiCard label="本月 Token 总量" value="18.42" unit="亿" delta="+5.2% 环比" trend="up" :data="[12, 13, 14, 14, 16, 17, 17, 18, 18]" />
    <KpiCard
      label="折算成本"
      value="¥4,382"
      delta="-2.1% 环比"
      trend="down"
      variant="info"
      :data="[46, 45, 45, 44, 44, 43, 44, 43, 43]"
    />
    <KpiCard label="部门归集率" value="100" unit="%" delta="无未归集账单" trend="flat" variant="warn" :data="[90, 92, 95, 96, 98, 99, 100, 100, 100]" />
    <KpiCard label="异常账单" value="0" unit="笔" delta="阈值内" trend="down" variant="danger" :data="[4, 3, 2, 2, 1, 1, 0, 0, 0]" />
  </div>

  <div class="grid g-main mt14">
    <PanelBox title="部门 Token 消耗占比" meta="SHARE %">
      <BarChart :items="BILLING_DEPTS" :height="200" unit="%" />
      <div class="legend">
        <span><i style="background: #125d4b"></i>输入 + 输出合计</span>
        <span>零售业务部占比 38%，环比 +4.1%</span>
      </div>
    </PanelBox>

    <PanelBox title="Agent 消耗占比" meta="TOP AGENTS">
      <DonutChart :parts="BILLING_PARTS" caption="风控数据智算消耗最高，主要来自长链路工具调用。" />
      <div class="notice mt14">
        <AppIcon name="coins" :size="15" />
        <span>计费口径：输入 Token × 0.0008 + 输出 Token × 0.0024（元 / 千 Token）。</span>
      </div>
    </PanelBox>
  </div>

  <section class="panel mt14">
    <header class="panel-head">
      <h2 class="panel-title">对账明细</h2>
      <span class="panel-meta">2026-11-01 ~ 2026-11-06</span>
    </header>
    <div class="panel-body" style="padding: 0; overflow-x: auto">
      <table class="tbl">
        <thead>
          <tr>
            <th>部门</th>
            <th>主要 Agent</th>
            <th class="r">输入 Token</th>
            <th class="r">输出 Token</th>
            <th class="r">金额</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in BILLING_ROWS" :key="row.dept">
            <td class="strong">{{ row.dept }}</td>
            <td>{{ row.agent }}</td>
            <td class="r num">{{ row.input }}</td>
            <td class="r num">{{ row.output }}</td>
            <td class="r num strong">{{ row.amount }}</td>
          </tr>
        </tbody>
        <tfoot>
          <tr style="background: var(--rail)">
            <td class="strong">合计</td>
            <td>5 个部门</td>
            <td class="r num strong">3,664,600</td>
            <td class="r num strong">1,330,500</td>
            <td class="r num strong">¥3,842.60</td>
          </tr>
        </tfoot>
      </table>
    </div>
  </section>

  <p class="hint mt14">对账单次月 3 日前推送财务系统；如对金额有疑问，请在「合规审计」中按 TraceID 回溯原始调用。</p>
</template>
