<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { AGENT_FILTERS } from '@/data/console'
import type { AgentItem } from '@/data/console'
import { usePlatformStore } from '@/stores/platform'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import AppIcon from '@/components/ui/AppIcon.vue'
import AgentWizard from '@/components/business/AgentWizard.vue'

const platform = usePlatformStore()
const ui = useUiStore()
const route = useRoute()

const tabs = [
  { label: 'Agent 列表', to: '/agents' },
  { label: '工作流编排', to: '/agents/flow' }
]

const filters = AGENT_FILTERS
const active = ref('全部')
const keyword = ref('')
const wizardOpen = ref(false)
const editing = ref<AgentItem | null>(null)

const rows = computed(() =>
  platform.agents.filter((agent) => {
    const matchStatus = active.value === '全部' || agent.status === active.value
    const text = keyword.value.trim().toLowerCase()
    const matchKeyword =
      !text || agent.name.toLowerCase().includes(text) || agent.code.toLowerCase().includes(text)
    return matchStatus && matchKeyword
  })
)

const statusTag: Record<AgentItem['status'], string> = {
  已发布: 'tag-ok',
  草稿: 'tag-warn',
  已下线: 'tag-neutral'
}

function openCreate() {
  editing.value = null
  wizardOpen.value = true
}

function openEdit(agent: AgentItem) {
  editing.value = agent
  wizardOpen.value = true
}

function onPublished(agent: AgentItem) {
  if (editing.value) {
    Object.assign(editing.value, agent)
  } else {
    platform.addAgent(agent)
    ui.toast('已添加到 Agent 列表', 'ok')
  }
}

function closeWizard() {
  wizardOpen.value = false
  editing.value = null
}

function toggleStatus(agent: AgentItem) {
  if (agent.status === '已发布') {
    agent.status = '已下线'
    ui.toast(`「${agent.name}」已下线，端点即刻不可调用`, 'ok')
  } else {
    agent.status = '已发布'
    ui.toast(`「${agent.name}」已重新发布`, 'ok')
  }
}

onMounted(() => {
  if (route.meta.autoWizard) openCreate()
})
</script>

<template>
  <PageHead
    eyebrow="AGENTS / 智能体编排中心"
    title="Agent 列表"
    sub="管理已发布的智能体：配置提示词、知识库、工具与护栏，发布后写入 Nacos。"
  >
    <template #actions>
      <button class="btn" type="button" @click="ui.toast('已导出 Agent 清单（演示）', 'ok')">
        <AppIcon name="upload" :size="14" />导出
      </button>
      <button class="btn btn-primary" type="button" @click="openCreate">
        <AppIcon name="plus" :size="14" />新建 Agent
      </button>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <section class="panel">
    <header class="panel-head" style="flex-wrap: wrap; gap: 12px">
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
      <div class="searchbox" style="margin-left: auto; min-width: 220px">
        <AppIcon name="search" :size="13" />
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索名称 / Code"
          style="border: 0; background: transparent; outline: none; width: 100%; font-size: 11.5px"
          aria-label="搜索 Agent"
        />
      </div>
      <span class="panel-meta">{{ rows.length }} ITEMS</span>
    </header>

    <div class="panel-body" style="padding: 0; overflow-x: auto">
      <table class="tbl">
        <thead>
          <tr>
            <th>Agent</th>
            <th>类型</th>
            <th>模型</th>
            <th class="r">工具</th>
            <th class="r">今日调用</th>
            <th>状态</th>
            <th>负责人</th>
            <th>更新时间</th>
            <th class="r">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="agent in rows" :key="agent.code">
            <td>
              <div class="strong">{{ agent.name }}</div>
              <div class="sub">{{ agent.code }}</div>
            </td>
            <td>
              <span class="tag" :class="agent.type === '长链路型' ? 'tag-info' : 'tag-brand'">{{ agent.type }}</span>
            </td>
            <td class="mono" style="font-size: 11.5px">{{ agent.model }}</td>
            <td class="r num">{{ agent.tools }}</td>
            <td class="r num">{{ agent.calls.toLocaleString() }}</td>
            <td><span class="tag" :class="statusTag[agent.status]">{{ agent.status }}</span></td>
            <td>{{ agent.by }}</td>
            <td class="mono" style="font-size: 11px; color: var(--muted)">{{ agent.time }}</td>
            <td class="r" style="white-space: nowrap">
              <button class="btn btn-sm" type="button" @click="openEdit(agent)">
                <AppIcon name="edit" :size="12" />配置
              </button>
              <button class="btn btn-sm btn-quiet" type="button" @click="toggleStatus(agent)">
                {{ agent.status === '已发布' ? '下线' : '发布' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!rows.length" style="padding: 40px; text-align: center; color: var(--faint)">
        没有符合条件的 Agent，试试调整筛选或新建一个。
      </div>
    </div>
  </section>

  <AgentWizard :open="wizardOpen" :agent="editing" @close="closeWizard" @published="onPublished" />
</template>
