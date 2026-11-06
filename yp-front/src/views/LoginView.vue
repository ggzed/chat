<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const REMEMBER_KEY = 'yp_login_user'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const remember = ref(false)
const showPwd = ref(false)
const busy = ref(false)
const done = ref(false)
const notice = ref<{ type: 'err' | 'ok'; text: string } | null>(null)
const shaking = ref(false)
const invalidField = ref<'username' | 'password' | null>(null)

onMounted(() => {
  try {
    const saved = localStorage.getItem(REMEMBER_KEY)
    if (saved) {
      username.value = saved
      remember.value = true
    }
  } catch {
    /* localStorage 不可用时静默降级 */
  }
})

function clearError() {
  notice.value = null
  invalidField.value = null
}

function fail(msg: string, field?: 'username' | 'password') {
  notice.value = { type: 'err', text: msg }
  invalidField.value = field ?? null
  shaking.value = false
  requestAnimationFrame(() => {
    shaking.value = true
  })
}

async function submit() {
  if (busy.value) return
  clearError()
  const u = username.value.trim()
  const p = password.value
  if (!u) return fail('请输入账号', 'username')
  if (!p) return fail('请输入密码', 'password')

  try {
    if (remember.value) localStorage.setItem(REMEMBER_KEY, u)
    else localStorage.removeItem(REMEMBER_KEY)
  } catch {
    /* 忽略 */
  }

  busy.value = true
  try {
    await auth.login(u, p)
    done.value = true
    notice.value = { type: 'ok', text: '认证通过，正在跳转控制台' }
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    window.setTimeout(() => {
      router.replace(redirect || '/dashboard/overview')
    }, 420)
  } catch (e) {
    fail(e instanceof Error ? e.message : '登录失败，请稍后重试')
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="card" :class="{ 'is-shake': shaking }" aria-labelledby="loginTitle">
      <div class="brand">
        <span class="brand-mark" aria-hidden="true">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2.5 20 6v5.5c0 5-3.4 8.6-8 10-4.6-1.4-8-5-8-10V6l8-3.5Z"/><path d="M8.8 12.2l2.2 2.2 4.3-4.6"/></svg>
        </span>
        <span class="brand-name"><strong>风控智能体中台</strong><span>Risk Agent Platform</span></span>
      </div>

      <h1 id="loginTitle">登录</h1>
      <p class="sub">请使用企业域账号进入控制台</p>

      <div v-if="notice" class="notice" :class="notice.type === 'err' ? 'is-err' : 'is-ok'" :role="notice.type === 'err' ? 'alert' : 'status'">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="M12 7.5v5.5M12 16.4h.01"/></svg>
        <span>{{ notice.text }}</span>
      </div>

      <form novalidate @submit.prevent="submit">
        <div class="field">
          <label for="username">账号</label>
          <div class="input-wrap">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="3.6"/><path d="M4.8 20c.5-3.7 3.5-5.8 7.2-5.8s6.7 2.1 7.2 5.8"/></svg>
            <input
              id="username"
              v-model="username"
              type="text"
              autocomplete="username"
              placeholder="工号 / 域账号"
              autofocus
              :aria-invalid="invalidField === 'username' || undefined"
              @input="clearError"
            />
          </div>
        </div>

        <div class="field">
          <label for="password">密码</label>
          <div class="input-wrap">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="4.5" y="10.5" width="15" height="10" rx="2"/><path d="M8 10.5V7.6a4 4 0 0 1 8 0v2.9"/></svg>
            <input
              id="password"
              v-model="password"
              :type="showPwd ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="请输入密码"
              :aria-invalid="invalidField === 'password' || undefined"
              @input="clearError"
            />
            <button
              type="button"
              class="eye"
              :aria-label="showPwd ? '隐藏密码' : '显示密码'"
              :aria-pressed="showPwd"
              @click="showPwd = !showPwd"
            >
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 12S6 5.8 12 5.8 21.5 12 21.5 12 18 18.2 12 18.2 2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="2.6"/><path v-if="showPwd" class="slash" d="M3.5 3.5 20.5 20.5"/></svg>
            </button>
          </div>
        </div>

        <div class="row">
          <label class="check" for="remember"><input id="remember" v-model="remember" type="checkbox" />记住账号</label>
        </div>

        <button class="submit" :class="{ 'is-done': done }" type="submit" :disabled="busy">
          <span>{{ done ? '登录成功，正在进入…' : busy ? '正在验证…' : '登录控制台' }}</span>
          <span v-if="busy && !done" class="spinner" aria-hidden="true"></span>
        </button>
      </form>

      <p class="demo">演示环境 · admin / admin123</p>
    </section>

    <p class="foot">RISK AGENT PLATFORM · 内部系统 · 仅限授权访问</p>
  </main>
</template>

<style scoped>
.login-page {
  --paper: #f4f6f2;
  --surface-2: #ffffff;
  --ink: #161c18;
  --ink-2: #3a443d;
  --muted: #5f6a62;
  --line: #dde3da;
  --line-2: #c9d2c7;
  --brand: #125d4b;
  --brand-hover: #0d4a3b;
  --brand-soft: #e5f0ea;
  --danger: #b42318;
  --danger-soft: #fcf0ee;
  --danger-line: #f0c9c3;
  --ok: #187a50;
  --ok-soft: #e9f4ec;
  --radius: 6px;
  --sh-md: 0 2px 6px rgba(22, 28, 24, 0.05), 0 14px 40px rgba(22, 28, 24, 0.07);
  --ease: cubic-bezier(0.16, 1, 0.3, 1);
  --mono: ui-monospace, 'SFMono-Regular', 'JetBrains Mono', Consolas, 'Liberation Mono', monospace;

  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18px;
  padding: 32px 18px;
  background: var(--paper);
  color: var(--ink);
}

.card {
  width: 100%;
  max-width: 400px;
  background: var(--surface-2);
  border: 1px solid var(--line);
  border-radius: 10px;
  box-shadow: var(--sh-md);
  padding: 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  margin-bottom: 28px;
}
.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--brand);
  color: #fff;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
}
.brand-name strong {
  display: block;
  font-size: 14px;
  font-weight: 650;
  letter-spacing: 0.02em;
  line-height: 1.3;
}
.brand-name span {
  display: block;
  font-family: var(--mono);
  font-size: 9.5px;
  letter-spacing: 0.13em;
  text-transform: uppercase;
  color: var(--muted);
  margin-top: 2px;
}

