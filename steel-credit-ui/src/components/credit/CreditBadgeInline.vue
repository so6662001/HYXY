<template>
  <div class="badge-inline" v-if="badge">
    <div class="badge-row">
      <span v-if="badge.buyerGrade" class="mini-grade" :class="`grade-${badge.buyerGrade}`">
        {{ badge.buyerGrade }}
      </span>
      <span v-if="badge.sellerGrade" class="mini-grade" :class="`grade-${badge.sellerGrade}`">
        {{ badge.sellerGrade }}
      </span>
      <SufficiencyBar
        v-if="sufficiencyLevel > 0"
        :level="sufficiencyLevel"
        :label="''"
      />
      <RiskTag v-if="badge.riskLevel" :level="badge.riskLevel" effect="plain" />
      <el-tag
        v-if="badge.overdueTag && badge.overdueTag !== 'NORMAL'"
        type="danger"
        size="small"
        round
      >
        {{ overdueLabel }}
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CreditBadgeVO } from '../../types/credit'
import { getOverdueLabel } from '../../utils/credit-helpers'
import SufficiencyBar from './SufficiencyBar.vue'
import RiskTag from './RiskTag.vue'

const props = defineProps<{ badge: CreditBadgeVO }>()

const sufficiencyLevel = computed(() =>
  props.badge.buyerDataSufficiency || props.badge.sellerDataSufficiency || 0
)

const overdueLabel = computed(() =>
  props.badge.overdueTag ? getOverdueLabel(props.badge.overdueTag) : ''
)
</script>

<style scoped>
.badge-inline {
  display: inline-flex;
}

.badge-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-grade {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 22px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}
</style>
