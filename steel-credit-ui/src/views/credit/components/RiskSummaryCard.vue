<template>
  <div class="risk-summary-card section-card">
    <div class="risk-header">
      <RiskTag :level="risk.overallRiskLevel" />
      <span class="risk-check-date" v-if="risk.lastCheckDate">
        最近检查: {{ risk.lastCheckDate }}
      </span>
    </div>

    <div class="risk-counts" v-if="risk.totalRiskCount > 0">
      <div class="risk-count-item">
        <span class="count-label">🔴 高危</span>
        <span class="count-value">{{ risk.highRiskCount }}</span>
      </div>
      <div class="risk-count-item">
        <span class="count-label">🟠 中危</span>
        <span class="count-value">{{ risk.mediumRiskCount }}</span>
      </div>
      <div class="risk-count-item">
        <span class="count-label">🟡 低危</span>
        <span class="count-value">{{ risk.lowRiskCount }}</span>
      </div>
      <div class="risk-count-item">
        <span class="count-label">总计</span>
        <span class="count-value">{{ risk.totalRiskCount }}</span>
      </div>
    </div>

    <div v-if="risk.totalRiskCount > 0" class="risk-breakdown">
      <span>企业自身风险: {{ risk.selfRiskCount }} 条</span>
      <el-divider direction="vertical" />
      <span>关联方风险: {{ risk.relatedPartyRiskCount }} 条</span>
    </div>

    <div class="disclaimer-text">
      {{ risk.disclaimer || '风险预警基于公开工商司法信息，与信用评级相互独立，仅供参考。' }}
    </div>

    <div class="risk-actions">
      <el-button @click="goDetail" plain>查看风险预警详情</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { RiskSummaryVO } from '../../../types/credit'
import RiskTag from '../../../components/credit/RiskTag.vue'

const props = defineProps<{
  risk: RiskSummaryVO
  enterpriseId: number
}>()

const router = useRouter()

function goDetail() {
  router.push({ name: 'RiskDetail', params: { id: props.enterpriseId } })
}
</script>

<style scoped>
.risk-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.risk-check-date {
  font-size: 13px;
  color: var(--text-secondary);
}

.risk-counts {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
}

.risk-count-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.count-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.count-value {
  font-size: 22px;
  font-weight: 700;
}

.risk-breakdown {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.risk-actions {
  margin-top: 16px;
}
</style>
