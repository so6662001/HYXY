-- ============================================================
-- 钢铁行业交易平台 - 企业信用评级与风险预警系统 数据库建表脚本
-- ============================================================

-- 企业基本信息表
CREATE TABLE IF NOT EXISTS `enterprise_info` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `enterprise_name` VARCHAR(200) NOT NULL COMMENT '企业名称',
    `credit_code` VARCHAR(50) COMMENT '统一社会信用代码',
    `role` INT DEFAULT 3 COMMENT '企业角色: 1=买家 2=卖家 3=双角色',
    `main_products` VARCHAR(500) COMMENT '主营品类',
    `region` VARCHAR(200) COMMENT '所在地区',
    `join_date` DATE COMMENT '入驻日期',
    `license_verified` INT DEFAULT 0 COMMENT '营业执照认证: 0=未 1=已',
    `qualification_verified` INT DEFAULT 0 COMMENT '行业资质认证: 0=未 1=已',
    `field_verified` INT DEFAULT 0 COMMENT '实地认证: 0=未 1=已',
    `introduction` TEXT COMMENT '企业简介',
    `contact_info` VARCHAR(500) COMMENT '联系方式',
    `business_scope` VARCHAR(1000) COMMENT '经营范围',
    `profile_completeness` INT DEFAULT 0 COMMENT '信息完整度(0-100)',
    `erp_connected` INT DEFAULT 0 COMMENT '是否接入ERP: 0=否 1=是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_credit_code` (`credit_code`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业基本信息';

