<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    data: number[]
    color?: string
  }>(),
  { color: '#125d4b' }
)

const points = computed(() => {
  const values = props.data
  if (!values.length) return ''
  const max = Math.max(...values)
  const min = Math.min(...values)
  const span = max - min || 1
  return values
    .map((value, index) => {
      const x = (index / (values.length - 1)) * 92 + 2
      const y = 27 - ((value - min) / span) * 23
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
})
</script>

<template>
  <svg class="kpi-spark" viewBox="0 0 96 30" preserveAspectRatio="none" aria-hidden="true">
    <polyline :points="points" fill="none" :stroke="color" stroke-width="1.6" stroke-linejoin="round" />
  </svg>
</template>
