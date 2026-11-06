import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { ROLES } from '@/data/console'
import type { RoleKey } from '@/data/console'
import { loginApi } from '@/api/auth'
import type { LoginData } from '@/api/auth'

const STORAGE_KEY = 'yp_console_auth'

interface PersistedAuth {
  token: string
  profile: Omit<LoginData, 'token' | 'roles'> & { roles: RoleKey[] }
  role: RoleKey
}

function loadPersisted(): PersistedAuth | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as PersistedAuth
    if (!parsed.token || !parsed.profile?.roles?.length) return null
    return parsed
  } catch {
    return null
  }
}

/** 角色与权限状态：登录后由服务端下发角色集合，当前视角只能在这些角色内切换。 */
export const useAuthStore = defineStore('auth', () => {
  const persisted = loadPersisted()

  const token = ref(persisted?.token ?? '')
  const profile = ref<PersistedAuth['profile'] | null>(persisted?.profile ?? null)
  const role = ref<RoleKey>(persisted?.role ?? 'admin')

  const isAuthenticated = computed(() => !!token.value)
  /** 当前用户拥有的角色（登录下发），演示角色切换被限制在这个集合内 */
  const availableRoles = computed<RoleKey[]>(() => profile.value?.roles ?? [])
  /** 当前角色对应的展示信息（名称 / 描述） */
  const user = computed(() => ROLES[role.value])
  /** 登录用户的真实姓名 */
  const realName = computed(() => profile.value?.realName ?? '')

  function persist() {
    if (!profile.value) return
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ token: token.value, profile: profile.value, role: role.value })
    )
  }

  async function login(username: string, password: string) {
    const data = await loginApi(username, password)
    const roles = (data.roles ?? []).filter((r): r is RoleKey => r in ROLES)
    if (!roles.length) throw new Error('账号未分配可用角色，请联系管理员')
    token.value = data.token
    profile.value = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      deptId: data.deptId,
      roles
    }
    role.value = roles.includes('admin') ? 'admin' : roles[0]
    persist()
  }

  function logout() {
    token.value = ''
    profile.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  function setRole(next: RoleKey) {
    if (availableRoles.value.includes(next)) {
      role.value = next
      persist()
    }
  }

  function allows(roles?: readonly RoleKey[]) {
    return !roles || roles.includes(role.value)
  }

  return { token, profile, role, user, realName, isAuthenticated, availableRoles, login, logout, setRole, allows }
})
