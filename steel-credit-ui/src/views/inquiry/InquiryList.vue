<template>
  <div class="page-container">
    <div class="section-card">
      <h2>询报价列表</h2>
      <p class="subtitle">展示各企业信用角标：信用等级 + 数据充分度 + 风险状态</p>
    </div>

    <div v-if="loadError" class="section-card">
      <el-empty description="加载失败，请稍后重试">
        <el-button type="primary" @click="loadList">重试</el-button>
      </el-empty>
    </div>

    <!-- Desktop: 表格视图 -->
    <div class="section-card desktop-table" v-if="!loadError && !isMobile">
      <el-table :data="list" stripe style="width: 100%" v-loading="loading">
        <template #empty><el-empty description="暂无数据" :image-size="60" /></template>
        <el-table-column label="企业名称" min-width="180">
          <template #default="{ row }">
            <router-link :to="{ name: 'EnterpriseCreditCard', params: { id: row.enterpriseId } }" class="enterprise-link">
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
            <el-tag v-if="row.overdueTag && row.overdueTag !== 'NORMAL'" :type="row.hasSevereOverdue ? 'danger' : 'warning'" size="small" round>
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

    <!-- Mobile: 卡片视图 -->
    <div v-if="!loadError && isMobile" v-loading="loading">
      <el-empty v-if="!list.length && !loading" description="暂无数据" :image-size="60" />
      <div v-for="row in list" :key="row.enterpriseId" class="mobile-card section-card" @click="goCredit(row.enterpriseId)">
        <div class="mc-name">{{ row.enterpriseName || '企业 ' + row.enterpriseId }}</div>
        <div class="mc-badges">
          <div v-if="row.buyerGrade" class="mc-badge-item">
            <span class="mc-label">买家</span>
            <span class="mini-grade" :class="`grade-${row.buyerGrade}`">{{ row.buyerGrade }}</span>
            <SufficiencyBar v-if="row.buyerDataSufficiency" :level="row.buyerDataSufficiency" label="" />
          </div>
          <div v-if="row.sellerGrade" class="mc-badge-item">
            <span class="mc-label">卖家</span>
            <span class="mini-grade" :class="`grade-${row.sellerGrade}`">{{ row.sellerGrade }}</span>
            <SufficiencyBar v-if="row.sellerDataSufficiency" :level="row.sellerDataSufficiency" label="" />
          </div>
        </div>
        <div class="mc-tags">
          <RiskTag v-if="row.riskLevel" :level="row.riskLevel" effect="plain" />
          <el-tag v-if="row.overdueTag && row.overdueTag !== 'NORMAL'" :type="row.hasSevereOverdue ? 'danger' : 'warning'" size="small" round>
            {{ getOverdueLabel(row.overdueTag) }}
          </el-tag>
          <el-tag v-else type="success" size="small" round>正常</el-tag>
        </div>
        <div class="mc-actions">
          <el-button size="small" type="primary" link @click.stop="goCredit(row.enterpriseId)">信用详情</el-button>
          <el-button size="small" type="warning" link @click.stop="goFeedback(row.enterpriseId)">评价</el-button>
        </div>
      </div>
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { getCreditBadges } from '../../api/credit'
import type { CreditBadgeVO } from '../../types/credit'
import { getOverdueLabel } from '../../utils/credit-helpers'
import SufficiencyBar from '../../components/credit/SufficiencyBar.vue'
import RiskTag from '../../components/credit/RiskTag.vue'

const router = useRouter()
const list = ref<CreditBadgeVO[]>([])
const loading = ref(false)
const loadError = ref(false)

const windowWidth = ref(window.innerWidth)
function onResize() { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onBeforeUnmount(() => window.removeEventListener('resize', onResize))
const isMobile = computed(() => windowWidth.value < 768)

async function loadList() {
  loading.value = true
  loadError.value = false
  try {
    const ids = [1001, 1002, 1003, 1004, 1005]
    const res = await getCreditBadges(ids)
    list.value = res.data.data || []
  } catch {
    loadError.value = true
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadList)

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
  flex-wrap: wrap;
  gap: 16px 32px;
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
  font-size: 13px;
  color: #94a3b8;
}

/* === Mobile card layout === */
.mobile-card {
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.mobile-card:active {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.mc-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--credit-primary);
  margin-bottom: 10px;
}

.mc-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 10px;
}

.mc-badge-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mc-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.mc-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.mc-actions {
  display: flex;
  gap: 12px;
  border-top: 1px solid var(--border-color);
  padding-top: 10px;
}

@media (max-width: 480px) {
  .mini-grade {
    height: 28px;
    min-width: 32px;
    font-size: 11px;
  }

  .legend-row {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
