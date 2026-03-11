<template>
  <div class="credit-summary-card section-card">
    <div class="summary-top">
      <div class="grade-section">
        <GradeBadge :grade="credit.creditGrade" />
        <div class="grade-info">
          <div class="grade-score">信用得分 <strong>{{ credit.totalScore }}</strong> / 1000</div>
          <SufficiencyBar :level="credit.dataSufficiency" :label="credit.dataSufficiencyLabel" />
        </div>
      </div>

      <div class="data-sources">
        <div class="source-title">评级数据来源</div>
        <div class="source-list">
          <div v-for="ds in credit.dataSources" :key="ds.name" class="source-item">
            <el-icon :color="ds.available ? '#16a34a' : '#cbd5e1'">
              <component :is="ds.available ? 'CircleCheck' : 'Remove'" />
            </el-icon>
            <span>{{ ds.name }}</span>
            <span v-if="ds.detail" class="source-detail">({{ ds.detail }})</span>
          </div>
        </div>
      </div>
    </div>

    <div class="core-indicators" v-if="credit.coreIndicators?.length">
      <div class="section-title">
        {{ role === 'BUYER' ? '作为买家的核心指标' : '作为卖家的核心指标' }}
      </div>
      <ScoreProgress
        v-for="ind in credit.coreIndicators"
        :key="ind.code"
        :name="ind.name"
        :score="ind.score"
        :max-score="ind.maxScore"
        :percentage="ind.percentage"
        :level="ind.level"
      />
    </div>

    <div
      v-if="credit.overdueTag && credit.overdueTag !== 'NORMAL'"
      class="overdue-warning"
    >
      <el-alert
        :title="'⚠ ' + credit.overdueTagDesc"
        :type="overdueAlertType"
        :closable="false"
        show-icon
      />
    </div>

    <div class="summary-actions">
      <el-button type="primary" @click="goDetail" plain>
        查看{{ role === 'BUYER' ? '买家' : '卖家' }}信用详情
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { CreditSummary } from '../../../types/credit'
import GradeBadge from '../../../components/credit/GradeBadge.vue'
import SufficiencyBar from '../../../components/credit/SufficiencyBar.vue'
import ScoreProgress from '../../../components/credit/ScoreProgress.vue'

const props = defineProps<{
  title: string
  credit: CreditSummary
  role: string
  enterpriseId: number
}>()

const router = useRouter()

const overdueAlertType = computed(() => {
  if (!props.credit.overdueTag) return 'info'
  const map: Record<string, 'warning' | 'error' | 'info'> = {
    MILD: 'warning', MODERATE: 'warning', SEVERE: 'error', MALICIOUS: 'error'
  }
  return map[props.credit.overdueTag] || 'info'
})

function goDetail() {
  const name = props.role === 'BUYER' ? 'BuyerCreditDetail' : 'SellerCreditDetail'
  router.push({ name, params: { id: props.enterpriseId } })
}
</script>

<style scoped>
.summary-top {
  display: flex;
  gap: 40px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.grade-section {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.grade-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.grade-score {
  font-size: 16px;
  color: var(--text-secondary);
}

.grade-score strong {
  font-size: 24px;
  color: var(--text-primary);
}

.source-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.source-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.source-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.source-detail {
  color: var(--text-secondary);
  font-size: 12px;
}

.overdue-warning {
  margin: 16px 0;
}

.summary-actions {
  margin-top: 20px;
}

@media (max-width: 480px) {
  .summary-top {
    gap: 16px;
  }

  .grade-section {
    gap: 12px;
  }

  .grade-score strong {
    font-size: 20px;
  }

  .source-item {
    font-size: 14px;
  }
}
</style>
