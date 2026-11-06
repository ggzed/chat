<script setup lang="ts">
defineProps<{
  title: string
  meta?: string
}>()
</script>

<template>
  <section class="panel">
    <header class="panel-head">
      <h2 class="panel-title">{{ title }}</h2>
      <span v-if="meta" class="panel-meta">{{ meta }}</span>
      <slot name="head" />
    </header>
    <div class="panel-body">
      <slot />
    </div>
  </section>
</template>
