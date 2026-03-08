# API 接口文档

> 基础路径: `http://localhost:8080/api`
>
> 在线文档: 启动后访问 `http://localhost:8080/api/doc.html`（Knife4j）

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 业务异常 |
| 500 | 系统内部错误 |

---

## 一、信用评级接口 (`/credit`)

### 1.1 获取企业信用名片

用于企业主页展示，包含买家信用 + 卖家信用 + 风险概况。

```
GET /credit/card/{enterpriseId}
```

**响应示例：**

```json
{
  "code": 200,
  "data": {
    "enterpriseId": 1001,
    "enterpriseName": "鼎盛建材有限公司",
    "role": "BOTH",
    "buyerCredit": {
      "creditGrade": "A",
      "totalScore": 738,
      "dataSufficiency": 4,
      "dataSufficiencyLabel": "较充分",
      "ratingMode": "FULL",
      "dataSources": [
        { "name": "平台询报价数据", "available": true },
        { "name": "ERP履约数据", "available": true, "detail": "2家商家" },
        { "name": "合作方评价", "available": true, "detail": "8条" }
      ],
      "coreIndicators": [
        { "name": "付款信用", "code": "PAYMENT", "score": 218, "maxScore": 280, "percentage": 78, "level": "中等偏上" },
        { "name": "订单履约", "code": "ORDER", "score": 102, "maxScore": 120, "percentage": 85, "level": "优秀" },
        { "name": "提货履约", "code": "PICKUP", "score": 92, "maxScore": 100, "percentage": 92, "level": "优秀" }
      ],
      "overdueTag": "MILD",
      "overdueTagDesc": "偶有超期，超期≤15天，已结清"
    },
    "sellerCredit": {
      "creditGrade": "AA",
      "totalScore": 812,
      "dataSufficiency": 3,
      "dataSufficiencyLabel": "中等",
      "ratingMode": "PLATFORM_FEEDBACK",
      "dataSources": [
        { "name": "平台询报价数据", "available": true },
        { "name": "ERP履约数据", "available": false },
        { "name": "合作方评价", "available": true, "detail": "15条" }
      ],
      "coreIndicators": [
        { "name": "报价服务", "code": "QUOTE_SERVICE", "score": 135, "maxScore": 150, "percentage": 90, "level": "优秀" },
        { "name": "供应能力", "code": "SUPPLY", "score": 80, "maxScore": 100, "percentage": 80, "level": "优秀" },
        { "name": "合作评价", "code": "FEEDBACK", "score": 132, "maxScore": 150, "percentage": 88, "level": "优秀" }
      ]
    },
    "riskSummary": {
      "enterpriseId": 1001,
      "overallRiskLevel": "LOW",
      "overallRiskLabel": "低危提示",
      "highRiskCount": 0,
      "mediumRiskCount": 0,
      "lowRiskCount": 2,
      "totalRiskCount": 2,
      "selfRiskCount": 1,
      "relatedPartyRiskCount": 1,
      "lastCheckDate": "2026-03-07",
      "disclaimer": "以下信息来源于公开工商司法数据，仅供参考，不构成信用评级依据。风险预警与信用评级相互独立。"
    }
  }
}
```

### 1.2 获取买家信用详情

```
GET /credit/buyer/detail/{enterpriseId}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| creditGrade | String | 信用等级 |
| totalScore | Integer | 总分 (0-1000) |
| ratingMode | String | 评级模式: FULL / PLATFORM_FEEDBACK / BASIC |
| ratingModeDesc | String | 评级模式描述 |
| dataSufficiency | Integer | 数据充分度 (1-5) |
| dataSufficiencyLabel | String | 数据充分度标签 |
| gradeCeiling | String | 等级天花板 |
| dimensions | Array | 各维度得分明细 |
| trend | Array | 信用趋势 (近12月) |
| gradeHistory | Array | 等级变更历史 |
| feedbackSummaries | Array | 合作方评价摘要 (最多3条) |
| missingDataHints | Array | 缺失数据提示 |
| suggestions | Array | 建议 |

### 1.3 获取卖家信用详情

```
GET /credit/seller/detail/{enterpriseId}
```

响应结构与买家信用详情一致，维度不同。

### 1.4 批量获取信用角标

用于询报价列表，批量获取多个企业的信用等级、风险状态、欠款标记等角标信息。

```
POST /credit/badges
Content-Type: application/json

