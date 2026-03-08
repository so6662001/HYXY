# 数据库设计文档

> 数据库: MySQL 8.0+
>
> 字符集: utf8mb4
>
> 建表脚本: `src/main/resources/schema.sql`

## 表清单

共 17 张表，按功能域划分为 6 组。

### 一、企业基础

#### enterprise_info — 企业基本信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 (雪花ID) |
| enterprise_name | VARCHAR(200) | 企业名称 |
| credit_code | VARCHAR(50) | 统一社会信用代码 |
| role | INT | 角色: 1=买家 2=卖家 3=双角色 |
| main_products | VARCHAR(500) | 主营品类 |
| region | VARCHAR(200) | 所在地区 |
| join_date | DATE | 入驻日期 |
| license_verified | INT | 营业执照认证: 0=未 1=已 |
| qualification_verified | INT | 行业资质认证 |
| field_verified | INT | 实地认证 |
| introduction | TEXT | 企业简介 |
| contact_info | VARCHAR(500) | 联系方式 |
| business_scope | VARCHAR(1000) | 经营范围 |
| profile_completeness | INT | 信息完整度 (0-100) |
| erp_connected | INT | 是否接入ERP: 0=否 1=是 |

---

### 二、信用评级

#### buyer_credit_score — 买家信用评分主表

每次评分生成一条记录，支持历史追溯。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| enterprise_id | BIGINT | 企业ID |
| rating_period | VARCHAR(10) | 评级周期 (yyyy-MM) |
| rating_mode | VARCHAR(30) | FULL / PLATFORM_FEEDBACK / BASIC |
| total_score | INT | 总得分 (0-1000) |
| credit_grade | VARCHAR(10) | 信用等级 (AAA~D) |
| data_sufficiency | INT | 数据充分度 (1-5) |
| grade_ceiling | VARCHAR(10) | 等级天花板 |
| basic_score | INT | 企业基础实力得分 |
| activity_score | INT | 采购活跃度得分 |
| behavior_quality_score | INT | 采购行为质量得分 |
| relationship_score | INT | 合作关系持续性得分 |
| order_fulfillment_score | INT | 订单履约得分 (ERP) |
| pickup_fulfillment_score | INT | 提货履约得分 (ERP) |
| payment_credit_score | INT | 付款信用得分 (ERP) |
| feedback_score | INT | 合作反馈得分 |
| consistency_bonus | INT | 多商家ERP一致性加分 |
| score_details | JSON | 各维度计算明细 |
| erp_merchant_count | INT | ERP数据来源商家数 |
| feedback_count | INT | 合作反馈条数 |
| rating_date | DATE | 评定日期 |
| effective_date | DATE | 生效日期 |

索引: `idx_enterprise_period(enterprise_id, rating_period)`, `idx_enterprise_date(enterprise_id, rating_date)`

#### seller_credit_score — 卖家信用评分主表

结构与买家信用评分主表类似，维度字段不同:
- `quote_service_score` — 报价服务质量
- `supply_capability_score` — 供应能力
- `delivery_fulfillment_score` — 交货履约 (ERP)
- `after_sales_score` — 售后纠纷 (ERP)
- `consistency_score` — 多方一致性
- `erp_buyer_count` — ERP数据来源买家数

#### credit_score_history — 信用等级变更历史

仅当等级发生变化时插入记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| enterprise_id | BIGINT | 企业ID |
| role_type | VARCHAR(10) | BUYER / SELLER |
| previous_grade | VARCHAR(10) | 变更前等级 |
| previous_score | INT | 变更前得分 |
| current_grade | VARCHAR(10) | 变更后等级 |
| current_score | INT | 变更后得分 |
| change_reason | VARCHAR(500) | 变更原因 |
| rating_period | VARCHAR(10) | 评级周期 |

#### credit_score_detail — 信用评分明细

每个子指标的原始值和计算得分，支持评分过程完整追溯。

