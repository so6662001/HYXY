<template>
  <div class="page-container">
    <div class="enterprise-header section-card" v-if="card">
      <h2 class="enterprise-name">{{ card.enterpriseName }}</h2>
      <div class="enterprise-meta">
        <el-tag type="info" size="small">
          {{ card.role === 'BUYER' ? '买家' : card.role === 'SELLER' ? '卖家' : '买卖双角色' }}
        </el-tag>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="credit-tabs" v-if="card">
      <!-- 买家信用 Tab -->
      <el-tab-pane
        v-if="card.buyerCredit"
        label="买家信用"
        name="buyer"
      >
        <CreditSummaryCard
          title="买家信用"
          :credit="card.buyerCredit"
          role="BUYER"
          :enterprise-id="Number(route.params.id)"
        />
      </el-tab-pane>

      <!-- 卖家信用 Tab -->
      <el-tab-pane
        v-if="card.sellerCredit"
        label="卖家信用"
        name="seller"
      >
        <CreditSummaryCard
          title="卖家信用"
          :credit="card.sellerCredit"
          role="SELLER"
          :enterprise-id="Number(route.params.id)"
        />
      </el-tab-pane>

      <!-- 风险预警 Tab -->
      <el-tab-pane v-if="card.riskSummary" label="风险预警" name="risk">
        <RiskSummaryCard
          :risk="card.riskSummary"
          :enterprise-id="Number(route.params.id)"
        />
      </el-tab-pane>
    </el-tabs>

    <el-empty v-if="!card && !loading" description="未找到企业信息" />
    <div v-if="loading" style="text-align: center; padding: 80px 0;">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCreditCard } from '../../api/credit'
import type { CreditCardVO } from '../../types/credit'
import CreditSummaryCard from './components/CreditSummaryCard.vue'
import RiskSummaryCard from './components/RiskSummaryCard.vue'

const route = useRoute()
const card = ref<CreditCardVO | null>(null)
const loading = ref(false)
const activeTab = ref('buyer')

async function loadData() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    const res = await getCreditCard(id)
    card.value = res.data.data
    if (card.value?.buyerCredit) activeTab.value = 'buyer'
    else if (card.value?.sellerCredit) activeTab.value = 'seller'
    else activeTab.value = 'risk'
  } catch {
    // error displayed by response interceptor
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
watch(() => route.params.id, loadData)
</script>

<style scoped>
.enterprise-header {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.enterprise-name {
  font-size: 22px;
  font-weight: 700;
  word-break: break-all;
}

.credit-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

@media (max-width: 480px) {
  .enterprise-name {
    font-size: 18px;
  }

  .enterprise-header {
    gap: 8px;
  }
}
</style>
