<script setup lang="ts">
import { computed, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import KpiCard from '@/components/ui/KpiCard.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const auth = useAuthStore()
const ui = useUiStore()

interface AlertRow {
  time: string
  level: 'P1' | 'P2' | 'P3'
  title: string
  service: string
  impact: string
  state: '未处理' | '处理中' | '已恢复'
}

const rows = ref<AlertRow[]>([
  { time: '11-06 19:42', level: 'P1', title: 'risk-rpc-core 响应超时率升至 4.2%', service: 'risk-rpc-core', impact: '数据智算降级，影响 12 个会话', state: '未处理' },
  { time: '11-06 18:10', level: 'P2', title: 'alert-notify-mcp 连续 3 次推送失败', service: 'alert-notify-mcp', impact: '预警通知延迟，重试队列积压', state: '处理中' },
  { time: '11-06 14:05', level: 'P3', title: 'es-vector-service 检索 P99 升至 210ms', service: 'es-vector-service', impact: 'RAG 召回轻微抖动，无用户感知', state: '已恢复' },
  { time: '11-05 22:31', level: 'P2', title: 'credit-bureau-mcp 外部征信接口超时', service: 'credit-bureau-mcp', impact: '征信查询转人工复核 8 笔', state: '已恢复' }
])

const filters = ['全部', '未处理', '处理中', '已恢复']
const active = ref('全部')

const filtered = computed(() =>
  active.value === '全部' ? rows.value : rows.value.filter((row) => row.state === active.value)
)

const levelTag = { P1: 'tag-danger', P2: 'tag-warn', P3: 'tag-info' } as const
const stateTag = { 未处理: 'tag-danger', 处理中: 'tag-warn', 已恢复: 'tag-ok' } as const
const canHandle = computed(() => auth.role !== 'ops')

function handle(row: AlertRow) {
  if (!canHandle.value) {
    ui.toast('运维/主管角色仅可查看告警，处理请转管理员', 'err')
    return
  }
  row.state = '处理中'
  ui.toast('已认领该告警，并通知值班同学', 'ok')
}

function markAllRead() {
  ui.toast('已全部标记为已读', 'ok')
}
</script>

<template>
  <PageHead eyebrow="DASHBOARD / 告警提示" title="告警与异常" sub="按严重级别收敛，P1 自动触发降级与值班通知。">
    <template #actions>
      <button class="btn" type="button" @click="markAllRead">全部标记已读</button>
      <button class="btn btn-primary" type="button" @click="ui.toast('已跳转值班排班表（演示）', 'ok')">查看值班</button>
    </template>
  </PageHead>

  <div class="grid g4">
    <KpiCard label="活跃告警" value="2" delta="1 条 P1" trend="up" variant="danger" :data="[1, 2, 2, 3, 2, 2, 1, 2, 2]" />
    <KpiCard label="今日新增" value="4" delta="较昨日 -2" trend="down" variant="warn" :data="[6, 5, 5, 4, 5, 4, 4, 3, 4]" />
    <KpiCard label="平均恢复时长" value="18" unit="min" delta="-31%" trend="down" :data="[30, 28, 26, 24, 22, 21, 20, 19, 18]" />
    <KpiCard label="自动降级次数" value="7" delta="全部成功" trend="flat" variant="info" :data="[4, 5, 6, 5, 6, 7, 6, 7, 7]" />
  </div>

  <section class="panel mt14">
    <header class="panel-head" style="flex-wrap: wrap; gap: 12px">
      <h2 class="panel-title">告警列表</h2>
      <div class="chips" style="margin-left: 8px">
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
      <span class="panel-meta">共 {{ filtered.length }} 条</span>
    </header>
    <div class="panel-body" style="padding: 0; overflow-x: auto">
      <table class="tbl">
        <thead>
          <tr>
            <th>时间</th>
            <th>级别</th>
            <th>事件</th>
            <th>影响服务</th>
            <th>影响范围</th>
            <th>状态</th>
            <th class="r">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in filtered" :key="row.time">
            <td class="mono" style="white-space: nowrap">{{ row.time }}</td>
            <td><span class="tag" :class="levelTag[row.level]">{{ row.level }}</span></td>
            <td class="strong">{{ row.title }}</td>
            <td class="mono" style="font-size: 11.5px">{{ row.service }}</td>
            <td>{{ row.impact }}</td>
            <td><span class="tag" :class="stateTag[row.state]">{{ row.state }}</span></td>
            <td class="r">
              <button class="btn btn-sm" type="button" @click="handle(row)">处理</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>

  <div class="notice mt14">
    <AppIcon name="shield" :size="15" />
    <span>P1 告警会同时通知值班主管与企业微信机器人；运维角色只读，不可直接修改告警状态。</span>
  </div>
</template>
