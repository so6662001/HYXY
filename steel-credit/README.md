# 钢铁行业交易平台 — 企业信用评级与风险预警系统

## 项目概述

面向钢铁行业 B2B 询报价交易平台的企业信用评级与风险预警系统。平台当前以询报价撮合为核心功能，实际交易在线下完成。系统通过多维数据采集与自适应评分模型，为买卖双方提供可信赖的信用参考与风险提示。

### 核心设计原则

- **买卖双角色分立评级** — 买家信用与卖家信用独立评估，维度和权重针对角色定制
- **信用评级与风险预警完全分离** — 两套独立体系、独立数据源、独立 API，互不干扰
- **数据充分度透明展示** — 坦诚告知信用评级的数据基础，不掩盖不夸大
- **自适应评级模式** — 根据实际可用数据自动调整权重，确保无 ERP 也能建立信用

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 开发语言 |
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis-Plus | 3.5.6 | ORM 框架 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 缓存（评级数据缓存预留） |
| Knife4j | 4.4.0 | API 文档 (Swagger) |
| Hutool | 5.8.27 | 工具库 |
| Lombok | - | 代码简化 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+（可选）

### 1. 建库建表

```bash
mysql -u root -p -e "CREATE DATABASE steel_credit DEFAULT CHARSET utf8mb4;"
mysql -u root -p steel_credit < src/main/resources/schema.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库和 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/steel_credit?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. 编译运行

```bash
mvn clean package -DskipTests
java -jar target/steel-credit-1.0.0-SNAPSHOT.jar
```

### 4. 访问 API 文档

启动后访问 Knife4j 接口文档：

```
http://localhost:8080/api/doc.html
```

## 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                     前端 (Vue3)                               │
│  信用名片 / 信用详情 / 风险预警 / 询报价信用角标 / 合作评价      │
└────────────────────────┬─────────────────────────────────────┘
                         │ HTTP REST
┌────────────────────────▼─────────────────────────────────────┐
│                   Controller 层                               │
│  /api/credit/**    /api/risk/**   /api/feedback/**            │
│  /api/erp/sync/**                                            │
├──────────────────────────────────────────────────────────────┤
│                   Service 层                                  │
│  ┌─────────────────┐ ┌──────────────┐ ┌────────────────────┐ │
│  │ CreditRating    │ │ RiskWarning  │ │ Cooperation        │ │
│  │ Service         │ │ Service      │ │ FeedbackService    │ │
│  └────────┬────────┘ └──────────────┘ └────────────────────┘ │
│           │                                                   │
│  ┌────────▼────────────────────────┐  ┌────────────────────┐ │
│  │  评分引擎 (Engine)              │  │ ErpDataSync        │ │
│  │  BuyerCreditEngine              │  │ Service            │ │
│  │  SellerCreditEngine             │  │                    │ │
│  └─────────────────────────────────┘  └────────────────────┘ │
├──────────────────────────────────────────────────────────────┤
│                   Mapper 层 (MyBatis-Plus)                    │
├──────────────────────────────────────────────────────────────┤
│                   MySQL (17 张表)                             │
└──────────────────────────────────────────────────────────────┘
         ▲                               ▲
         │ 定时同步                       │ API 对接
    ┌────┴────┐                    ┌─────┴─────┐
    │ ERP 系统 │                    │  企查查 API │
    └─────────┘                    └───────────┘
```

## 模块说明

### 一、信用评级模块 (`/api/credit`)

#### 买家信用评分维度

| 维度 | 权重(全量模式) | 数据来源 | 核心指标 |
|------|:---:|------|------|
| 企业基础实力 | 6% | 平台 | 资料完整度、资质认证、入驻时长 |
| 采购活跃度 | 6% | 平台 | 询价频率、伙伴数量、活跃连续性 |
| 采购行为质量 | 6% | 平台 | 询价认真度、报价查看率 |
| 合作关系持续性 | 5% | 平台 | 回头询价率、长期合作伙伴数 |
| **订单履约** | **12%** | **ERP** | 订单取消率、部分提货率、取消时点 |
| **提货履约** | **10%** | **ERP** | 提货及时率、足量提货率 |
| **付款信用** | **28%** | **ERP** | 付款及时率、超期欠款率、严重超期次数、当前欠款状态 |
| 合作反馈 | 12% | 反馈 | 合作方印象评分、继续合作意愿 |
| 多商家一致性 | 15% | ERP | 跨商家表现一致性验证 |

#### 卖家信用评分维度

