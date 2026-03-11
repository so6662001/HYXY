# 钢铁交易平台 · 企业信用系统前端

基于 Vue3 + TypeScript + Element Plus + ECharts 构建的企业信用评级与风险预警前端应用。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 7.x | 构建工具 |
| Element Plus | 最新 | UI 组件库 |
| ECharts | 最新 | 图表（雷达图、趋势图） |
| Vue Router | 4.x | 路由 |
| Pinia | 最新 | 状态管理 |
| Axios | 最新 | HTTP 请求 |

## 快速开始

```bash
# 安装依赖
npm install

# 开发模式（端口 3000，自动代理到后端 8080）
npm run dev

# 生产构建
npm run build

# 类型检查
npx vue-tsc --noEmit
```

## 项目结构

```
src/
├── api/                            # API 请求封装
│   ├── credit.ts                   # 信用评级 API
│   ├── risk.ts                     # 风险预警 API
│   └── feedback.ts                 # 合作评价 API
├── types/
│   └── credit.ts                   # TypeScript 类型定义
├── utils/
│   ├── request.ts                  # Axios 封装
│   └── credit-helpers.ts           # 信用/风险相关工具函数
├── components/credit/              # 可复用信用组件
│   ├── GradeBadge.vue              # 信用等级徽章
│   ├── SufficiencyBar.vue          # 数据充分度指示器
│   ├── ScoreProgress.vue           # 维度得分进度条
│   ├── RiskTag.vue                 # 风险等级标签
│   └── CreditBadgeInline.vue       # 询报价列表行内信用角标
├── views/
│   ├── Layout.vue                  # 全局布局
│   ├── credit/
│   │   ├── CreditCardPage.vue      # 企业主页（信用名片 Tab）
│   │   ├── BuyerCreditDetail.vue   # 买家信用详情（雷达图+趋势图+维度表格）
│   │   ├── SellerCreditDetail.vue  # 卖家信用详情
│   │   └── components/
│   │       ├── CreditSummaryCard.vue  # 信用概要卡片
│   │       └── RiskSummaryCard.vue    # 风险预警概要卡片
│   ├── risk/
│   │   └── RiskDetailPage.vue      # 风险预警详情页
│   ├── feedback/
│   │   └── FeedbackPage.vue        # 合作评价页（角色化表单+列表）
│   └── inquiry/
│       └── InquiryList.vue         # 询报价列表（信用角标展示）
├── router/index.ts                 # 路由配置
├── assets/styles/global.css        # 全局样式（信用/风险色系变量）
└── main.ts                         # 入口文件
```

## 页面说明

| 路由 | 页面 | 功能 |
|------|------|------|
| `/enterprise/:id` | 企业信用名片 | 买家信用 + 卖家信用 + 风险预警，Tab 切换展示 |
| `/credit/buyer/:id` | 买家信用详情 | 雷达图、趋势折线图、各维度得分表格、评价摘要、缺失数据提示 |
| `/credit/seller/:id` | 卖家信用详情 | 同上，维度和色系不同 |
| `/risk/:id` | 风险预警详情 | 风险概览、企业自身风险、关联方风险、关联方表格 |
| `/feedback/:id` | 合作印象评价 | 角色化评价表单（卖家评买家/买家评卖家）+ 评价列表 |
| `/inquiry` | 询报价列表 | 信用等级角标、数据充分度、风险状态、欠款标记 |

## 设计要点

- **信用评级与风险预警视觉分离**：信用区域使用蓝绿色系，风险区域使用橙红色系
- **数据充分度可视化**：5 格方块指示器，实心为已有数据
- **买卖双角色 Tab 切换**：同一企业可查看买家信用和卖家信用两张画像
- **角色化评价表单**：根据评价方角色动态展示不同的分项评价维度
- **ECharts 图表**：雷达图展示各维度占比，折线图展示信用趋势
