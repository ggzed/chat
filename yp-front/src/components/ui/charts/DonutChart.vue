<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    parts: { label: string; value: number; color: string }[]
    caption?: string
  }>(),
  { caption: '' }
)

const radius = 44
const circumference = 2 * Math.PI * radius
const total = computed(() => props.parts.reduce((sum, item) => sum + item.value, 0) || 1)

const arcs = computed(() => {
  let offset = 0
  return props.parts.map((item) => {
    const length = (item.value / total.value) * circumference
    const arc = {
      ...item,
      dash: `${length} ${circumference - length}`,
      offset: -offset
    }
    offset += length
    return arc
  })
})
</script>

<template>
  <div class="chart-wrap" style="display: flex; align-items: center; gap: 18px; flex-wrap: wrap">
    <svg width="120" height="120" viewBox="0 0 120 120" role="img" aria-label="Agent Token 消耗占比">
      <circle cx="60" cy="60" :r="radius" fill="none" stroke="#eef1ea" stroke-width="16" />
      <circle
        v-for="(arc, index) in arcs"
        :key="index"
        cx="60"
        cy="60"
        :r="radius"
        fill="none"
        :stroke="arc.color"
        stroke-width="16"
        :stroke-dasharray="arc.dash"
        :stroke-dashoffset="arc.offset"
        stroke-linecap="butt"
        transform="rotate(-90 60 60)"
      />
      <text x="60" y="56" text-anchor="middle" font-size="9" fill="#8a948c" font-family="ui-monospace, monospace">TOTAL</text>
      <text x="60" y="72" text-anchor="middle" font-size="16" font-weight="700" fill="#161c18">100%</text>
    </svg>
    <div style="display: flex; flex-direction: column; gap: 7px; flex: 1; min-width: 140px">
      <div v-for="item in parts" :key="item.label" style="display: flex; align-items: center; gap: 8px; font-size: 11.5px; color: #5f6a62">
        <i :style="{ width: '9px', height: '9px', borderRadius: '2px', background: item.color, display: 'inline-block' }"></i>
        <span style="flex: 1">{{ item.label }}</span>
        <b class="num" style="color: #161c18">{{ item.value }}%</b>
      </div>
      <p v-if="caption" class="hint" style="margin: 2px 0 0">{{ caption }}</p>
    </div>
  </div>
</template>
