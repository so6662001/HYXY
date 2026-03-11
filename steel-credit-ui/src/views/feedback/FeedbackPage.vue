<template>
  <div class="page-container">
    <div class="section-card">
      <h2>合作印象评价</h2>
      <p class="subtitle">为您的合作伙伴留下印象评价，帮助其他企业了解对方的合作表现</p>
    </div>

    <!-- 评价表单 -->
    <div class="section-card">
      <div class="section-title">提交评价</div>
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="被评方企业ID" prop="targetEnterpriseId">
          <el-input-number v-model="form.targetEnterpriseId" :min="1" />
        </el-form-item>

        <el-form-item label="评价角色">
          <el-radio-group v-model="evaluatorRole">
            <el-radio value="SELLER_RATE_BUYER">我是卖家，评价买家</el-radio>
            <el-radio value="BUYER_RATE_SELLER">我是买家，评价卖家</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="合作次数" prop="cooperationLevel">
          <el-radio-group v-model="form.cooperationLevel">
            <el-radio :value="1">1-2次</el-radio>
            <el-radio :value="2">3-5次</el-radio>
            <el-radio :value="3">5次以上</el-radio>
            <el-radio :value="4">长期合作</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="整体印象" prop="overallImpression">
          <el-radio-group v-model="form.overallImpression">
            <el-radio :value="4">😊 非常好</el-radio>
            <el-radio :value="3">🙂 不错</el-radio>
            <el-radio :value="2">😐 一般</el-radio>
            <el-radio :value="1">😟 较差</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 卖家评买家的分项 -->
        <template v-if="evaluatorRole === 'SELLER_RATE_BUYER'">
          <el-divider content-position="left">分项评价（选填）</el-divider>
          <el-form-item label="付款表现">
            <el-radio-group v-model="form.paymentRating">
              <el-radio :value="4">👍 从不拖欠</el-radio>
              <el-radio :value="3">👌 偶有小延迟</el-radio>
              <el-radio :value="2">😐 经常催款</el-radio>
              <el-radio :value="1">👎 严重拖欠</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="提货表现">
            <el-radio-group v-model="form.pickupRating">
              <el-radio :value="4">👍 按时足量</el-radio>
              <el-radio :value="3">👌 基本按时</el-radio>
              <el-radio :value="2">😐 常推迟</el-radio>
              <el-radio :value="1">👎 不配合</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="订单稳定性">
            <el-radio-group v-model="form.orderStabilityRating">
              <el-radio :value="4">👍 从不取消</el-radio>
              <el-radio :value="3">👌 偶尔调整</el-radio>
              <el-radio :value="2">😐 常改量</el-radio>
              <el-radio :value="1">👎 常取消</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <!-- 买家评卖家的分项 -->
        <template v-if="evaluatorRole === 'BUYER_RATE_SELLER'">
          <el-divider content-position="left">分项评价（选填）</el-divider>
          <el-form-item label="交货及时性">
            <el-radio-group v-model="form.deliveryRating">
              <el-radio :value="4">👍 准时可靠</el-radio>
              <el-radio :value="3">👌 基本准时</el-radio>
              <el-radio :value="2">😐 常延迟</el-radio>
              <el-radio :value="1">👎 严重延迟</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="货物品质">
            <el-radio-group v-model="form.qualityRating">
              <el-radio :value="4">👍 质量稳定</el-radio>
              <el-radio :value="3">👌 基本合格</el-radio>
              <el-radio :value="2">😐 偶有问题</el-radio>
              <el-radio :value="1">👎 常不达标</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="报价诚信">
            <el-radio-group v-model="form.pricingRating">
              <el-radio :value="4">👍 报实价</el-radio>
              <el-radio :value="3">👌 基本合理</el-radio>
              <el-radio :value="2">😐 偏高</el-radio>
              <el-radio :value="1">👎 虚报价格</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <el-form-item label="沟通配合">
          <el-radio-group v-model="form.communicationRating">
            <el-radio :value="4">👍 顺畅高效</el-radio>
            <el-radio :value="3">👌 一般</el-radio>
            <el-radio :value="2">😐 较被动</el-radio>
            <el-radio :value="1">👎 困难</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="继续合作意愿">
          <el-radio-group v-model="form.willContinue">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="2">视情况</el-radio>
            <el-radio :value="3">不太愿意</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="一句话评价">
          <el-input
            v-model="form.comment"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="选填，您的评价将匿名展示"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交评价</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 评价列表 -->
    <div class="section-card">
      <div class="section-title">已收到的评价</div>
      <div class="filter-row">
        <el-radio-group v-model="listRole" @change="loadFeedbacks">
          <el-radio-button value="SELLER_RATE_BUYER">卖家对买家的评价</el-radio-button>
          <el-radio-button value="BUYER_RATE_SELLER">买家对卖家的评价</el-radio-button>
        </el-radio-group>
      </div>

      <el-empty v-if="!feedbacks.length" description="暂无评价" :image-size="60" />
      <div v-for="fb in feedbacks" :key="fb.id" class="feedback-card">
        <div class="fb-header">
          <span class="fb-impression">{{ getImpressionIcon(fb.overallImpression) }} {{ getImpressionText(fb.overallImpression) }}</span>
          <span class="fb-level">合作: {{ getLevelText(fb.cooperationLevel) }}</span>
          <span class="fb-time">{{ fb.createTime }}</span>
        </div>
        <div class="fb-ratings">
          <template v-if="fb.evaluatorRole === 'SELLER_RATE_BUYER'">
            <el-tag v-if="fb.paymentRating" size="small" :type="ratingTagType(fb.paymentRating)">付款: {{ ratingText(fb.paymentRating) }}</el-tag>
            <el-tag v-if="fb.pickupRating" size="small" :type="ratingTagType(fb.pickupRating)">提货: {{ ratingText(fb.pickupRating) }}</el-tag>
            <el-tag v-if="fb.orderStabilityRating" size="small" :type="ratingTagType(fb.orderStabilityRating)">订单: {{ ratingText(fb.orderStabilityRating) }}</el-tag>
          </template>
          <template v-else>
            <el-tag v-if="fb.deliveryRating" size="small" :type="ratingTagType(fb.deliveryRating)">交货: {{ ratingText(fb.deliveryRating) }}</el-tag>
            <el-tag v-if="fb.qualityRating" size="small" :type="ratingTagType(fb.qualityRating)">质量: {{ ratingText(fb.qualityRating) }}</el-tag>
            <el-tag v-if="fb.pricingRating" size="small" :type="ratingTagType(fb.pricingRating)">报价: {{ ratingText(fb.pricingRating) }}</el-tag>
          </template>
          <el-tag v-if="fb.communicationRating" size="small" :type="ratingTagType(fb.communicationRating)">沟通: {{ ratingText(fb.communicationRating) }}</el-tag>
        </div>
        <div class="fb-comment" v-if="fb.comment">
          <q>{{ fb.comment }}</q>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { submitFeedback, getFeedbackList } from '../../api/feedback'
