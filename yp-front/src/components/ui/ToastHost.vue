<script setup lang="ts">
import { useUiStore } from '@/stores/ui'
import AppIcon from './AppIcon.vue'

const ui = useUiStore()
</script>

<template>
  <div class="toast-root" role="status" aria-live="polite">
    <div
      v-for="item in ui.toasts"
      :key="item.id"
      class="toast"
      :class="item.kind"
      @click="ui.dismissToast(item.id)"
    >
      <AppIcon :name="item.kind === 'err' ? 'alert' : item.kind === 'ok' ? 'check' : 'clock'" :size="15" />
      <span>{{ item.message }}</span>
    </div>
  </div>
</template>