| 字段 | 类型 | 说明 |
|------|------|------|
| score_id | BIGINT | 关联评分主表ID |
| enterprise_id | BIGINT | 企业ID |
| role_type | VARCHAR(10) | BUYER / SELLER |
| dimension_code | VARCHAR(50) | 维度编码 |
| indicator_code | VARCHAR(50) | 子指标编码 |
| raw_value | DECIMAL(18,4) | 原始数据值 |
| max_score | INT | 满分 |
| calculated_score | INT | 计算得分 |
| data_source | VARCHAR(20) | PLATFORM / ERP / FEEDBACK |
| formula_version | VARCHAR(20) | 公式版本号 |

#### credit_scoring_config — 评分配置表

权重和公式可通过管理后台调整，支持版本化和生效日期控制。

| 字段 | 类型 | 说明 |
|------|------|------|
| role_type | VARCHAR(10) | BUYER / SELLER |
| rating_mode | VARCHAR(30) | FULL / PLATFORM_FEEDBACK / BASIC |
| dimension_code | VARCHAR(50) | 维度编码 |
| indicator_code | VARCHAR(50) | 子指标编码 |
| max_score | INT | 满分 |
| weight_percent | INT | 权重百分比 |
| formula_version | VARCHAR(20) | 公式版本 |
| effective_date | DATE | 生效日期 |
| expire_date | DATE | 失效日期 (null=永久) |
| enabled | INT | 是否启用 |

---

### 三、平台行为数据

#### inquiry_quote_stat — 询报价行为统计 (按月汇总)

| 字段 | 类型 | 说明 |
|------|------|------|
| enterprise_id | BIGINT | 企业ID |
| stat_month | VARCHAR(10) | 统计月份 (yyyy-MM) |
| inquiry_sent_count | INT | 发出询价数 |
| inquiry_received_count | INT | 收到询价数 |
| quote_sent_count | INT | 发出报价数 |
| quote_received_count | INT | 收到报价数 |
| inquiry_responded_count | INT | 响应询价数 |
| avg_response_hours | DECIMAL(10,2) | 平均响应时长 (小时) |
| quote_price_deviation_pct | DECIMAL(10,4) | 报价偏离市场均价% |
| distinct_partner_count | INT | 询报价伙伴数(去重) |
| inventory_update_count | INT | 库存更新次数 |

唯一索引: `uk_enterprise_month(enterprise_id, stat_month)`

#### enterprise_relation — 企业关系图谱

记录因询报价往来形成的业务关系。

| 字段 | 类型 | 说明 |
|------|------|------|
| enterprise_a_id | BIGINT | 企业A |
| enterprise_b_id | BIGINT | 企业B |
| first_interaction_time | DATETIME | 首次询报价时间 |
| last_interaction_time | DATETIME | 最近询报价时间 |
| total_interaction_count | INT | 累计询报价次数 |
| recent_90d_count | INT | 近90天询报价次数 |
| relation_months | INT | 关系持续月数 |
| active_status | INT | 活跃状态: 1=活跃 0=沉寂 |

---

### 四、ERP 对接数据

#### erp_order_record — ERP订单履约记录

从商家ERP系统同步的订单数据。

| 字段 | 类型 | 说明 |
|------|------|------|
| erp_order_no | VARCHAR(100) | ERP订单号 |
| buyer_enterprise_id | BIGINT | 买家企业ID |
| seller_enterprise_id | BIGINT | 卖家企业ID |
| order_amount | DECIMAL(18,2) | 订单金额 |
| order_quantity | DECIMAL(18,4) | 订单数量(吨) |
| order_date | DATE | 下单日期 |
| order_status | VARCHAR(30) | COMPLETED/CANCELLED/PARTIAL/IN_PROGRESS |
| cancelled | INT | 是否取消 |
| cancel_stage | VARCHAR(30) | 取消时点: BEFORE_STOCK/AFTER_STOCK |
| agreed_pickup_date | DATE | 约定提货日期 |
| actual_pickup_date | DATE | 实际提货日期 |
| agreed_pickup_quantity | DECIMAL(18,4) | 约定提货量 |
| actual_pickup_quantity | DECIMAL(18,4) | 实际提货量 |
| pickup_overdue | INT | 提货是否超期 |
| agreed_shipment_date | DATE | 约定发货日期 |
| shipment_date | DATE | 实际发货日期 |
| shipment_overdue | INT | 发货是否超期 |
| quality_passed | INT | 质量合格 |
| returned | INT | 是否退货 |
| sync_batch_no | VARCHAR(50) | 同步批次号 |