import type { CooperationFeedback, FeedbackSubmitRequest } from '../../types/credit'
import { getImpressionIcon, getImpressionText } from '../../utils/credit-helpers'

const route = useRoute()
const enterpriseId = computed(() => Number(route.params.id))
const evaluatorRole = ref('SELLER_RATE_BUYER')
const listRole = ref('SELLER_RATE_BUYER')
const feedbacks = ref<CooperationFeedback[]>([])
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<FeedbackSubmitRequest>({
  targetEnterpriseId: enterpriseId.value,
  cooperationLevel: 2,
  overallImpression: 3,
  triggerScene: 'VIEW_CREDIT'
})

watch(enterpriseId, (newId) => {
  form.targetEnterpriseId = newId
  loadFeedbacks()
})

const rules = {
  targetEnterpriseId: [{ required: true, message: '请填写被评方企业ID' }],
  cooperationLevel: [{ required: true, message: '请选择合作次数' }],
  overallImpression: [{ required: true, message: '请选择整体印象' }]
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await submitFeedback(enterpriseId.value, form)
    ElMessage.success('评价提交成功')
    loadFeedbacks()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

async function loadFeedbacks() {
  try {
    const res = await getFeedbackList(enterpriseId.value, listRole.value)
    feedbacks.value = res.data.data || []
  } catch (e) {
    console.error(e)
  }
}

function getLevelText(level: number): string {
  const map: Record<number, string> = { 1: '1-2次', 2: '3-5次', 3: '5次以上', 4: '长期合作' }
  return map[level] || ''
}

function ratingText(val: number): string {
  const map: Record<number, string> = { 4: '优', 3: '良', 2: '中', 1: '差' }
  return map[val] || ''
}

function ratingTagType(val: number): 'success' | 'warning' | 'danger' | 'info' {
  if (val >= 4) return 'success'
  if (val >= 3) return 'info'
  if (val >= 2) return 'warning'
  return 'danger'
}

onMounted(loadFeedbacks)
</script>

<style scoped>
.subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.filter-row {
  margin-bottom: 16px;
}

.feedback-card {
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  margin-bottom: 12px;
}

.fb-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.fb-impression {
  font-weight: 600;
}

.fb-level, .fb-time {
  font-size: 13px;
  color: var(--text-secondary);
}

.fb-ratings {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.fb-comment {
  font-style: italic;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
