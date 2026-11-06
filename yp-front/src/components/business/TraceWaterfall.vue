<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { TRACE_SPANS } from '@/data/console'

const visible = ref<boolean[]>(TRACE_SPANS.map(() => false))
let timers: number[] = []

function clearTimers() {
  timers.forEach((timer) => window.clearTimeout(timer))
  timers = []
}

function replay() {
  clearTimers()
  visible.value = TRACE_SPANS.map(() => false)
  TRACE_SPANS.forEach((_, index) => {
    const timer = window.setTimeout(() => {
      visible.value = visible.value.map((value, i) => (i === index ? true : value))
    }, 90 + index * 120)
    timers.push(timer)
  })
}

onMounted(replay)
onBeforeUnmount(clearTimers)

defineExpose({ replay })
</script>

<template>
  <div>
    <div v-for="(span, index) in TRACE_SPANS" :key="span.name" class="wf-row">
      <div class="wf-label">
        <span class="dot" :class="span.color === 'b-danger' ? 'dot-danger' : span.color === 'b-warn' ? 'dot-warn' : 'dot-ok'"></span>
        <span style="overflow: hidden; text-overflow: ellipsis">{{ span.name }}</span>
      </div>
      <div class="wf-track">
        <div
          class="wf-bar"
          :class="[span.color, { 'is-in': visible[index] }]"
          :style="{ left: `${span.offset}%`, width: `${span.width}%`, transitionDelay: '0ms' }"
        ></div>
      </div>
      <div class="wf-ms">{{ span.ms }}</div>
    </div>
    <div class="wf-axis">
      <span>0ms</span>
      <span>200ms</span>
      <span>400ms</span>
      <span>600ms</span>
      <span>800ms</span>
      <span>1000ms</span>
      <span>1200ms</span>
    </div>
  </div>
</template>