#### erp_payment_record — ERP付款记录

| 字段 | 类型 | 说明 |
|------|------|------|
| erp_order_no | VARCHAR(100) | ERP订单号 |
| buyer_enterprise_id | BIGINT | 买家企业ID |
| seller_enterprise_id | BIGINT | 卖家企业ID |
| payable_amount | DECIMAL(18,2) | 应付金额 |
| paid_amount | DECIMAL(18,2) | 实付金额 |
| agreed_payment_date | DATE | 约定付款日期 |
| actual_payment_date | DATE | 实际付款日期 |
| agreed_payment_days | INT | 约定账期(天) |
| overdue_days | INT | 超期天数 |
| severely_overdue | INT | 是否严重超期(>60天) |
| payment_status | VARCHAR(20) | PAID/UNPAID/PARTIAL |
| has_outstanding | INT | 当前是否有欠款 |

#### erp_overdue_summary — ERP欠款汇总 (每日快照)

按买家-卖家维度每日汇总欠款状态。

| 字段 | 类型 | 说明 |
|------|------|------|
| buyer_enterprise_id | BIGINT | 买家 |
| seller_enterprise_id | BIGINT | 卖家 |
| snapshot_date | DATE | 快照日期 |
| total_payable | DECIMAL(18,2) | 近12月应付总额 |
| total_paid | DECIMAL(18,2) | 近12月已付总额 |
| outstanding_balance | DECIMAL(18,2) | 当前欠款余额 |
| overdue_amount | DECIMAL(18,2) | 超期欠款金额 |
| max_overdue_days | INT | 最大超期天数 |
| overdue_count | INT | 超期次数 |
| severe_overdue_count | INT | 严重超期次数 |
| overdue_level | VARCHAR(20) | 欠款等级 |

#### buyer_overdue_tag — 买家欠款标记

| 字段 | 类型 | 说明 |
|------|------|------|
| buyer_enterprise_id | BIGINT | 买家 |
| overdue_level | VARCHAR(20) | NORMAL/MILD/MODERATE/SEVERE/MALICIOUS |
| tag_reason | VARCHAR(500) | 标记原因 |
| tag_time | DATETIME | 标记时间 |
| active_tag | INT | 是否当前生效 |
| overdue_count | INT | 超期次数 |
| max_overdue_days | INT | 最大超期天数 |

#### erp_sync_log — ERP同步日志

| 字段 | 类型 | 说明 |
|------|------|------|
| sync_batch_no | VARCHAR(50) | 同步批次号 |
| seller_enterprise_id | BIGINT | 商家 |
| sync_time | DATETIME | 同步时间 |
| data_type | VARCHAR(20) | ORDER/PAYMENT/OVERDUE |
| record_count | INT | 同步记录数 |
| sync_status | VARCHAR(20) | SUCCESS/FAILED/PARTIAL |
| error_message | TEXT | 异常信息 |

---

### 五、合作评价

#### cooperation_feedback — 合作印象评价