| 维度 | 权重(全量模式) | 数据来源 | 核心指标 |
|------|:---:|------|------|
| 企业基础实力 | 10% | 平台 | 资料完整度、资质认证、入驻时长 |
| 报价服务质量 | 15% | 平台 | 响应率、响应速度、报价合理性 |
| 供应能力 | 10% | 平台 | 被询价热度、库存维护、品类覆盖 |
| 交货履约 | 20% | ERP | 按时发货率、足量发货率、质量合格率 |
| 售后与纠纷 | 10% | ERP | 退货率、质量异议率 |
| 合作反馈 | 15% | 反馈 | 买家评价的交货/质量/诚信 |
| 关系持续性 | 10% | 平台 | 回头客比例、长期客户数 |
| 多方一致性 | 10% | 综合 | 多买家评价的一致性 |

#### 三种评级模式（自适应切换）

| 模式 | 触发条件 | 信用等级天花板 | 数据充分度 |
|------|----------|:---:|------|
| **全量模式** | ERP 记录 ≥ 3 笔 | AAA | 充分 |
| **平台+反馈模式** | 无 ERP，合作反馈 ≥ 5 条 | AA | 中等 |
| **基础模式** | 仅有平台行为数据 | A | 有限 |

#### 信用等级映射

| 等级 | 分数区间 | 含义 |
|------|----------|------|
| AAA | 900-1000 | 极优 |
| AA | 800-899 | 优秀 |
| A | 700-799 | 良好 |
| BBB | 600-699 | 中等偏上 |
| BB | 500-599 | 中等 |
| B | 400-499 | 一般 |
| C | 200-399 | 较差 |
| D | 0-199 | 差 |

### 二、风险预警模块 (`/api/risk`)

**与信用评级完全独立。** 数据来源于企查查 API（已预留对接接口），涵盖：

- **企业自身风险** — 司法风险、经营风险、财务风险、变更风险
- **关联方穿透** — 控股股东、实际控制人、法定代表人及其关联企业的风险
- **四级风险等级** — 高危 / 中危 / 低危 / 暂无风险

### 三、合作印象评价模块 (`/api/feedback`)

角色化评价设计：
- **卖家评买家**：付款表现、提货表现、订单稳定性、沟通配合度
- **买家评卖家**：交货及时性、货物品质、报价诚信、沟通配合度

防刷机制：
- 仅有询报价往来记录的双方才能互评
- 同一对企业间评价次数上限 4 次
- 评价可信度权重基于询报价往来次数自动计算

### 四、ERP 数据同步模块 (`/api/erp/sync`)

- 批量同步订单数据（提货/取消/质量等）
- 批量同步付款数据（付款/超期/欠款等）
- 自动计算买家欠款等级标记（正常 / 轻度 / 中度 / 严重 / 恶意）

### 五、定时任务

| 任务 | 执行时间 | 说明 |
|------|----------|------|
| 信用评分批量计算 | 每日 02:00 | 重算所有企业的买家/卖家信用评分 |
| 风险信息批量刷新 | 每日 03:00 | 从企查查拉取最新风险数据 |

## API 接口一览

### 信用评级

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/credit/card/{enterpriseId}` | 获取企业信用名片（买家+卖家+风险概况） |
| GET | `/api/credit/buyer/detail/{enterpriseId}` | 获取买家信用详情 |
| GET | `/api/credit/seller/detail/{enterpriseId}` | 获取卖家信用详情 |
| POST | `/api/credit/badges` | 批量获取信用角标（询报价列表用） |
| POST | `/api/credit/calculate/{enterpriseId}` | 手动触发单企业评分计算 |
| POST | `/api/credit/calculate/all` | 手动触发全量评分计算（管理员） |

### 风险预警

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/risk/summary/{enterpriseId}` | 获取风险预警概况 |
| GET | `/api/risk/detail/{enterpriseId}` | 获取风险预警详情（含关联方） |
| POST | `/api/risk/refresh/{enterpriseId}` | 手动刷新风险信息 |

### 合作评价

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/feedback/submit/{evaluatorEnterpriseId}` | 提交合作印象评价 |
| GET | `/api/feedback/list/{targetEnterpriseId}?evaluatorRole=` | 获取评价列表 |
| GET | `/api/feedback/can-evaluate?evaluatorId=&targetId=` | 检查是否可以评价 |

### ERP 数据同步

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/erp/sync/orders/{sellerEnterpriseId}` | 同步 ERP 订单数据 |
| POST | `/api/erp/sync/payments/{sellerEnterpriseId}` | 同步 ERP 付款数据 |

