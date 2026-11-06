import { post } from './http'

export interface LoginData {
  token: string
  userId: number
  username: string
  realName: string
  deptId: number | null
  /** 角色编码：sales / admin / ops */
  roles: string[]
}

/** 登录：POST /api/admin/auth/login（经网关透传到 yp-agent-admin） */
export function loginApi(username: string, password: string) {
  return post<LoginData>('/api/admin/auth/login', { username, password })
}
