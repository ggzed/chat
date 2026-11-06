<script setup lang="ts">
import { computed } from 'vue'
import { TREND_A } from '@/data/console'
import SparkLine from '@/components/ui/charts/SparkLine.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const props = defineProps<{
  refs: { src: string; score: string; txt: string; bm25: string; knn: string }[]
}>()

const total = computed(() => props.refs.length)
</script>

<template>
  <div class="params">
    <span class="param">index=risk_rules_v3</span>
    <span class="param">top_k=5</span>
    <span class="param">threshold=0.72</span>
    <span class="param">hybrid=BM25+KNN</span>
  </div>

  <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 10px">
    <span class="tag tag-brand">命中 {{ total }} 条</span>
    <span class="hint">按相关性降序 · 已过滤低于阈值的片段</span>
  </div>

  <article v-for="ref in refs" :key="ref.src" class="ref-card">
    <div class="ref-head">
      <span class="tag tag-neutral">RULE</span>
      <span class="rh-src" :title="ref.src">{{ ref.src }}</span>
      <span class="mono" style="color: var(--brand); font-weight: 700">score {{ ref.score }}</span>
    </div>
    <p class="ref-txt" v-html="ref.txt"></p>
    <div class="ref-foot">
      <span>BM25 {{ ref.bm25 }}</span>
      <span>KNN {{ ref.knn }}</span>
    </div>
  </article>

  <div class="notice" style="margin-top: 12px">
    <AppIcon name="shield" :size="15" />
    <span>回答严格受限于以上规则片段；未命中时 Agent 会明确回复「暂无相关规则」。</span>
  </div>

  <div style="margin-top: 14px; padding-top: 12px; border-top: 1px solid var(--line); display: flex; align-items: center; gap: 12px">
    <span class="hint" style="margin: 0">本次召回耗时</span>
    <span class="mono" style="font-size: 11.5px; color: var(--brand); font-weight: 700">842 ms</span>
    <SparkLine :data="TREND_A.slice(0, 9)" :color="'#125d4b'" />
  </div>
</template>
