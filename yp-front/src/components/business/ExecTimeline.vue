<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { DEPT_BARS, TL_STEPS } from '@/data/console'
import AppIcon from '@/components/ui/AppIcon.vue'

type State = 'idle' | 'active' | 'done'

const states = ref<State[]>(TL_STEPS.map(() => 'idle'))
const running = ref(false)
let timers: number[] = []

const done = computed(() => states.value.filter((state) => state === 'done').length)
const finished = computed(() => done.value === TL_STEPS.length)

function clearTimers() {
  timers.forEach((timer) => window.clearTimeout(timer))
  timers = []
}

function replay() {
  clearTimers()
  running.value = true
  states.value = TL_STEPS.map(() => 'idle')

  TL_STEPS.forEach((_, index) => {
    const start = window.setTimeout(() => {
      states.value = states.value.map((state, i) => (i === index ? 'active' : state))
    }, index * 520)

    const end = window.setTimeout(() => {
      states.value = states.value.map((state, i) => (i === index ? 'done' : state))
      if (index === TL_STEPS.length - 1) running.value = false
    }, index * 520 + 460)

    timers.push(start, end)
  })
}

onBeforeUnmount(clearTimers)
replay()

defineExpose({ replay })
</script>

<template>
  <div class="side-bar">
    <span class="side-title">执行时间线</span>
    <span class="tag" :class="finished ? 'tag-ok' : 'tag-brand'" style="margin-left: auto">
      {{ finished ? '已完成' : running ? '执行中' : '待执行' }}
    </span>
    <button class="btn btn-sm btn-quiet" type="button" @click="replay">
      <AppIcon name="refresh" :size="12" />回放
    </button>
  </div>

  <div class="side-body">
    <div class="timeline">
      <div
        v-for="(step, index) in TL_STEPS"
        :key="step.title"
        class="tl-step"
        :class="{ 'is-done': states[index] === 'done', 'is-active': states[index] === 'active' }"
      >
        <div class="tl-rail">
          <span class="tl-dot">
            <AppIcon v-if="states[index] === 'done'" name="check" :size="11" />
            <template v-else>{{ index + 1 }}</template>
          </span>
        </div>
        <div class="tl-body">
          <div class="tl-t">
            {{ step.title }}
            <span class="tl-ms">{{ step.ms }}</span>
          </div>
          <div class="tl-d">{{ step.desc }}</div>
        </div>
      </div>
    </div>

    <div v-if="finished" class="tl-result">
      <div class="tr-title">刚控数量 · 按部门（2026-11-05）</div>
      <div v-for="bar in DEPT_BARS" :key="bar.name" class="barline">
        <span class="bl-name">{{ bar.name }}</span>
        <span class="bl-track"><span class="bl-fill" :style="{ width: `${bar.width}%` }"></span></span>
        <span class="bl-val">{{ bar.value }}</span>
      </div>
      <div class="hint" style="margin-top: 6px">汇总 1,284 条 · 命中黑名单占比 41.2% · 统计口径已校验</div>
    </div>
    <div v-else class="tl-result">
      <div class="skeleton" style="width: 40%"></div>
      <div class="skeleton" style="width: 78%"></div>
      <div class="skeleton" style="width: 62%"></div>
      <div class="skeleton" style="width: 86%; margin-bottom: 0"></div>
    </div>
  </div>
</template>
