<script setup lang="ts">
import { ref, watch } from 'vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const props = defineProps<{
  open: boolean
  toolName: string
  toolCode: string
}>()

const emit = defineEmits<{ cancel: []; confirm: [] }>()

const ack = ref(false)

watch(
  () => props.open,
  (open) => {
    if (open) ack.value = false
  }
)
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-backdrop" style="z-index: 120" @click.self="emit('cancel')">
      <div class="modal confirm-modal" role="alertdialog" aria-modal="true" aria-labelledby="confirm-title">
        <div class="modal-head confirm-head">
          <AppIcon name="alert" :size="18" />
          <div>
            <h3 id="confirm-title">高危工具二次确认</h3>
            <div class="mh-sub">该操作会影响真实资金，请确认已理解风险</div>
          </div>
        </div>
        <div class="confirm-body">
          <div class="danger-note">
            <b>{{ toolName }}</b>（<span class="mono">{{ toolCode }}</span>）属于写操作。挂载后 Agent 可在对话中直接触发资金类动作，必须同时启用「高危阻断」护栏，并写入合规审计日志。
          </div>
          <label class="check-row" :class="{ 'is-checked': ack }">
            <input v-model="ack" type="checkbox" />
            <span class="cr-body">
              <span class="cr-t">我已确认风险并知悉审计要求</span>
              <span class="cr-d">确认后该工具会被加入挂载列表，发布前还需配置护栏。</span>
            </span>
          </label>
        </div>
        <div class="modal-foot">
          <span class="mf-spacer"></span>
          <button class="btn" type="button" @click="emit('cancel')">取消</button>
          <button class="btn btn-danger" type="button" :disabled="!ack" @click="emit('confirm')">确认挂载</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
