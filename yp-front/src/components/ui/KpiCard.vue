<script setup lang="ts">
import SparkLine from './charts/SparkLine.vue'

withDefaults(
  defineProps<{
    label: string
    value: string
    unit?: string
    delta?: string
    trend?: 'up' | 'down' | 'flat'
    variant?: '' | 'danger' | 'warn' | 'info'
    data: number[]
  }>(),
  { unit: '', delta: '', trend: 'flat', variant: '' }
)
</script>

<template>
  <article class="kpi" :class="variant ? `k-${variant}` : ''">
    <p class="kpi-label">{{ label }}</p>
    <p class="kpi-value">
      {{ value }}<small v-if="unit">{{ unit }}</small>
    </p>
    <div class="kpi-foot">
      <span v-if="delta" class="kpi-delta" :class="trend">{{ delta }}</span>
      <SparkLine :data="data" :color="variant === 'danger' ? '#b42318' : variant === 'warn' ? '#9a5b06' : undefined" />
    </div>
  </article>
</template>
