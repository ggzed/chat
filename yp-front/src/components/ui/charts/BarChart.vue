<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    items: { key: string; value: number }[]
    height?: number
    unit?: string
  }>(),
  { height: 190, unit: '亿 Token' }
)

const width = 560
const padX = 34
const padTop = 16
const padBottom = 30
const innerW = width - padX * 2
const innerH = computed(() => props.height - padTop - padBottom)
const max = computed(() => Math.max(...props.items.map((item) => item.value)) * 1.15 || 1)

const bars = computed(() =>
  props.items.map((item, index) => {
    const barWidth = Math.min(46, innerW / props.items.length - 18)
    const x = padX + (innerW / props.items.length) * index + (innerW / props.items.length - barWidth) / 2
    const barHeight = (item.value / max.value) * innerH.value
    const y = padTop + innerH.value - barHeight
    return { ...item, x, y, barWidth, barHeight }
  })
)

const ticks = computed(() =>
  [0, 1, 2, 3].map((index) => {
    const y = padTop + (innerH.value / 3) * index
    return { y, value: Math.round(max.value * (1 - index / 3)) }
  })
)
</script>

<template>
  <div class="chart-wrap">
    <svg :viewBox="`0 0 ${width} ${height}`" role="img" aria-label="部门 Token 消耗">
      <line
        v-for="tick in ticks"
        :key="tick.y"
        :x1="padX"
        :x2="width - padX"
        :y1="tick.y"
        :y2="tick.y"
        stroke="#e7ebe4"
      />
      <text
        v-for="tick in ticks"
        :key="`v-${tick.y}`"
        :x="padX - 6"
        :y="tick.y + 3"
        text-anchor="end"
        font-size="9"
        fill="#8a948c"
        font-family="ui-monospace, monospace"
      >
        {{ tick.value }}
      </text>
      <g v-for="bar in bars" :key="bar.key">
        <rect
          :x="bar.x"
          :y="bar.y"
          :width="bar.barWidth"
          :height="bar.barHeight"
          rx="3"
          fill="#125d4b"
          opacity="0.9"
        />
        <text
          :x="bar.x + bar.barWidth / 2"
          :y="padTop + innerH + 15"
          text-anchor="middle"
          font-size="9.5"
          fill="#5f6a62"
        >
          {{ bar.key }}
        </text>
        <text
          :x="bar.x + bar.barWidth / 2"
          :y="bar.y - 5"
          text-anchor="middle"
          font-size="9"
          fill="#161c18"
          font-weight="600"
          font-family="ui-monospace, monospace"
        >
          {{ bar.value }}
        </text>
      </g>
      <text :x="width - padX" :y="height - 6" text-anchor="end" font-size="9" fill="#8a948c">
        {{ unit }}
      </text>
    </svg>
  </div>
</template>
