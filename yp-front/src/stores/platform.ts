import { ref } from 'vue'
import { defineStore } from 'pinia'
import { AGENTS, DOCS } from '@/data/console'
import type { AgentItem, DocItem } from '@/data/console'

/** Agent 与知识库文档的内存态，发布/上传后即时回写列表。 */
export const usePlatformStore = defineStore('platform', () => {
  const agents = ref<AgentItem[]>(AGENTS.map((item) => ({ ...item })))
  const docs = ref<DocItem[]>(DOCS.map((item) => ({ ...item })))

  function addAgent(agent: AgentItem) {
    agents.value.unshift(agent)
  }

  function uploadDoc() {
    const doc: DocItem = {
      name: '风控规则补充条款_2026Q4.pdf',
      type: 'PDF',
      size: '1.2 MB',
      chunks: 0,
      status: '解析中',
      index: '—',
      by: '11-06'
    }
    docs.value.unshift(doc)
    window.setTimeout(() => {
      doc.chunks = 88
      doc.status = '已向量化'
      doc.index = 'risk_rules_v3'
    }, 1800)
    return doc
  }

  return { agents, docs, addAgent, uploadDoc }
})
