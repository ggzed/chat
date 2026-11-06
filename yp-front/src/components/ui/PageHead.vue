<script setup lang="ts">
withDefaults(
  defineProps<{
    eyebrow?: string
    title: string
    sub?: string
  }>(),
  { eyebrow: '', sub: '' }
)
</script>

<template>
  <header class="page-head">
    <div>
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
      <h1 class="page-title">{{ title }}</h1>
      <p v-if="sub" class="page-sub">{{ sub }}</p>
    </div>
    <div class="head-actions">
      <slot name="actions" />
    </div>
  </header>
</template>
