<script setup lang="ts">
import { useRoute } from 'vue-router'
import type { RouteLocationRaw } from 'vue-router'

defineProps<{
  items: { label: string; to: RouteLocationRaw }[]
}>()

const route = useRoute()

function isActive(to: RouteLocationRaw): boolean {
  return typeof to === 'string' && route.path === to
}
</script>

<template>
  <nav class="tabs" aria-label="子页面切换">
    <RouterLink
      v-for="item in items"
      :key="String(item.to)"
      :to="item.to"
      class="tab"
      :class="{ 'is-active': isActive(item.to) }"
    >
      {{ item.label }}
    </RouterLink>
  </nav>
</template>
