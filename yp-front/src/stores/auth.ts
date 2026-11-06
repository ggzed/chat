import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { ALL_ROLES, ROLES } from '@/data/console'
import type { RoleKey } from '@/data/console'

/** 角色与权限状态。生产环境中 role 与 permissions 应由登录接口下发。 */
export const useAuthStore = defineStore('auth', () => {
  const role = ref<RoleKey>('admin')
  const user = computed(() => ROLES[role.value])

  function setRole(next: RoleKey) {
    if (ALL_ROLES.includes(next)) {
      role.value = next
    }
  }

  function allows(roles?: readonly RoleKey[]) {
    return !roles || roles.includes(role.value)
  }

  return { role, user, setRole, allows }
})