-- 买家信用评分主表
CREATE TABLE IF NOT EXISTS `buyer_credit_score` (
    `id` BIGINT NOT NULL,
    `enterprise_id` BIGINT NOT NULL COMMENT '企业ID',
    `rating_period` VARCHAR(10) NOT NULL COMMENT '评级周期(yyyy-MM)',
    `rating_mode` VARCHAR(30) NOT NULL COMMENT '评级模式: FULL/PLATFORM_FEEDBACK/BASIC',
    `total_score` INT DEFAULT 0 COMMENT '总得分(0-1000)',
    `credit_grade` VARCHAR(10) COMMENT '信用等级',
    `data_sufficiency` INT DEFAULT 1 COMMENT '数据充分度(1-5)',
    `grade_ceiling` VARCHAR(10) COMMENT '等级天花板',
    `basic_score` INT DEFAULT 0 COMMENT '企业基础实力得分',
    `activity_score` INT DEFAULT 0 COMMENT '采购活跃度得分',
    `behavior_quality_score` INT DEFAULT 0 COMMENT '采购行为质量得分',
    `relationship_score` INT DEFAULT 0 COMMENT '合作关系持续性得分',
    `order_fulfillment_score` INT DEFAULT 0 COMMENT '订单履约得分(ERP)',
    `pickup_fulfillment_score` INT DEFAULT 0 COMMENT '提货履约得分(ERP)',
    `payment_credit_score` INT DEFAULT 0 COMMENT '付款信用得分(ERP)',
    `feedback_score` INT DEFAULT 0 COMMENT '合作反馈得分',
    `consistency_bonus` INT DEFAULT 0 COMMENT '多商家ERP一致性加分',
    `score_details` JSON COMMENT '各维度明细(JSON)',
    `erp_merchant_count` INT DEFAULT 0 COMMENT 'ERP数据来源商家数',
    `feedback_count` INT DEFAULT 0 COMMENT '合作反馈条数',
    `rating_date` DATE COMMENT '评定日期',
    `effective_date` DATE COMMENT '生效日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_enterprise_period` (`enterprise_id`, `rating_period`),
    INDEX `idx_enterprise_date` (`enterprise_id`, `rating_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='买家信用评分主表';

-- 卖家信用评分主表
CREATE TABLE IF NOT EXISTS `seller_credit_score` (
    `id` BIGINT NOT NULL,
    `enterprise_id` BIGINT NOT NULL,
    `rating_period` VARCHAR(10) NOT NULL,
    `rating_mode` VARCHAR(30) NOT NULL,
    `total_score` INT DEFAULT 0,
    `credit_grade` VARCHAR(10),
    `data_sufficiency` INT DEFAULT 1,
    `grade_ceiling` VARCHAR(10),
    `basic_score` INT DEFAULT 0,
    `quote_service_score` INT DEFAULT 0 COMMENT '报价服务质量得分',
    `supply_capability_score` INT DEFAULT 0 COMMENT '供应能力得分',
    `delivery_fulfillment_score` INT DEFAULT 0 COMMENT '交货履约得分(ERP)',
    `after_sales_score` INT DEFAULT 0 COMMENT '售后纠纷得分(ERP)',
    `feedback_score` INT DEFAULT 0,
    `relationship_score` INT DEFAULT 0,
    `consistency_score` INT DEFAULT 0 COMMENT '多方一致性得分',
    `score_details` JSON,
    `erp_buyer_count` INT DEFAULT 0,
    `feedback_count` INT DEFAULT 0,
    `rating_date` DATE,
    `effective_date` DATE,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_enterprise_period` (`enterprise_id`, `rating_period`),
    INDEX `idx_enterprise_date` (`enterprise_id`, `rating_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卖家信用评分主表';

-- 信用评级变更历史
CREATE TABLE IF NOT EXISTS `credit_score_history` (
    `id` BIGINT NOT NULL,
    `enterprise_id` BIGINT NOT NULL,
    `role_type` VARCHAR(10) NOT NULL COMMENT 'BUYER/SELLER',
    `previous_grade` VARCHAR(10),
    `previous_score` INT,
    `current_grade` VARCHAR(10),
    `current_score` INT,
    `change_reason` VARCHAR(500),
    `rating_period` VARCHAR(10),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_enterprise_role` (`enterprise_id`, `role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用评级变更历史';

-- 信用评分明细表
CREATE TABLE IF NOT EXISTS `credit_score_detail` (
    `id` BIGINT NOT NULL,
    `score_id` BIGINT NOT NULL,
    `enterprise_id` BIGINT NOT NULL,
    `role_type` VARCHAR(10) NOT NULL,
    `rating_period` VARCHAR(10),
    `dimension_code` VARCHAR(50),
    `dimension_name` VARCHAR(100),
    `indicator_code` VARCHAR(50),
    `indicator_name` VARCHAR(100),
    `raw_value` DECIMAL(18,4),
    `max_score` INT,
    `calculated_score` INT,
    `data_source` VARCHAR(20),
    `formula_version` VARCHAR(20),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_score_id` (`score_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用评分明细';

-- 询报价行为统计表
CREATE TABLE IF NOT EXISTS `inquiry_quote_stat` (
    `id` BIGINT NOT NULL,
    `enterprise_id` BIGINT NOT NULL,
    `stat_month` VARCHAR(10) NOT NULL COMMENT '统计月份(yyyy-MM)',
    `inquiry_sent_count` INT DEFAULT 0,
    `inquiry_received_count` INT DEFAULT 0,
    `quote_sent_count` INT DEFAULT 0,
    `quote_received_count` INT DEFAULT 0,
    `inquiry_responded_count` INT DEFAULT 0,
    `avg_response_hours` DECIMAL(10,2),
    `quote_price_deviation_pct` DECIMAL(10,4),
    `distinct_partner_count` INT DEFAULT 0,
    `inventory_update_count` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_enterprise_month` (`enterprise_id`, `stat_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询报价行为统计(按月)';

-- 企业关系图谱表
CREATE TABLE IF NOT EXISTS `enterprise_relation` (
    `id` BIGINT NOT NULL,
    `enterprise_a_id` BIGINT NOT NULL,
    `enterprise_b_id` BIGINT NOT NULL,
    `first_interaction_time` DATETIME,
    `last_interaction_time` DATETIME,
    `total_interaction_count` INT DEFAULT 0,
    `recent_90d_count` INT DEFAULT 0,
    `relation_months` INT DEFAULT 0,
    `active_status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_enterprise_a` (`enterprise_a_id`),
    INDEX `idx_enterprise_b` (`enterprise_b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业关系图谱';

-- ERP订单履约记录
CREATE TABLE IF NOT EXISTS `erp_order_record` (
    `id` BIGINT NOT NULL,
    `erp_order_no` VARCHAR(100) NOT NULL,
    `buyer_enterprise_id` BIGINT NOT NULL,
    `seller_enterprise_id` BIGINT NOT NULL,
    `order_amount` DECIMAL(18,2),
    `order_quantity` DECIMAL(18,4),
    `order_date` DATE,
    `order_status` VARCHAR(30),
    `cancelled` INT DEFAULT 0,
    `cancel_stage` VARCHAR(30),
    `agreed_pickup_date` DATE,
    `actual_pickup_date` DATE,
    `agreed_pickup_quantity` DECIMAL(18,4),
    `actual_pickup_quantity` DECIMAL(18,4),
    `pickup_overdue` INT DEFAULT 0,
    `shipment_date` DATE,
    `agreed_shipment_date` DATE,
    `shipment_overdue` INT DEFAULT 0,
    `quality_passed` INT DEFAULT 1,
    `returned` INT DEFAULT 0,
    `sync_batch_no` VARCHAR(50),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_buyer` (`buyer_enterprise_id`, `order_date`),
    INDEX `idx_seller` (`seller_enterprise_id`, `order_date`),
    INDEX `idx_erp_order_no` (`erp_order_no`),
    UNIQUE INDEX `uk_order_seller` (`erp_order_no`, `seller_enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP订单履约记录';

-- ERP付款记录
CREATE TABLE IF NOT EXISTS `erp_payment_record` (
    `id` BIGINT NOT NULL,
    `erp_order_no` VARCHAR(100) NOT NULL,
    `buyer_enterprise_id` BIGINT NOT NULL,
    `seller_enterprise_id` BIGINT NOT NULL,
    `payable_amount` DECIMAL(18,2),
    `paid_amount` DECIMAL(18,2),
    `agreed_payment_date` DATE,
    `actual_payment_date` DATE,
    `agreed_payment_days` INT,
    `overdue_days` INT DEFAULT 0,
    `severely_overdue` INT DEFAULT 0,
    `payment_status` VARCHAR(20),
    `has_outstanding` INT DEFAULT 0,
    `sync_batch_no` VARCHAR(50),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_buyer` (`buyer_enterprise_id`, `agreed_payment_date`),
    INDEX `idx_seller` (`seller_enterprise_id`),
    INDEX `idx_buyer_seller_date` (`buyer_enterprise_id`, `seller_enterprise_id`, `agreed_payment_date`),
    UNIQUE INDEX `uk_erp_order_seller` (`erp_order_no`, `seller_enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP付款记录';

-- ERP欠款汇总表
CREATE TABLE IF NOT EXISTS `erp_overdue_summary` (
    `id` BIGINT NOT NULL,
    `buyer_enterprise_id` BIGINT NOT NULL,
    `seller_enterprise_id` BIGINT NOT NULL,
    `snapshot_date` DATE NOT NULL,
    `total_payable` DECIMAL(18,2),
    `total_paid` DECIMAL(18,2),
    `outstanding_balance` DECIMAL(18,2),
    `overdue_amount` DECIMAL(18,2),
    `max_overdue_days` INT DEFAULT 0,
    `overdue_count` INT DEFAULT 0,
    `severe_overdue_count` INT DEFAULT 0,
    `overdue_level` VARCHAR(20),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_buyer_date` (`buyer_enterprise_id`, `snapshot_date`),
    INDEX `idx_buyer_seller_date` (`buyer_enterprise_id`, `seller_enterprise_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP欠款汇总(每日快照)';

-- 合作印象评价表
CREATE TABLE IF NOT EXISTS `cooperation_feedback` (
    `id` BIGINT NOT NULL,
    `evaluator_enterprise_id` BIGINT NOT NULL COMMENT '评价方',
    `target_enterprise_id` BIGINT NOT NULL COMMENT '被评方',
    `evaluator_role` VARCHAR(30) NOT NULL COMMENT 'BUYER_RATE_SELLER/SELLER_RATE_BUYER',
    `cooperation_level` INT COMMENT '合作次数: 1=1-2次 2=3-5次 3=5+ 4=长期',
    `overall_impression` INT COMMENT '整体印象: 4=非常好 3=不错 2=一般 1=较差',
    `payment_rating` INT COMMENT '付款(卖家评买家)',
    `pickup_rating` INT COMMENT '提货(卖家评买家)',
    `order_stability_rating` INT COMMENT '订单稳定性(卖家评买家)',
    `delivery_rating` INT COMMENT '交货(买家评卖家)',
    `quality_rating` INT COMMENT '质量(买家评卖家)',
    `pricing_rating` INT COMMENT '报价诚信(买家评卖家)',
    `communication_rating` INT COMMENT '沟通(通用)',
    `will_continue` INT COMMENT '继续合作: 1=是 2=视情况 3=不愿意',
    `comment` VARCHAR(500) COMMENT '一句话评价',
    `trigger_scene` VARCHAR(30) COMMENT '触发场景',
    `credibility_weight` DOUBLE COMMENT '可信度权重',
    `valid` INT DEFAULT 1 COMMENT '是否有效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_target_role` (`target_enterprise_id`, `evaluator_role`, `valid`),
    INDEX `idx_evaluator_target` (`evaluator_enterprise_id`, `target_enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合作印象评价';

-- 风险事件主表
CREATE TABLE IF NOT EXISTS `risk_event` (
    `id` BIGINT NOT NULL,
    `enterprise_id` BIGINT NOT NULL,
    `risk_category` VARCHAR(30) NOT NULL COMMENT 'JUDICIAL/OPERATIONAL/FINANCIAL/CHANGE',
    `risk_level` VARCHAR(20) NOT NULL COMMENT 'HIGH/MEDIUM/LOW/NONE',
    `event_title` VARCHAR(500),
    `event_detail` JSON,
    `involved_amount` DECIMAL(18,2),
    `event_date` DATE,
    `discovery_date` DATE,
    `data_source` VARCHAR(100),
    `event_status` VARCHAR(50),
    `is_related_party` INT DEFAULT 0 COMMENT '0=自身 1=关联方',
    `related_party_name` VARCHAR(200),
    `related_party_info_id` BIGINT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_enterprise` (`enterprise_id`),
    INDEX `idx_enterprise_level` (`enterprise_id`, `risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险事件(企查查)';

-- 关联方信息表
CREATE TABLE IF NOT EXISTS `related_party_info` (
    `id` BIGINT NOT NULL,
    `enterprise_id` BIGINT NOT NULL,
    `party_name` VARCHAR(200),
    `party_credit_code` VARCHAR(50),
    `relation_type` VARCHAR(30) COMMENT 'SHAREHOLDER/LEGAL_PERSON/CONTROLLER/GUARANTOR',
    `shareholding_ratio` DECIMAL(10,4),
    `relation_level` INT DEFAULT 1 COMMENT '1=直接 2=间接',
    `has_risk` INT DEFAULT 0,
    `data_source` VARCHAR(100),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_enterprise` (`enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关联方信息(企查查)';

-- 风险快照表
CREATE TABLE IF NOT EXISTS `risk_snapshot` (
    `id` BIGINT NOT NULL,
    `enterprise_id` BIGINT NOT NULL,
    `snapshot_date` DATE NOT NULL,
    `overall_risk_level` VARCHAR(20),
    `high_risk_count` INT DEFAULT 0,
    `medium_risk_count` INT DEFAULT 0,
    `low_risk_count` INT DEFAULT 0,
    `self_risk_count` INT DEFAULT 0,
    `related_party_risk_count` INT DEFAULT 0,
    `risk_summary` JSON,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_enterprise_date` (`enterprise_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险快照(每日)';

-- 风险预警推送记录
CREATE TABLE IF NOT EXISTS `risk_alert_log` (
    `id` BIGINT NOT NULL,
    `receiver_enterprise_id` BIGINT NOT NULL,
    `target_enterprise_id` BIGINT NOT NULL,
    `risk_event_id` BIGINT,
    `channel` VARCHAR(20),
    `alert_time` DATETIME,
    `read_status` INT DEFAULT 0,
    `read_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_receiver` (`receiver_enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险预警推送记录';

-- 买家欠款标记表
CREATE TABLE IF NOT EXISTS `buyer_overdue_tag` (
    `id` BIGINT NOT NULL,
    `buyer_enterprise_id` BIGINT NOT NULL,
    `overdue_level` VARCHAR(20) NOT NULL COMMENT 'NORMAL/MILD/MODERATE/SEVERE/MALICIOUS',
    `tag_reason` VARCHAR(500),
    `tag_time` DATETIME,
    `active_tag` INT DEFAULT 1,
    `overdue_count` INT DEFAULT 0,
    `max_overdue_days` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_buyer_active` (`buyer_enterprise_id`, `active_tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='买家欠款标记';

-- 信用评分配置表
CREATE TABLE IF NOT EXISTS `credit_scoring_config` (
    `id` BIGINT NOT NULL,
    `role_type` VARCHAR(10) NOT NULL,
    `rating_mode` VARCHAR(30) NOT NULL,
    `dimension_code` VARCHAR(50),
    `dimension_name` VARCHAR(100),
    `indicator_code` VARCHAR(50),
    `indicator_name` VARCHAR(100),
    `max_score` INT,
    `weight_percent` INT,
    `formula_version` VARCHAR(20),
    `effective_date` DATE,
    `expire_date` DATE,
    `enabled` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_role_mode` (`role_type`, `rating_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用评分配置';

-- ERP数据同步日志
CREATE TABLE IF NOT EXISTS `erp_sync_log` (
    `id` BIGINT NOT NULL,
    `sync_batch_no` VARCHAR(50) NOT NULL,
    `seller_enterprise_id` BIGINT,
    `sync_time` DATETIME,
    `data_type` VARCHAR(20),
    `record_count` INT DEFAULT 0,
    `sync_status` VARCHAR(20),
    `error_message` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_batch` (`sync_batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP同步日志';