## 数据库设计

共 17 张表，按功能域划分：

| 功能域 | 表名 | 说明 |
|--------|------|------|
| **企业基础** | `enterprise_info` | 企业基本信息 |
| **信用评级** | `buyer_credit_score` | 买家信用评分主表 |
| | `seller_credit_score` | 卖家信用评分主表 |
| | `credit_score_detail` | 信用评分明细（可追溯） |
| | `credit_score_history` | 信用等级变更历史 |
| | `credit_scoring_config` | 评分配置（权重/公式可配） |
| **平台行为** | `inquiry_quote_stat` | 询报价行为统计（按月） |
| | `enterprise_relation` | 企业关系图谱 |
| **ERP 数据** | `erp_order_record` | ERP 订单履约记录 |
| | `erp_payment_record` | ERP 付款记录 |
| | `erp_overdue_summary` | ERP 欠款汇总（每日快照） |
| | `erp_sync_log` | ERP 同步日志 |
| | `buyer_overdue_tag` | 买家欠款等级标记 |
| **合作评价** | `cooperation_feedback` | 合作印象评价 |
| **风险预警** | `risk_event` | 风险事件（企查查） |
| | `related_party_info` | 关联方信息 |
| | `risk_snapshot` | 风险快照（每日） |
| | `risk_alert_log` | 风险预警推送记录 |

完整建表脚本：`src/main/resources/schema.sql`

## 项目结构

```
src/main/java/com/steel/credit/
├── SteelCreditApplication.java         # 启动类
├── config/                              # 配置
│   ├── MyBatisPlusConfig.java           # MyBatis-Plus 分页+自动填充
│   └── GlobalExceptionHandler.java      # 全局异常处理
├── constant/                            # 枚举常量
│   ├── CreditGrade.java                 # 信用等级 (AAA~D)
│   ├── EnterpriseRole.java              # 企业角色 (买家/卖家/双角色)
│   ├── DataSufficiency.java             # 数据充分度 (5级)
│   ├── RatingMode.java                  # 评级模式 (全量/平台+反馈/基础)
│   ├── RiskLevel.java                   # 风险等级 (高危/中危/低危/无)
│   ├── RiskCategory.java                # 风险类别
│   ├── OverdueLevel.java                # 欠款等级 (5级)
│   └── FeedbackTrigger.java             # 评价触发场景
├── entity/                              # 数据实体 (18个)
├── mapper/                              # MyBatis-Plus Mapper (18个)
├── dto/
│   ├── request/                         # 请求对象
│   │   ├── FeedbackSubmitRequest.java
│   │   ├── ErpOrderSyncRequest.java
│   │   └── ErpPaymentSyncRequest.java
│   └── response/                        # 响应对象
│       ├── Result.java                  # 统一返回体
│       ├── CreditCardVO.java            # 信用名片
│       ├── CreditDetailVO.java          # 信用详情
│       ├── RiskSummaryVO.java           # 风险概况
│       ├── RiskDetailVO.java            # 风险详情
│       └── InquiryCreditBadgeVO.java    # 信用角标
├── service/
│   ├── CreditRatingService.java         # 信用评级接口
│   ├── RiskWarningService.java          # 风险预警接口
│   ├── CooperationFeedbackService.java  # 合作评价接口
│   ├── ErpDataSyncService.java          # ERP同步接口
│   ├── impl/                            # 服务实现
│   └── engine/                          # 评分引擎
│       ├── BuyerCreditEngine.java       # 买家信用评分引擎
│       └── SellerCreditEngine.java      # 卖家信用评分引擎
├── controller/                          # REST 控制器
│   ├── CreditRatingController.java      # /api/credit/**
│   ├── RiskWarningController.java       # /api/risk/**
│   ├── CooperationFeedbackController.java
│   └── ErpDataSyncController.java       # /api/erp/sync/**
└── task/
    └── CreditScoringTask.java           # 定时任务
```

## 待对接事项

| 事项 | 说明 | 位置 |
|------|------|------|
| 企查查 API 对接 | 拉取企业风险、关联方信息 | `RiskWarningServiceImpl.refreshRiskInfo()` |
| ERP 系统主动推送 | 可改为 ERP 主动调用同步接口 | `ErpDataSyncController` |
| 消息推送 | 评级变更/风险预警通知 | Service 层预留 |
| Redis 缓存 | 信用评分结果缓存 | 依赖已引入，逻辑待加 |
| 询报价行为统计汇总 | 从平台主业务库定时汇总到 `inquiry_quote_stat` | 需对接平台主库 |
