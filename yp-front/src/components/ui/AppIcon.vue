<script setup lang="ts">
import { computed } from 'vue'
import { ICONS } from '@/data/icons'

const props = withDefaults(defineProps<{ name: string; size?: number; stroke?: number }>(), {
  size: 16,
  stroke: 1.6
})

const path = computed(() => ICONS[props.name] ?? ICONS.grid)
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    :stroke-width="stroke"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    v-html="path"
  />
</template>
