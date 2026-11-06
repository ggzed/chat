<script setup lang="ts">
import ConsoleSidebar from '@/components/layout/ConsoleSidebar.vue'
import ConsoleTopBar from '@/components/layout/ConsoleTopBar.vue'
import ToastHost from '@/components/ui/ToastHost.vue'
</script>

<template>
  <a class="skip" href="#main">跳到主要内容</a>
  <div class="app-shell">
    <ConsoleSidebar />
    <div class="right-col">
      <div class="sysbar">
        <span><i class="s-dot"></i>PaaS 平台 · 运行中</span>
        <span>大模型推理集群 · 正常</span>
        <span>MCP 通道 · 5/6 在线</span>
        <span class="spacer"></span>
        <span>2026-11-06 22:57 CST</span>
      </div>
      <ConsoleTopBar />
      <main id="main" class="main" tabindex="-1">
        <RouterView />
      </main>
    </div>
  </div>
  <ToastHost />
</template>