[1001, 1002, 1003]
```

**响应示例：**

```json
{
  "code": 200,
  "data": [
    {
      "enterpriseId": 1001,
      "enterpriseName": "鼎盛建材",
      "buyerGrade": "A",
      "buyerDataSufficiency": 4,
      "sellerGrade": "AA",
      "sellerDataSufficiency": 3,
      "riskLevel": "LOW",
      "overdueTag": "MILD",
      "hasSevereOverdue": false
    }
  ]
}
```

### 1.5 手动触发信用评分计算

```
POST /credit/calculate/{enterpriseId}
```

### 1.6 手动触发全量评分计算（管理员）

```
POST /credit/calculate/all
```

---

## 二、风险预警接口 (`/risk`)

> 风险预警与信用评级完全独立，数据来源于企查查公开信息。

### 2.1 获取风险预警概况

```
GET /risk/summary/{enterpriseId}
```

### 2.2 获取风险预警详情

```
GET /risk/detail/{enterpriseId}
```

**响应示例：**

```json
{
  "code": 200,
  "data": {
    "enterpriseId": 1001,
    "enterpriseName": "鼎盛建材有限公司",
    "summary": { "..." : "同 summary 接口" },
    "selfRiskEvents": [
      {
        "eventId": 5001,
        "riskCategory": "JUDICIAL",
        "riskCategoryDesc": "司法风险",
        "riskLevel": "LOW",
        "riskLevelLabel": "低危提示",
        "eventTitle": "小额民事纠纷",
        "involvedAmount": 120000,
        "eventDate": "2025-11-20",
        "eventStatus": "已结案"
      }
    ],
    "relatedPartyRiskEvents": [],
    "relatedParties": [
      {
        "partyId": 3001,
        "partyName": "张某",
        "relationType": "SHAREHOLDER",
        "relationTypeDesc": "股东",
        "shareholdingRatio": 0.60,
        "relationLevel": 1,
        "hasRisk": false
      }
    ],
    "disclaimer": "以下信息来源于公开工商司法数据，仅供参考，不构成信用评级依据。风险预警与信用评级相互独立。"
  }
}
```

### 2.3 手动刷新风险信息

```
POST /risk/refresh/{enterpriseId}
```

---

## 三、合作印象评价接口 (`/feedback`)

### 3.1 提交合作印象评价

```
POST /feedback/submit/{evaluatorEnterpriseId}
Content-Type: application/json
```

**请求体（卖家评买家示例）：**

```json
{
  "targetEnterpriseId": 1002,
  "cooperationLevel": 3,
  "overallImpression": 3,
  "paymentRating": 2,
  "pickupRating": 3,
  "orderStabilityRating": 3,
  "communicationRating": 4,
  "willContinue": 1,
  "comment": "付款偶有拖延，但催一下都会给",
  "triggerScene": "RE_INQUIRY"
}
```

**请求体（买家评卖家示例）：**

```json
{
  "targetEnterpriseId": 1003,
  "cooperationLevel": 4,
  "overallImpression": 4,
  "deliveryRating": 4,
  "qualityRating": 4,
  "pricingRating": 3,
  "communicationRating": 4,
  "willContinue": 1,
  "comment": "长期合作，交货稳定",
  "triggerScene": "PERIODIC"
}
```

**请求体字段说明：**

| 字段 | 必填 | 类型 | 说明 |
|------|:---:|------|------|
| targetEnterpriseId | 是 | Long | 被评方企业ID |
| cooperationLevel | 是 | Int(1-4) | 合作次数: 1=1-2次 2=3-5次 3=5+次 4=长期 |
| overallImpression | 是 | Int(1-4) | 整体印象: 4=非常好 3=不错 2=一般 1=较差 |
| paymentRating | 否 | Int(1-4) | 付款表现（卖家评买家） |
| pickupRating | 否 | Int(1-4) | 提货表现（卖家评买家） |
| orderStabilityRating | 否 | Int(1-4) | 订单稳定性（卖家评买家） |
| deliveryRating | 否 | Int(1-4) | 交货及时（买家评卖家） |
| qualityRating | 否 | Int(1-4) | 货物品质（买家评卖家） |
| pricingRating | 否 | Int(1-4) | 报价诚信（买家评卖家） |
| communicationRating | 否 | Int(1-4) | 沟通配合 |
| willContinue | 否 | Int(1-3) | 继续合作意愿: 1=是 2=视情况 3=不愿意 |
| comment | 否 | String | 一句话评价（≤500字） |
| triggerScene | 否 | String | 触发场景: RE_INQUIRY / VIEW_CREDIT / PERIODIC / INVITATION |

### 3.2 获取评价列表

```
GET /feedback/list/{targetEnterpriseId}?evaluatorRole=SELLER_RATE_BUYER
```

| 参数 | 说明 |
|------|------|
| evaluatorRole | `SELLER_RATE_BUYER`（卖家对买家的评价）或 `BUYER_RATE_SELLER`（买家对卖家的评价） |

### 3.3 检查是否可以评价

```
GET /feedback/can-evaluate?evaluatorId=1001&targetId=1002
```

返回 `true`/`false`，只有有询报价往来记录的双方才能互评。

---

## 四、ERP 数据同步接口 (`/erp/sync`)

### 4.1 同步订单数据

```
POST /erp/sync/orders/{sellerEnterpriseId}
Content-Type: application/json
```

**请求体：**

```json
[
  {
    "erpOrderNo": "ERP20260301001",
    "buyerEnterpriseId": 1002,
    "sellerEnterpriseId": 2001,
    "orderAmount": 385000.00,
    "orderQuantity": 100.0,
    "orderDate": "2026-03-01",
    "orderStatus": "COMPLETED",
    "cancelled": false,
    "agreedPickupDate": "2026-03-05",
    "actualPickupDate": "2026-03-04",
    "agreedPickupQuantity": 100.0,
    "actualPickupQuantity": 100.0,
    "agreedShipmentDate": "2026-03-03",
    "shipmentDate": "2026-03-03",
    "qualityPassed": true,
    "returned": false
  }
]
```

### 4.2 同步付款数据

```
POST /erp/sync/payments/{sellerEnterpriseId}
Content-Type: application/json
```

**请求体：**

```json
[
  {
    "erpOrderNo": "ERP20260301001",
    "buyerEnterpriseId": 1002,
    "sellerEnterpriseId": 2001,
    "payableAmount": 385000.00,
    "paidAmount": 385000.00,
    "agreedPaymentDate": "2026-04-01",
    "actualPaymentDate": "2026-03-28",
    "agreedPaymentDays": 30,
    "paymentStatus": "PAID"
  }
]
```

同步完成后，系统自动更新相关买家的欠款等级标记。
