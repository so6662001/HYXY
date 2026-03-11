<template>
  <div class="page-container" v-if="detail">
    <div class="section-card">
      <h2>{{ detail.enterpriseName }} · 风险预警详情</h2>
      <div class="disclaimer-text" style="margin-top: 8px;">
        {{ detail.disclaimer }}
      </div>
    </div>

    <!-- 风险概览 -->
    <div class="section-card">
      <div class="section-title">风险概览</div>
      <div class="risk-overview-row">
        <RiskTag :level="detail.summary.overallRiskLevel" />
        <div class="risk-stats">
          <span>🔴 高危: <strong>{{ detail.summary.highRiskCount }}</strong></span>
          <span>🟠 中危: <strong>{{ detail.summary.mediumRiskCount }}</strong></span>
          <span>🟡 低危: <strong>{{ detail.summary.lowRiskCount }}</strong></span>
          <span>总计: <strong>{{ detail.summary.totalRiskCount }}</strong></span>
        </div>
      </div>
    </div>

    <!-- 企业自身风险 -->
    <div class="section-card">
      <div class="section-title">企业自身风险事件 ({{ detail.selfRiskEvents.length }}条)</div>
      <el-empty v-if="!detail.selfRiskEvents.length" description="暂无企业自身风险信息" :image-size="60" />
      <div v-for="ev in detail.selfRiskEvents" :key="ev.eventId" class="risk-event-item">
        <div class="event-header">
          <el-tag
            :type="ev.riskLevel === 'HIGH' ? 'danger' : ev.riskLevel === 'MEDIUM' ? 'warning' : 'info'"
            size="small"
          >
            {{ ev.riskLevelLabel }}
          </el-tag>
          <span class="event-title">{{ ev.eventTitle }}</span>
          <span class="event-date">{{ ev.eventDate }}</span>
        </div>
        <div class="event-meta">
          <span>类型: {{ ev.riskCategoryDesc }}</span>
          <span v-if="ev.involvedAmount">涉案金额: ¥{{ ev.involvedAmount?.toLocaleString() }}</span>
          <span v-if="ev.eventStatus">状态: {{ ev.eventStatus }}</span>
        </div>
      </div>
    </div>

    <!-- 关联方风险 -->
    <div class="section-card">
      <div class="section-title">关联方风险事件 ({{ detail.relatedPartyRiskEvents.length }}条)</div>
      <el-empty v-if="!detail.relatedPartyRiskEvents.length" description="暂无关联方风险信息" :image-size="60" />
      <div v-for="ev in detail.relatedPartyRiskEvents" :key="ev.eventId" class="risk-event-item">
        <div class="event-header">
          <el-tag
            :type="ev.riskLevel === 'HIGH' ? 'danger' : ev.riskLevel === 'MEDIUM' ? 'warning' : 'info'"
            size="small"
          >
            {{ ev.riskLevelLabel }}
          </el-tag>
          <span class="event-title">{{ ev.eventTitle }}</span>
          <span class="event-date">{{ ev.eventDate }}</span>
        </div>
        <div class="event-meta">
          <span>关联方: {{ ev.relatedPartyName }}</span>
          <span>类型: {{ ev.riskCategoryDesc }}</span>
          <span v-if="ev.involvedAmount">金额: ¥{{ ev.involvedAmount?.toLocaleString() }}</span>
        </div>
      </div>
    </div>

    <!-- 关联方图谱 -->
    <div class="section-card" v-if="detail.relatedParties?.length">
      <div class="section-title">关联方信息</div>
      <el-table :data="detail.relatedParties" stripe>
        <el-table-column prop="partyName" label="关联方名称" min-width="180" />
        <el-table-column prop="relationTypeDesc" label="关联类型" width="120" />
        <el-table-column label="持股比例" width="100">
          <template #default="{ row }">
            {{ row.shareholdingRatio ? (row.shareholdingRatio * 100).toFixed(1) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="关联层级" width="100">
          <template #default="{ row }">
            {{ row.relationLevel === 1 ? '直接关联' : '间接关联' }}
          </template>
        </el-table-column>
        <el-table-column label="风险状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.hasRisk ? 'danger' : 'success'" size="small">
              {{ row.hasRisk ? '有风险' : '无风险' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <div v-else-if="loading" style="text-align: center; padding: 80px 0;">
    <el-icon class="is-loading" :size="32"><Loading /></el-icon>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getRiskDetail } from '../../api/risk'
import type { RiskDetailVO } from '../../types/credit'
import RiskTag from '../../components/credit/RiskTag.vue'

const route = useRoute()
const detail = ref<RiskDetailVO | null>(null)
const loading = ref(false)

async function loadData() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    const res = await getRiskDetail(id)
    detail.value = res.data.data
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
watch(() => route.params.id, loadData)
</script>

<style scoped>
.risk-overview-row {
  display: flex;
  align-items: center;
  gap: 24px;
}

.risk-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--text-secondary);
}

.risk-stats strong {
  color: var(--text-primary);
}

.risk-event-item {
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  margin-bottom: 12px;
}

.event-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.event-title {
  font-weight: 500;
  flex: 1;
}

.event-date {
  font-size: 13px;
  color: var(--text-secondary);
}

.event-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
