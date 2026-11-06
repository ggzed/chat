<script setup lang="ts">
import { computed } from 'vue'
import { SERVERS } from '@/data/console'
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

const readonly = computed(() => auth.role === 'ops')

function testConnection(name: string) {
  if (readonly.value) {
    ui.toast('只读角色不可执行连通性测试', 'err')
    return
  }
  ui.toast(`${name} 连通性正常 · 握手耗时 12ms`, 'ok')
}
</script>

<template>
  <PageHead
    eyebrow="HUB / MCP SERVER"
    title="MCP Server 注册"
    sub="登记 RPC / MCP 服务地址、超时与降级话术；降级话术会在工具不可用时直接回复给用户。"
  >
    <template #actions>
      <button v-if="!readonly" class="btn btn-primary" type="button" @click="ui.toast('新增注册需平台组审批，已打开申请单（演示）', 'ok')">
        <AppIcon name="plus" :size="14" />新增注册
      </button>
      <span v-else class="tag tag-neutral">只读视角</span>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <div class="notice warn" style="margin-bottom: 14px">
    <AppIcon name="alert" :size="15" />
    <span>risk-rpc-core 当前处于降级状态：调用将返回「服务暂时不可用，请稍后重试」，并记录降级来源。</span>
  </div>

  <section class="panel">
    <header class="panel-head">
      <h2 class="panel-title">已注册服务</h2>
      <span class="panel-meta">{{ SERVERS.length }} SERVERS · 5 ONLINE / 1 DEGRADED</span>
    </header>
    <div class="panel-body" style="padding: 0; overflow-x: auto">
      <table class="tbl">
        <thead>
          <tr>
            <th>服务名称</th>
            <th>地址</th>
            <th>协议</th>
            <th>超时</th>
            <th>降级话术</th>
            <th>状态</th>
            <th class="r">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="server in SERVERS" :key="server.name">
            <td class="strong">{{ server.name }}</td>
            <td class="mono" style="font-size: 11.5px">{{ server.address }}</td>
            <td><span class="tag tag-neutral">{{ server.protocol }}</span></td>
            <td class="num">{{ server.timeout }}</td>
            <td style="color: var(--muted); font-size: 11.5px">{{ server.fallback }}</td>
            <td>
              <span class="tag" :class="server.status === '在线' ? 'tag-ok' : 'tag-warn'">{{ server.status }}</span>
            </td>
            <td class="r" style="white-space: nowrap">
              <button class="btn btn-sm" type="button" @click="testConnection(server.name)">测试连接</button>
              <button
                class="btn btn-sm btn-quiet"
                type="button"
                @click="ui.toast(readonly ? '只读角色不可编辑注册信息' : '编辑入口为演示占位', readonly ? 'err' : '')"
              >
                编辑
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>

  <p class="hint mt14">提示：超时与降级话术变更会实时下发到网关，无需重启 Agent 服务。</p>
</template>
