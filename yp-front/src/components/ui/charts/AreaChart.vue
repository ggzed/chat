<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  primary: number[]
  secondary?: number[]
}>()

const width = 640
const height = 200
const padX = 12
const padTop = 14
const padBottom = 28
const innerW = width - padX * 2
const innerH = height - padTop - padBottom
const fillId = `area-fill-${Math.random().toString(36).slice(2, 8)}`

const primaryMax = computed(() => Math.max(...props.primary) * 1.12 || 1)
const secondaryMax = computed(() =>
  props.secondary && props.secondary.length ? Math.max(...props.secondary) * 1.35 : 1
)

function line(values: number[], max: number): string {
  return values
    .map((value, index) => {
      const x = padX + (index / (values.length - 1)) * innerW
      const y = padTop + innerH - (value / max) * innerH
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
}

const primaryPoints = computed(() => line(props.primary, primaryMax.value))
const secondaryPoints = computed(() =>
  props.secondary && props.secondary.length ? line(props.secondary, secondaryMax.value) : ''
)
const areaPath = computed(() => {
  const points = primaryPoints.value.split(' ')
  const startX = padX
  const endX = padX + innerW
  return `M${startX},${padTop + innerH} L${points.join(' L')} L${endX},${padTop + innerH} Z`
})
const ticks = computed(() =>
  [0, 1, 2, 3, 4].map((index) => {
    const y = padTop + (innerH / 4) * index
    return { y, value: Math.round(primaryMax.value * (1 - index / 4)) }
  })
)
</script>

<template>
  <div class="chart-wrap">
    <svg :viewBox="`0 0 ${width} ${height}`" role="img" aria-label="24 小时调用量趋势">
      <defs>
        <linearGradient :id="fillId" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#125d4b" stop-opacity="0.18" />
          <stop offset="100%" stop-color="#125d4b" stop-opacity="0.02" />
        </linearGradient>
      </defs>
      <g>
        <line
          v-for="tick in ticks"
          :key="tick.y"
          :x1="padX"
          :x2="width - padX"
          :y1="tick.y"
          :y2="tick.y"
          stroke="#e7ebe4"
          stroke-width="1"
        />
        <text
          v-for="tick in ticks"
          :key="`t-${tick.y}`"
          :x="padX"
          :y="tick.y - 4"
          fill="#8a948c"
          font-size="9"
          font-family="ui-monospace, monospace"
        >
          {{ tick.value.toLocaleString() }}
        </text>
      </g>
      <path :d="areaPath" :fill="`url(#${fillId})`" />
      <polyline
        :points="primaryPoints"
        fill="none"
        stroke="#125d4b"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <polyline
        v-if="secondaryPoints"
        :points="secondaryPoints"
        fill="none"
        stroke="#b42318"
        stroke-width="1.4"
        stroke-dasharray="5 4"
        opacity="0.8"
      />
      <g fill="#8a948c" font-size="9" font-family="ui-monospace, monospace">
        <text :x="padX" :y="height - 8">00:00</text>
        <text :x="padX + innerW / 2 - 12" :y="height - 8">12:00</text>
        <text :x="width - padX - 26" :y="height - 8">24:00</text>
      </g>
    </svg>
    <div class="legend">
      <span><i style="background: #125d4b"></i>调用量</span>
      <span><i style="background: #b42318"></i>错误率（右轴）</span>
    </div>
  </div>
</template>
