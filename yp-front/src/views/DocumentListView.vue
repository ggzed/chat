<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePlatformStore } from '@/stores/platform'
import { useUiStore } from '@/stores/ui'
import type { DocItem } from '@/data/console'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const auth = useAuthStore()
const platform = usePlatformStore()
const ui = useUiStore()

const tabs = [
  { label: '文档列表', to: '/kb/docs' },
  { label: '召回测试', to: '/kb/recall' }
]

const readonly = computed(() => auth.role === 'ops')

const statusTag: Record<DocItem['status'], string> = {
  已向量化: 'tag-ok',
  解析中: 'tag-warn',
  解析失败: 'tag-danger'
}

function upload() {
  if (readonly.value) {
    ui.toast('只读角色不可上传文档', 'err')
    return
  }
  platform.uploadDoc()
  ui.toast('文档已上传，正在解析与向量化…', 'ok')
}

function retry(doc: DocItem) {
  if (readonly.value) {
    ui.toast('只读角色不可操作文档', 'err')
    return
  }
  doc.status = '解析中'
  window.setTimeout(() => {
    doc.status = '已向量化'
    doc.chunks = 96
    doc.index = 'risk_rules_v3'
  }, 1600)
  ui.toast('已重新解析该文档', 'ok')
}
</script>

<template>
  <PageHead
    eyebrow="KNOWLEDGE / 文档列表"
    title="知识库文档"
    sub="上传、解析、切片与向量化。解析完成后可在「召回测试」中验证命中效果。"
  >
    <template #actions>
      <button v-if="!readonly" class="btn btn-primary" type="button" @click="upload">
        <AppIcon name="upload" :size="14" />上传文档
      </button>
      <span v-else class="tag tag-neutral">只读视角</span>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <div v-if="readonly" class="notice" style="margin-bottom: 14px">
    <AppIcon name="shield" :size="15" />
    <span>运维/主管角色可查看文档与解析状态；上传、重试与删除仅管理员可用。</span>
  </div>

  <div v-if="!readonly" class="uploader" style="margin-bottom: 14px">
    <AppIcon class="u-ico" name="upload" :size="26" />
    <div style="font-weight: 600; color: var(--ink-2)">拖拽 PDF / Word / CSV 到此处，或点击上传</div>
    <div class="hint" style="margin-top: 4px">单文件 ≤ 20 MB · 自动切片并写入 risk_rules_v3 索引</div>
    <button class="btn btn-primary btn-sm" type="button" style="margin-top: 12px" @click="upload">选择文件</button>
  </div>

  <section class="panel">
    <header class="panel-head">
      <h2 class="panel-title">全部文档</h2>
      <span class="panel-meta">{{ platform.docs.length }} DOCS</span>
    </header>
    <div class="panel-body" style="padding: 0; overflow-x: auto">
      <table class="tbl">
        <thead>
          <tr>
            <th>文档</th>
            <th>类型</th>
            <th>大小</th>
            <th class="r">切片</th>
            <th>状态</th>
            <th>索引库</th>
            <th>上传时间</th>
            <th class="r">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="doc in platform.docs" :key="doc.name">
            <td>
              <div class="strong" style="display: flex; align-items: center; gap: 8px">
                <AppIcon name="file" :size="14" style="color: var(--brand)" />
                {{ doc.name }}
              </div>
            </td>
            <td><span class="tag tag-neutral">{{ doc.type }}</span></td>
            <td class="num">{{ doc.size }}</td>
            <td class="r num">{{ doc.chunks || '—' }}</td>
            <td>
              <span class="tag" :class="statusTag[doc.status]">{{ doc.status }}</span>
              <div v-if="doc.status === '解析中'" class="progress" style="margin-top: 5px">
                <i style="width: 62%"></i>
              </div>
            </td>
            <td class="mono" style="font-size: 11px">{{ doc.index }}</td>
            <td class="num">{{ doc.by }}</td>
            <td class="r" style="white-space: nowrap">
              <button v-if="doc.status === '解析失败'" class="btn btn-sm" type="button" @click="retry(doc)">重试</button>
              <button
                class="btn btn-sm btn-quiet"
                type="button"
                @click="ui.toast(doc.status === '解析中' ? '文档仍在解析中，请稍候' : `已打开「${doc.name}」的切片预览（演示）`, doc.status === '解析中' ? 'err' : '')"
              >
                切片预览
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
