<script setup lang="ts">
import { computed, ref } from 'vue'
import { TOOL_CAT_FILTERS, TOOLS } from '@/data/console'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const auth = useAuthStore()
const ui = useUiStore()

const tabs = [
  { label: '工具分类目录', to: '/hub/tools' },
  { label: 'MCP Server 注册', to: '/hub/servers' },
  { label: '接口调试', to: '/hub/debug' }
]

const filters = TOOL_CAT_FILTERS
const active = ref('全部')
const keyword = ref('')

const tools = computed(() =>
  TOOLS.filter((tool) => {
    const matchCat = active.value === '全部' || tool.cat === active.value
    const text = keyword.value.trim().toLowerCase()
    const matchText = !text || tool.name.toLowerCase().includes(text) || tool.code.toLowerCase().includes(text)
    return matchCat && matchText
  })
)

const readonly = computed(() => auth.role === 'ops')
</script>

<template>
  <PageHead
    eyebrow="HUB / 工具与插件广场"
    title="工具分类目录"
    sub="统一治理 RPC 与 MCP 工具：超时、降级话术与风险等级由平台统一配置。"
  >
    <template #actions>
      <button v-if="!readonly" class="btn btn-primary" type="button" @click="ui.toast('工具注册需在 MCP Server 注册页完成', 'ok')">
        <AppIcon name="plus" :size="14" />注册工具
      </button>
      <span v-else class="tag tag-neutral">只读视角</span>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <div v-if="readonly" class="notice" style="margin-bottom: 14px">
    <AppIcon name="shield" :size="15" />
    <span>运维/主管角色可查看工具目录与健康状态，新增、编辑与调试入口仅管理员可用。</span>
  </div>

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
          placeholder="搜索工具名称 / code"
          style="border: 0; background: transparent; outline: none; width: 100%; font-size: 11.5px"
          aria-label="搜索工具"
        />
      </div>
      <span class="panel-meta">{{ tools.length }} TOOLS</span>
    </header>
    <div class="panel-body">
      <div class="tool-grid">
        <article v-for="tool in tools" :key="tool.code" class="tool-card" :class="{ 'is-risk': tool.risk }">
          <div class="tc-top">
            <span class="tc-ico"><AppIcon :name="tool.risk ? 'alert' : 'zap'" :size="14" /></span>
            <div style="flex: 1; min-width: 0">
              <div class="tc-name">{{ tool.name }}</div>
              <div class="tc-code">{{ tool.code }}</div>
            </div>
            <span class="tag" :class="tool.status === '在线' ? 'tag-ok' : 'tag-warn'">{{ tool.status }}</span>
          </div>
          <div class="chips" style="margin-top: 10px">
            <span class="tag tag-neutral">{{ tool.cat }}</span>
            <span class="tag tag-info">{{ tool.proto }}</span>
            <span v-if="tool.risk" class="tag tag-danger">高危 · 写操作</span>
          </div>
          <div class="tc-meta">
            <div>超时<b>{{ tool.timeout }}</b></div>
            <div>P95 时延<b>{{ tool.latency }}</b></div>
            <div style="margin-left: auto; align-self: flex-end">
              <button
                class="btn btn-sm btn-quiet"
                type="button"
                @click="ui.toast(readonly ? '只读角色不可调试工具' : `已打开 ${tool.name} 的调试面板`, readonly ? 'err' : '')"
              >
                {{ readonly ? '查看' : '调试' }}
              </button>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>
