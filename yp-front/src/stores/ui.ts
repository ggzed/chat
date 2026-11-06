import { reactive, ref } from 'vue'
import { defineStore } from 'pinia'

export type ToastKind = 'ok' | 'err' | ''

export interface ToastItem {
  id: number
  message: string
  kind: ToastKind
}

let seed = 0

export const useUiStore = defineStore('ui', () => {
  const sidebarOpen = ref(false)
  const collapsed = reactive<Record<string, boolean>>({})
  const toasts = ref<ToastItem[]>([])

  function toggleGroup(id: string) {
    collapsed[id] = !collapsed[id]
  }

  function toggleSidebar(open?: boolean) {
    sidebarOpen.value = open ?? !sidebarOpen.value
  }

  function dismissToast(id: number) {
    toasts.value = toasts.value.filter((item) => item.id !== id)
  }

  function toast(message: string, kind: ToastKind = '') {
    const id = ++seed
    toasts.value.push({ id, message, kind })
    window.setTimeout(() => dismissToast(id), 3000)
  }

  return { sidebarOpen, collapsed, toasts, toggleGroup, toggleSidebar, toast, dismissToast }
})
