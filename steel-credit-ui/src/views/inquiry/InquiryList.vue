<template>
  <div class="page-container">
    <div class="section-card">
      <h2>询报价列表</h2>
      <p class="subtitle">展示各企业信用角标：信用等级 + 数据充分度 + 风险状态</p>
    </div>

    <div class="section-card">
      <el-table :data="list" stripe style="width: 100%">
        <el-table-column label="企业名称" min-width="180">
          <template #default="{ row }">
            <router-link
              :to="{ name: 'EnterpriseCreditCard', params: { id: row.enterpriseId } }"
              class="enterprise-link"
            >
              {{ row.enterpriseName || '企业 ' + row.enterpriseId }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column label="买家信用" width="160">
          <template #default="{ row }">
            <div class="badge-cell" v-if="row.buyerGrade">
              <span class="mini-grade" :class="`grade-${row.buyerGrade}`">{{ row.buyerGrade }}</span>
              <SufficiencyBar v-if="row.buyerDataSufficiency" :level="row.buyerDataSufficiency" label="" />
            </div>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="卖家信用" width="160">
          <template #default="{ row }">
            <div class="badge-cell" v-if="row.sellerGrade">
              <span class="mini-grade" :class="`grade-${row.sellerGrade}`">{{ row.sellerGrade }}</span>
              <SufficiencyBar v-if="row.sellerDataSufficiency" :level="row.sellerDataSufficiency" label="" />
            </div>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="风险预警" width="120">
          <template #default="{ row }">
            <RiskTag v-if="row.riskLevel" :level="row.riskLevel" effect="plain" />
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="欠款标记" width="120">
          <template #default="{ row }">
            <el-tag
              v-if="row.overdueTag && row.overdueTag !== 'NORMAL'"
              :type="row.hasSevereOverdue ? 'danger' : 'warning'"
              size="small"
              round
            >
              {{ getOverdueLabel(row.overdueTag) }}
            </el-tag>
            <span v-else class="no-data">正常</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="goCredit(row.enterpriseId)">信用详情</el-button>
            <el-button size="small" type="warning" link @click="goFeedback(row.enterpriseId)">评价</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="section-card legend-card">
      <div class="section-title">图例说明</div>
      <div class="legend-row">
        <div class="legend-item">
          <span class="mini-grade grade-AA">AA</span>
          <span>信用等级</span>
        </div>
        <div class="legend-item">
          <SufficiencyBar :level="3" label="" />
          <span>数据充分度</span>
        </div>
        <div class="legend-item">
          <RiskTag level="NONE" effect="plain" />
          <span>风险预警状态</span>
        </div>
      </div>
      <p class="legend-note">信用等级基于平台交易行为评定 | 风险预警基于公开信息，二者独立</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCreditBadges } from '../../api/credit'
import type { CreditBadgeVO } from '../../types/credit'
import { getOverdueLabel } from '../../utils/credit-helpers'
import SufficiencyBar from '../../components/credit/SufficiencyBar.vue'
import RiskTag from '../../components/credit/RiskTag.vue'

const router = useRouter()
const list = ref<CreditBadgeVO[]>([])

onMounted(async () => {
  try {
    const ids = [1001, 1002, 1003, 1004, 1005]
    const res = await getCreditBadges(ids)
    list.value = res.data.data || []
  } catch (e) {
    list.value = [
      { enterpriseId: 1001, enterpriseName: '鼎盛钢铁', buyerGrade: 'AA', buyerDataSufficiency: 5, sellerGrade: 'A', sellerDataSufficiency: 3, riskLevel: 'NONE', overdueTag: 'NORMAL', hasSevereOverdue: false },
      { enterpriseId: 1002, enterpriseName: '宏达贸易', buyerGrade: 'BBB', buyerDataSufficiency: 2, riskLevel: 'LOW', overdueTag: 'MILD', hasSevereOverdue: false },
      { enterpriseId: 1003, enterpriseName: '新锐工贸', buyerGrade: 'B', buyerDataSufficiency: 1, riskLevel: 'MEDIUM', overdueTag: 'SEVERE', hasSevereOverdue: true },
      { enterpriseId: 1004, enterpriseName: '长江钢材', sellerGrade: 'AAA', sellerDataSufficiency: 5, riskLevel: 'NONE' },
      { enterpriseId: 1005, enterpriseName: '明辉实业', buyerGrade: 'A', buyerDataSufficiency: 3, sellerGrade: 'AA', sellerDataSufficiency: 4, riskLevel: 'NONE', overdueTag: 'NORMAL', hasSevereOverdue: false }
    ]
  }
})

function goCredit(id: number) {
  router.push({ name: 'EnterpriseCreditCard', params: { id } })
}

function goFeedback(id: number) {
  router.push({ name: 'FeedbackPage', params: { id } })
}
</script>

<style scoped>
.subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.enterprise-link {
  color: var(--credit-primary);
  text-decoration: none;
  font-weight: 500;
}

.enterprise-link:hover {
  text-decoration: underline;
}

.badge-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-grade {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 24px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  padding: 0 6px;
}

.no-data {
  color: #cbd5e1;
}

.legend-card {
  background: #f8fafc;
}

.legend-row {
  display: flex;
  gap: 32px;
  margin-bottom: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.legend-note {
  font-size: 12px;
  color: #94a3b8;
}
</style>
