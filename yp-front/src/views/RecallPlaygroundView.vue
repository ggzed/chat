<script setup lang="ts">
import { computed, ref } from 'vue'
import { RECALL } from '@/data/console'
import { useUiStore } from '@/stores/ui'
import PageHead from '@/components/ui/PageHead.vue'
import PageTabs from '@/components/ui/PageTabs.vue'
import PanelBox from '@/components/ui/PanelBox.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const ui = useUiStore()

const tabs = [
  { label: '文档列表', to: '/kb/docs' },
  { label: '召回测试', to: '/kb/recall' }
]

const query = ref('单笔超过 50 万的转账需要什么审批？')
const index = ref('risk_rules_v3')
const model = ref('bge-m3 · 混合检索')
const threshold = ref(0.72)
const loading = ref(false)
const executed = ref(true)
const latency = ref(842)
const hits = ref(RECALL.filter((item) => item.score >= threshold.value).length)

const results = computed(() => {
  if (loading.value) return []
  return RECALL.filter((item) => item.score >= threshold.value)
})

function run() {
  loading.value = true
  executed.value = true
  const start = performance.now()
  window.setTimeout(() => {
    latency.value = Math.round(performance.now() - start) + 780
    hits.value = results.value.length
    loading.value = false
    ui.toast(`召回完成 · ${hits.value} 条命中 · ${latency.value}ms`, 'ok')
  }, 760)
}

function onThreshold() {
  hits.value = results.value.length
}
</script>

<template>
  <PageHead
    eyebrow="KNOWLEDGE / 召回测试"
    title="召回测试台"
    sub="调参不影响线上配置；确认效果后可回到 Agent 配置中同步阈值与 Top-K。"
  >
    <template #actions>
      <button class="btn" type="button" @click="ui.toast('已保存为草稿参数（演示）', 'ok')">
        <AppIcon name="file" :size="14" />保存草稿
      </button>
      <button class="btn btn-primary" type="button" :disabled="loading" @click="run">
        <AppIcon name="play" :size="14" />{{ loading ? '检索中…' : '执行召回' }}
      </button>
    </template>
  </PageHead>

  <PageTabs :items="tabs" />

  <div class="grid g-main">
    <PanelBox title="检索参数" meta="PLAYGROUND">
      <div class="field">
        <label class="field-label" for="recall-query">查询语句</label>
        <textarea id="recall-query" v-model="query" class="textarea" style="min-height: 84px"></textarea>
      </div>

      <div class="split-2">
        <div class="field">
          <label class="field-label" for="recall-index">索引库</label>
          <select id="recall-index" v-model="index" class="select">
            <option value="risk_rules_v3">risk_rules_v3 · 风控规则库</option>
            <option value="collection_rules">collection_rules · 催收合规库</option>
          </select>
        </div>
        <div class="field">
          <label class="field-label" for="recall-model">向量模型</label>
          <select id="recall-model" v-model="model" class="select">
            <option>bge-m3 · 混合检索</option>
            <option>bge-large-zh · 纯向量</option>
            <option>BM25 · 纯关键词</option>
          </select>
        </div>
      </div>

      <div class="field" style="margin-bottom: 0">
        <label class="field-label" for="recall-threshold">
          相似度阈值 <span class="inline-val">{{ threshold.toFixed(2) }}</span>
        </label>
        <input
          id="recall-threshold"
          v-model.number="threshold"
          class="range"
          type="range"
          min="0.3"
          max="0.95"
          step="0.01"
          @input="onThreshold"
        />
        <div style="display: flex; justify-content: space-between" class="hint">
          <span>0.30 · 召回优先</span>
          <span>0.95 · 精确优先</span>
        </div>
      </div>

      <div class="params" style="border-bottom: 0; padding-bottom: 0; margin: 14px 0 0">
        <span class="param">index={{ index }}</span>
        <span class="param">top_k=5</span>
        <span class="param">model=bge-m3</span>
        <span class="param">hybrid=true</span>
        <span class="param">threshold={{ threshold.toFixed(2) }}</span>
      </div>
    </PanelBox>

    <PanelBox title="召回结果" :meta="`${hits} HITS`">
      <div style="display: flex; gap: 16px; margin-bottom: 14px; flex-wrap: wrap">
        <div>
          <div class="hint">检索耗时</div>
          <b class="mono" style="font-size: 15px; color: var(--brand)">{{ latency }} ms</b>
        </div>
        <div>
          <div class="hint">命中片段</div>
          <b class="mono" style="font-size: 15px">{{ hits }} 条</b>
        </div>
        <div>
          <div class="hint">上下文 Token</div>
          <b class="mono" style="font-size: 15px">{{ hits * 186 }}</b>
        </div>
      </div>

      <div v-if="loading" style="padding-top: 4px">
        <div class="skeleton" style="width: 42%"></div>
        <div class="skeleton" style="width: 88%"></div>
        <div class="skeleton" style="width: 76%"></div>
        <div class="skeleton" style="width: 58%; margin-bottom: 18px"></div>
        <div class="skeleton" style="width: 36%"></div>
        <div class="skeleton" style="width: 82%"></div>
        <div class="skeleton" style="width: 64%; margin-bottom: 0"></div>
      </div>

      <template v-else>
        <article v-for="item in results" :key="item.src" class="recall-item">
          <div class="ri-head">
            <span class="ri-rank">#{{ item.rank }}</span>
            <span class="ri-src">{{ item.src }}</span>
            <span class="tag tag-brand">score {{ item.score.toFixed(2) }}</span>
          </div>
          <p class="ri-txt" v-html="item.txt"></p>
          <div class="ri-scores">
            <div>BM25<b>{{ item.bm25 }}</b></div>
            <div>KNN<b>{{ item.knn }}</b></div>
            <div style="margin-left: auto; align-self: center">
              <button class="btn btn-sm btn-quiet" type="button" @click="ui.toast('已复制片段到剪贴板（演示）', 'ok')">引用</button>
            </div>
          </div>
        </article>

        <div v-if="!results.length" class="notice warn">
          <AppIcon name="alert" :size="15" />
          <span>当前阈值下没有命中片段。可降低阈值，或检查文档是否已完成向量化。</span>
        </div>
      </template>

      <p v-if="executed && !loading" class="hint" style="margin-top: 6px">
        阈值调整会实时过滤结果，便于观察召回与精确度的平衡点。
      </p>
    </PanelBox>
  </div>
</template>
