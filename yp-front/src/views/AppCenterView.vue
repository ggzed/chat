<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const auth = useAuthStore()
const ui = useUiStore()

const apps = [
  {
    name: '风控规则助手',
    endpoint: 'rule_qa_v3',
    icon: 'book',
    alt: false,
    desc: '面向业务同学的解释型 Agent：回答授信、反欺诈、大额拦截等规则问题，并附命中条款原文与页码。',
    calls: 4821,
    latency: '0.9s',
    satisfaction: '96%',
    to: '/apps/rule-qa'
  },
  {
    name: '风控数据智算',
    endpoint: 'risk_analytics_v2',
    icon: 'chart',
    alt: true,
    desc: '长链路型 Agent：自动拆解统计任务，调用交易 / 规则接口取数，校验统计口径后生成图表与结论。',
    calls: 1264,
    latency: '4.2s',
    satisfaction: '93%',
    to: '/apps/analytics'
  }
]
</script>

<template>
  <PageHead
    eyebrow="APPS / 智能应用中心"
    title="我的 Agent"
    sub="仅展示当前角色已授权的 Agent。业务员可直接使用，管理员可前往编排中心维护配置。"
  >
    <template #actions>
      <RouterLink v-if="auth.role === 'admin'" to="/agents" class="btn btn-primary">
        <AppIcon name="nodes" :size="14" />前往编排中心
      </RouterLink>
    </template>
  </PageHead>

  <div class="notice" style="margin-bottom: 16px">
    <AppIcon name="shield" :size="15" />
    <span v-if="auth.role === 'sales'">业务员视角下，Agent 配置、工具挂载与知识库均为只读；如需调整请联系智能体平台组。</span>
    <span v-else>管理员视角可在此体验线上 Agent，也可在「智能体编排中心」修改提示词、工具与护栏后重新发布。</span>
  </div>

  <div class="grid g2">
    <article v-for="app in apps" :key="app.name" class="app-card">
      <div class="ac-top">
        <span class="ac-ico" :class="{ alt: app.alt }"><AppIcon :name="app.icon" :size="19" /></span>
        <div style="flex: 1; min-width: 0">
          <h2 class="ac-name">{{ app.name }}</h2>
          <div class="ac-endpoint">{{ app.endpoint }} · 已发布</div>
        </div>
        <span class="tag tag-ok">在线</span>
      </div>
      <p class="ac-desc">{{ app.desc }}</p>
      <div class="ac-stats">
        <div>今日会话<b>{{ app.calls.toLocaleString() }}</b></div>
        <div>平均时延<b>{{ app.latency }}</b></div>
        <div>满意度<b>{{ app.satisfaction }}</b></div>
        <RouterLink :to="app.to" class="btn btn-primary btn-sm" style="margin-left: auto">
          打开 <AppIcon name="arrow" :size="12" />
        </RouterLink>
      </div>
    </article>

    <article class="app-card" style="opacity: 0.72; justify-content: center; text-align: center">
      <span class="ac-ico" style="margin: 0 auto"><AppIcon name="plus" :size="19" /></span>
      <h2 class="ac-name">更多 Agent 即将上线</h2>
      <p class="ac-desc" style="min-height: 0">
        催收合规问答、贷前尽调摘要正在灰度中。上线后将按部门与角色自动授权，无需单独申请。
      </p>
      <button class="btn btn-sm" type="button" @click="ui.toast('已登记关注，上线后会通过飞书通知你', 'ok')">
        关注上线通知
      </button>
    </article>
  </div>
</template>
