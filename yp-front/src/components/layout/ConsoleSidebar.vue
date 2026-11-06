<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ALL_ROLES, NAV, ROLES, groupOf } from '@/data/console'
import type { NavItem, RoleKey } from '@/data/console'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import AppIcon from '@/components/ui/AppIcon.vue'

const auth = useAuthStore()
const ui = useUiStore()
const route = useRoute()
const router = useRouter()

const activePath = computed(() => (route.path === '/agents/new' ? '/agents' : route.path))
const currentGroup = computed(() => groupOf(activePath.value)?.id)

function allowed(item: NavItem): boolean {
  return item.roles.includes(auth.role)
}

function lockedClick(item: NavItem) {
  ui.toast(`当前角色（${ROLES[auth.role].name}）无权访问「${item.label}」`, 'err')
}

function switchRole(next: RoleKey) {
  if (next === auth.role) return
  auth.setRole(next)
  ui.toast(`已切换到${ROLES[next].name}视角`, 'ok')

  if (route.name === 'denied') {
    const from = typeof route.query.from === 'string' ? route.query.from : ''
    if (from) {
      const target = router.resolve(from)
      if (auth.allows(target.meta.roles)) {
        router.replace(from)
      }
    }
    return
  }

  if (!auth.allows(route.meta.roles)) {
    router.replace({ name: 'denied', query: { from: route.fullPath } })
  }
}

watch(
  () => route.fullPath,
  () => ui.toggleSidebar(false)
)
</script>

<template>
  <aside class="sidebar" :class="{ 'is-open': ui.sidebarOpen }" aria-label="主导航">
    <div class="brand">
      <div class="brand-mark">
        <AppIcon name="shield" :size="18" />
      </div>
      <div class="brand-text">
        <strong>风控智能体中台</strong>
        <span>RISK AGENT CONSOLE</span>
      </div>
    </div>

    <div class="role-switch" role="group" aria-label="演示角色切换">
      <button
        v-for="key in ALL_ROLES"
        :key="key"
        class="role-btn"
        :class="{ 'is-on': auth.role === key }"
        type="button"
        :aria-pressed="auth.role === key"
        @click="switchRole(key)"
      >
        {{ ROLES[key].name }}
      </button>
    </div>
    <p class="role-caption">{{ ROLES[auth.role].desc }}</p>

    <nav class="nav-scroll">
      <div
        v-for="group in NAV"
        :key="group.id"
        class="nav-group"
        :class="{ 'is-collapsed': ui.collapsed[group.id], 'is-current': currentGroup === group.id }"
      >
        <button
          class="nav-group-head"
          type="button"
          :aria-expanded="!ui.collapsed[group.id]"
          @click="ui.toggleGroup(group.id)"
        >
          <AppIcon class="g-ico" :name="group.icon" :size="17" />
          <span class="g-label">{{ group.label }}</span>
          <AppIcon class="chev" name="chevD" :size="14" />
        </button>
        <div class="nav-items">
          <template v-for="item in group.items" :key="item.path">
            <RouterLink
              v-if="allowed(item)"
              :to="item.path"
              class="nav-item"
              :class="{ 'is-active': activePath === item.path }"
            >
              <span class="n-label">{{ item.label }}</span>
              <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
              <span v-if="item.tag" class="nav-tag">{{ item.tag }}</span>
            </RouterLink>
            <button
              v-else
              type="button"
              class="nav-item is-locked"
              aria-disabled="true"
              :title="`${ROLES[auth.role].name}无权访问`"
              @click="lockedClick(item)"
            >
              <span class="n-label">{{ item.label }}</span>
              <span v-if="item.badge" class="nav-badge" style="opacity: 0.5">{{ item.badge }}</span>
              <AppIcon class="lock" name="lock" :size="12" />
            </button>
          </template>
        </div>
      </div>
    </nav>

    <div class="side-foot">
      <div>v2.4.0 · 网关 cn-sh-01</div>
      <div><b>●</b> 所有服务正常 · 99.97%</div>
    </div>
  </aside>
</template>