h1 {
  font-size: 25px;
  font-weight: 700;
  letter-spacing: -0.03em;
  margin: 0;
}
.sub {
  font-size: 12.5px;
  color: var(--muted);
  margin: 6px 0 24px;
}

.field {
  margin-bottom: 16px;
}
.field label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-2);
  margin-bottom: 7px;
}
.input-wrap {
  position: relative;
}
.input-wrap > svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--muted);
  pointer-events: none;
}
.input-wrap input {
  width: 100%;
  height: 44px;
  padding: 0 42px 0 38px;
  background: var(--surface-2);
  border: 1px solid var(--line-2);
  border-radius: var(--radius);
  outline: none;
  transition: border-color 0.18s var(--ease), box-shadow 0.18s var(--ease);
  font-size: 13.5px;
}
.input-wrap input::placeholder {
  color: var(--muted);
}
.input-wrap input:hover {
  border-color: #aab8ac;
}
.input-wrap input:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(18, 93, 75, 0.12);
}
.input-wrap input[aria-invalid='true'] {
  border-color: var(--danger);
  box-shadow: 0 0 0 3px rgba(180, 35, 24, 0.1);
}

.eye {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
  width: 34px;
  height: 34px;
  border: 0;
  background: transparent;
  color: var(--muted);
  border-radius: 5px;
  display: grid;
  place-items: center;
  cursor: pointer;
}
.eye:hover {
  background: var(--brand-soft);
  color: var(--brand);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 22px;
}
.check {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: var(--muted);
  cursor: pointer;
  user-select: none;
}
.check input {
  width: 15px;
  height: 15px;
  accent-color: var(--brand);
  cursor: pointer;
  margin: 0;
}

.submit {
  width: 100%;
  height: 44px;
  border: 0;
  border-radius: var(--radius);
  background: var(--brand);
  color: #fff;
  font-size: 13.5px;
  font-weight: 650;
  letter-spacing: 0.02em;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  transition: background 0.18s var(--ease);
  cursor: pointer;
}
.submit:hover:not(:disabled) {
  background: var(--brand-hover);
}
.submit:disabled {
  cursor: progress;
  opacity: 0.85;
}
.submit.is-done {
  background: var(--ok);
  cursor: default;
  opacity: 1;
}

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.notice {
  font-size: 12px;
  line-height: 1.5;
  border-radius: 5px;
  padding: 9px 11px;
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
  gap: 7px;
}
.notice svg {
  flex: 0 0 auto;
  margin-top: 1px;
}
.notice.is-err {
  background: var(--danger-soft);
  border: 1px solid var(--danger-line);
  color: var(--danger);
}
.notice.is-ok {
  background: var(--ok-soft);
  border: 1px solid #c4e3ce;
  color: var(--ok);
}

.card.is-shake {
  animation: shake 0.32s var(--ease);
}
@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-5px);
  }
  75% {
    transform: translateX(5px);
  }
}

.demo {
  font-size: 11.5px;
  color: var(--muted);
  text-align: center;
  margin: 20px 0 0;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}
.foot {
  font-family: var(--mono);
  font-size: 10.5px;
  color: var(--muted);
  letter-spacing: 0.04em;
  text-align: center;
}

@media (max-width: 420px) {
  .card {
    padding: 26px 22px;
  }
  .login-page {
    justify-content: flex-start;
    padding-top: 48px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .login-page *,
  .login-page *::before,
  .login-page *::after {
    animation-duration: 0.001ms !important;
    transition-duration: 0.001ms !important;
  }
}
</style>
