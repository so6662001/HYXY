import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../views/Layout.vue'),
      redirect: '/inquiry',
      children: [
        {
          path: '/enterprise/:id',
          name: 'EnterpriseCreditCard',
          component: () => import('../views/credit/CreditCardPage.vue'),
          meta: { title: '企业信用名片' }
        },
        {
          path: '/credit/buyer/:id',
          name: 'BuyerCreditDetail',
          component: () => import('../views/credit/BuyerCreditDetail.vue'),
          meta: { title: '买家信用详情' }
        },
        {
          path: '/credit/seller/:id',
          name: 'SellerCreditDetail',
          component: () => import('../views/credit/SellerCreditDetail.vue'),
          meta: { title: '卖家信用详情' }
        },
        {
          path: '/risk/:id',
          name: 'RiskDetail',
          component: () => import('../views/risk/RiskDetailPage.vue'),
          meta: { title: '风险预警详情' }
        },
        {
          path: '/feedback/:id',
          name: 'FeedbackPage',
          component: () => import('../views/feedback/FeedbackPage.vue'),
          meta: { title: '合作印象评价' }
        },
        {
          path: '/inquiry',
          name: 'InquiryList',
          component: () => import('../views/inquiry/InquiryList.vue'),
          meta: { title: '询报价列表' }
        }
      ]
    }
  ]
})

export default router
