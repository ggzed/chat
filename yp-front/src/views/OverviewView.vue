<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { HEALTH, TOP_AGENTS, TREND_A, TREND_B } from '@/data/console'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PanelBox from '@/components/ui/PanelBox.vue'
import KpiCard from '@/components/ui/KpiCard.vue'
import AreaChart from '@/components/ui/charts/AreaChart.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const auth = useAuthStore()
const ui = useUiStore()
const router = useRouter()

const isSales = computed(() => auth.role === 'sales')

const alerts = [
  { level: 'P1', text: 'risk-rpc-core 响应超时率升至 4.2%，已自动降级', time: '19:42' },
  { level: 'P2', text: 'alert-notify-mcp 连续 3 次推送失败，重试队列积压', time: '18:10' }
]

const quickLinks = computed(() =>
  isSales.value
    ? [
        { title: '我的 Agent', desc: '查看已授权智能体并直接使用', icon: 'chat', to: '/apps' },
        { title: '风控规则助手', desc: '规则解释与条款原文检索', icon: 'book', to: '/apps/rule-qa' },
        { title: '风控数据智算', desc: '用自然语言发起统计任务', icon: 'chart', to: '/apps/analytics' }
      ]
    : [
        { title: '新建对话', desc: '基于已发布 Agent 发起一次任务', icon: 'chat', to: '/apps' },
        { title: '召回测试', desc: '验证知识库检索效果与阈值', icon: 'search', to: '/kb/recall' },
        { title: '全链路追踪', desc: '按 TraceID 回放一次调用', icon: 'trace', to: '/audit/trace' }
      ]
)
</script>

<template>
  <PageHead
    eyebrow="DASHBOARD / 系统总览"
    title="今日运行总览"
    :sub="`2026-11-06 · 数据每 60 秒刷新 · 当前视角：${auth.user.name}`"
  >
    <template #actions>
      <RouterLink v-if="isSales" to="/apps" class="btn btn-primary">
        <AppIcon name="chat" :size="14" />进入我的 Agent
      </RouterLink>
      <RouterLink v-else to="/dashboard/alerts" class="btn btn-primary">
        <AppIcon name="alert" :size="14" />查看全部告警
      </RouterLink>
      <button class="btn" type="button" @click="ui.toast('已触发数据刷新', 'ok')">
        <AppIcon name="refresh" :size="14" />刷新
      </button>
    </template>
  </PageHead>

  <div v-if="!isSales" class="alert-strip">
    <div class="a-ico"><AppIcon name="alert" :size="18" /></div>
    <div class="a-list">
      <div v-for="alert in alerts" :key="alert.text" class="alert-row">
        <span class="tag tag-danger">{{ alert.level }}</span>
        <b>{{ alert.text }}</b>
        <span class="a-time">{{ alert.time }}</span>
      </div>
    </div>
    <RouterLink to="/dashboard/alerts" class="btn btn-sm btn-ghost">处理</RouterLink>
  </div>

  <div class="grid g4">
    <KpiCard label="今日调用量" value="248,930" delta="+6.3% 较昨日" trend="up" :data="[12, 18, 15, 22, 28, 31, 26, 35, 41]" />
    <KpiCard
      label="平均响应时延"
      value="1.42"
      unit="s"
      delta="-12.8% 较昨日"
      trend="down"
      variant="info"
      :data="[28, 26, 24, 22, 20, 18, 17, 16, 15]"
    />
    <KpiCard
      label="今日 Token 消耗"
      value="1,846,300"
      delta="+4.1% 较昨日"
      trend="up"
      variant="warn"
      :data="[18, 22, 25, 24, 29, 33, 36, 40, 42]"
    />
    <KpiCard
      label="错误率"
      value="0.82"
      unit="%"
      delta="+0.11% 较昨日"
      trend="up"
      variant="danger"
      :data="[8, 7, 9, 6, 5, 4, 6, 7, 9]"
    />
  </div>

  <div class="grid g-main mt14">
    <PanelBox title="24 小时调用趋势" meta="CALLS / HOUR">
      <AreaChart :primary="TREND_A" :secondary="TREND_B" />
    </PanelBox>

    <PanelBox title="服务健康度" meta="6 SERVICES">
      <div v-for="item in HEALTH" :key="item.name" class="health-row">
        <span class="dot" :class="item.status === 'warn' ? 'dot-warn' : 'dot-ok'"></span>
        <span class="h-name">{{ item.name }}</span>
        <span class="health-track"><span class="health-fill" :style="{ width: `${item.width}%`, background: item.status === 'warn' ? 'var(--warn)' : 'var(--brand)' }"></span></span>
        <span class="h-lat">{{ item.latency }}</span>
      </div>
      <div class="notice mt14">
        <AppIcon name="shield" :size="15" />
        <span>整体可用性 99.97%，过去 24 小时无 P0 故障。</span>
      </div>
    </PanelBox>
  </div>

  <div class="grid g3 mt14">
    <PanelBox title="Agent 调用排行" meta="TOP 5">
      <div v-for="agent in TOP_AGENTS" :key="agent.name" class="barline">
        <span class="bl-name" :title="agent.name">{{ agent.name }}</span>
        <span class="bl-track"><span class="bl-fill" :style="{ width: `${agent.width}%` }"></span></span>
        <span class="bl-val">{{ agent.value.toLocaleString() }}</span>
      </div>
    </PanelBox>

    <PanelBox v-if="!isSales" title="最近告警" meta="2 UNREAD">
      <div v-for="alert in alerts" :key="alert.text" class="health-row">
        <span class="tag tag-danger">{{ alert.level }}</span>
        <span class="h-name">{{ alert.text }}</span>
        <span class="h-lat">{{ alert.time }}</span>
      </div>
      <RouterLink to="/dashboard/alerts" class="btn btn-sm btn-quiet" style="margin-top: 10px">
        查看全部 <AppIcon name="arrow" :size="12" />
      </RouterLink>
    </PanelBox>

    <PanelBox title="快捷入口" meta="SHORTCUTS">
      <button
        v-for="link in quickLinks"
        :key="link.title"
        type="button"
        class="switch-row"
        style="width: 100%; border: 0; background: transparent; text-align: left; cursor: pointer"
        @click="router.push(link.to)"
      >
        <span style="display: flex; align-items: center; gap: 9px">
          <span class="tc-ico"><AppIcon :name="link.icon" :size="14" /></span>
          <span>
            <span class="sw-t" style="display: block">{{ link.title }}</span>
            <span class="sw-d">{{ link.desc }}</span>
          </span>
        </span>
        <AppIcon name="arrow" :size="14" />
      </button>
    </PanelBox>
  </div>
</template>
