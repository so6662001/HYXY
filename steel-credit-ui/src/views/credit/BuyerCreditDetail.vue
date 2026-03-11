<template>
  <div class="page-container" v-if="detail">
    <!-- 评级概览 -->
    <div class="section-card overview-card">
      <div class="overview-top">
        <GradeBadge :grade="detail.creditGrade" />
        <div class="overview-info">
          <h2>{{ detail.enterpriseName }} · 买家信用详情</h2>
          <div class="overview-row">
            <span>信用得分 <strong>{{ detail.totalScore }}</strong> / 1000</span>
            <el-divider direction="vertical" />
            <span>评级模式: {{ detail.ratingModeDesc }}</span>
          </div>
          <div class="overview-row">
            <span>数据充分度:</span>
            <SufficiencyBar :level="detail.dataSufficiency" :label="detail.dataSufficiencyLabel" />
            <el-divider direction="vertical" />
            <span>等级天花板: {{ detail.gradeCeiling }}</span>
            <el-divider direction="vertical" />
            <span>评级日期: {{ detail.ratingDate }}</span>
          </div>
        </div>
      </div>
      <div class="data-source-row" v-if="detail.erpSourceCount != null">
        <el-tag size="small" type="info">ERP数据: {{ detail.erpSourceCount }}家商家</el-tag>
        <el-tag size="small" type="info">合作评价: {{ detail.feedbackCount }}条</el-tag>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-row">
      <div class="section-card chart-card">
        <div class="section-title">评分雷达图</div>
        <div ref="radarChartRef" class="chart-container"></div>
      </div>
      <div class="section-card chart-card">
        <div class="section-title">信用趋势</div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 维度得分 -->
    <div class="section-card">
      <div class="section-title">各维度得分明细</div>
      <el-table :data="detail.dimensions" stripe>
        <el-table-column prop="dimensionName" label="维度" width="160" />
        <el-table-column prop="dataSource" label="数据来源" width="80">
          <template #default="{ row }">
            <el-tag :type="row.dataSource === 'ERP' ? 'warning' : row.dataSource === '反馈' ? 'success' : 'info'" size="small">
              {{ row.dataSource }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="得分" width="120">
          <template #default="{ row }">
            {{ row.score }} / {{ row.maxScore }}
          </template>
        </el-table-column>
        <el-table-column label="占比" width="80">
          <template #default="{ row }">{{ row.percentage }}%</template>
        </el-table-column>
        <el-table-column label="进度" min-width="200">
          <template #default="{ row }">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: row.percentage + '%', background: getProgressColor(row.percentage) }" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="trend" label="趋势" width="60" align="center" />
      </el-table>
    </div>

    <!-- 合作方评价摘要 -->
    <div class="section-card" v-if="detail.feedbackSummaries?.length">
      <div class="section-title">合作方评价摘要</div>
      <div class="feedback-list">
        <div v-for="(fb, idx) in detail.feedbackSummaries" :key="idx" class="feedback-item">
          <span class="feedback-icon">{{ getImpressionIcon(fb.overallImpression) }}</span>
          <q class="feedback-comment">{{ fb.comment }}</q>
          <span class="feedback-source" v-if="fb.evaluatorType">— {{ fb.evaluatorType }}</span>
        </div>
      </div>
    </div>

    <!-- 缺失数据提示 -->
    <div class="section-card" v-if="detail.missingDataHints?.length">
      <div class="section-title">关键数据缺失提示</div>
      <div v-for="(hint, idx) in detail.missingDataHints" :key="idx" class="missing-hint">
        <el-icon color="#cbd5e1"><Remove /></el-icon>
        <span>{{ hint }}</span>
      </div>
    </div>

    <!-- 建议 -->
    <div class="section-card" v-if="detail.suggestions?.length">
      <div class="section-title">平台建议</div>
      <el-alert
        v-for="(s, idx) in detail.suggestions"
        :key="idx"
        :title="s"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 8px;"
      />
    </div>
  </div>

  <div v-else-if="loading" style="text-align: center; padding: 80px 0;">
    <el-icon class="is-loading" :size="32"><Loading /></el-icon>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { getBuyerCreditDetail } from '../../api/credit'
import type { CreditDetailVO } from '../../types/credit'
import { getProgressColor, getImpressionIcon } from '../../utils/credit-helpers'
import GradeBadge from '../../components/credit/GradeBadge.vue'
import SufficiencyBar from '../../components/credit/SufficiencyBar.vue'

const route = useRoute()
const detail = ref<CreditDetailVO | null>(null)
const loading = ref(false)
const radarChartRef = ref<HTMLDivElement>()
const trendChartRef = ref<HTMLDivElement>()

async function loadData() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    const res = await getBuyerCreditDetail(id)
    detail.value = res.data.data
    await nextTick()
    renderRadar()
    renderTrend()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
watch(() => route.params.id, loadData)

function renderRadar() {
  if (!radarChartRef.value || !detail.value) return
  const chart = echarts.init(radarChartRef.value)
  const dims = detail.value.dimensions
  chart.setOption({
    radar: {
      indicator: dims.map(d => ({ name: d.dimensionName, max: 100 })),
      shape: 'polygon',
      splitArea: { areaStyle: { color: ['#f0f9ff', '#e0f2fe', '#bae6fd', '#7dd3fc'] } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: dims.map(d => d.percentage),
        name: '买家信用',
        areaStyle: { color: 'rgba(37, 99, 235, 0.15)' },
        lineStyle: { color: '#2563eb' },
        itemStyle: { color: '#2563eb' }
      }]
    }]
  })
}

function renderTrend() {
  if (!trendChartRef.value || !detail.value?.trend?.length) return
  const chart = echarts.init(trendChartRef.value)
  const trend = [...detail.value.trend].reverse()
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trend.map(t => t.period) },
    yAxis: { type: 'value', min: 0, max: 1000 },
    series: [{
      type: 'line',
      data: trend.map(t => t.score),
      smooth: true,
      areaStyle: { color: 'rgba(37, 99, 235, 0.08)' },
      lineStyle: { color: '#2563eb', width: 2 },
      itemStyle: { color: '#2563eb' }
    }],
    grid: { left: 50, right: 20, top: 20, bottom: 30 }
  })
}
</script>

<style scoped>
.overview-card .overview-top {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.overview-info h2 {
  font-size: 20px;
  margin-bottom: 8px;
}

.overview-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.overview-row strong {
  font-size: 22px;
  color: var(--text-primary);
}

.data-source-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-container {
  height: 300px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feedback-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: 8px;
}

.feedback-icon {
  font-size: 18px;
}

.feedback-comment {
  font-style: italic;
  color: var(--text-secondary);
}

.feedback-source {
  font-size: 12px;
  color: #94a3b8;
}

.missing-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
</style>