| 字段 | 类型 | 说明 |
|------|------|------|
| evaluator_enterprise_id | BIGINT | 评价方 |
| target_enterprise_id | BIGINT | 被评方 |
| evaluator_role | VARCHAR(30) | BUYER_RATE_SELLER / SELLER_RATE_BUYER |
| cooperation_level | INT | 合作次数: 1/2/3/4 |
| overall_impression | INT | 整体印象: 4=非常好 ~ 1=较差 |
| payment_rating | INT | 付款 (卖家评买家) |
| pickup_rating | INT | 提货 (卖家评买家) |
| order_stability_rating | INT | 订单稳定性 (卖家评买家) |
| delivery_rating | INT | 交货 (买家评卖家) |
| quality_rating | INT | 质量 (买家评卖家) |
| pricing_rating | INT | 报价诚信 (买家评卖家) |
| communication_rating | INT | 沟通 (通用) |
| will_continue | INT | 继续合作: 1=是 2=视情况 3=不愿意 |
| comment | VARCHAR(500) | 一句话评价 |
| trigger_scene | VARCHAR(30) | 触发场景 |
| credibility_weight | DOUBLE | 可信度权重 (系统计算) |
| valid | INT | 是否有效 |

---

### 六、风险预警

#### risk_event — 风险事件 (企查查)

| 字段 | 类型 | 说明 |
|------|------|------|
| enterprise_id | BIGINT | 企业ID |
| risk_category | VARCHAR(30) | JUDICIAL/OPERATIONAL/FINANCIAL/CHANGE |
| risk_level | VARCHAR(20) | HIGH/MEDIUM/LOW/NONE |
| event_title | VARCHAR(500) | 事件标题 |
| event_detail | JSON | 事件详情 |
| involved_amount | DECIMAL(18,2) | 涉案金额 |
| event_date | DATE | 事件日期 |
| discovery_date | DATE | 发现日期 |
| event_status | VARCHAR(50) | 状态 |
| is_related_party | INT | 0=自身 1=关联方 |
| related_party_name | VARCHAR(200) | 关联方名称 |

#### related_party_info — 关联方信息

| 字段 | 类型 | 说明 |
|------|------|------|
| enterprise_id | BIGINT | 企业ID |
| party_name | VARCHAR(200) | 关联方名称 |
| relation_type | VARCHAR(30) | SHAREHOLDER/LEGAL_PERSON/CONTROLLER/GUARANTOR |
| shareholding_ratio | DECIMAL(10,4) | 持股比例 |
| relation_level | INT | 1=直接 2=间接 |
| has_risk | INT | 是否有风险 |

#### risk_snapshot — 风险快照 (每日)

| 字段 | 类型 | 说明 |
|------|------|------|
| enterprise_id | BIGINT | 企业ID |
| snapshot_date | DATE | 快照日期 |
| overall_risk_level | VARCHAR(20) | 综合风险等级 |
| high/medium/low_risk_count | INT | 各级别数量 |
| self_risk_count | INT | 自身风险数 |
| related_party_risk_count | INT | 关联方风险数 |
| risk_summary | JSON | 风险汇总 |

#### risk_alert_log — 风险预警推送记录

| 字段 | 类型 | 说明 |
|------|------|------|
| receiver_enterprise_id | BIGINT | 接收方 |
| target_enterprise_id | BIGINT | 被预警企业 |
| risk_event_id | BIGINT | 风险事件ID |
| channel | VARCHAR(20) | SITE/SMS/EMAIL |
| alert_time | DATETIME | 推送时间 |
| read_status | INT | 是否已读 |

---

## ER 关系概要

```
enterprise_info (1) ─── (N) buyer_credit_score
                    ─── (N) seller_credit_score
                    ─── (N) credit_score_history
                    ─── (N) inquiry_quote_stat
                    ─── (N) risk_event
                    ─── (N) risk_snapshot
                    ─── (N) related_party_info

enterprise_relation: enterprise_a_id → enterprise_info.id
                     enterprise_b_id → enterprise_info.id

erp_order_record:    buyer_enterprise_id  → enterprise_info.id
                     seller_enterprise_id → enterprise_info.id

erp_payment_record:  buyer_enterprise_id  → enterprise_info.id
                     seller_enterprise_id → enterprise_info.id

cooperation_feedback: evaluator_enterprise_id → enterprise_info.id
                      target_enterprise_id    → enterprise_info.id
```
