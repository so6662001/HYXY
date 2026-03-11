package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 买家信用评分主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("buyer_credit_score")
public class BuyerCreditScore extends BaseEntity {

    /** 企业ID */
    private Long enterpriseId;

    /** 评级周期 (yyyy-MM) */
    private String ratingPeriod;

    /** 评级模式: FULL/PLATFORM_FEEDBACK/BASIC */
    private String ratingMode;

    /** 总得分 (0-1000) */
    private Integer totalScore;

    /** 信用等级: AAA/AA/A/BBB/BB/B/C/D */
    private String creditGrade;

    /** 数据充分度等级 (1-5) */
    private Integer dataSufficiency;

    /** 等级天花板 */
    private String gradeCeiling;

    // ====== 各维度得分 ======

    /** 企业基础实力得分 */
    private Integer basicScore;

    /** 采购活跃度得分 */
    private Integer activityScore;

    /** 采购行为质量得分 */
    private Integer behaviorQualityScore;

    /** 合作关系持续性得分 */
    private Integer relationshipScore;

    /** 订单履约得分 (ERP) */
    private Integer orderFulfillmentScore;

    /** 提货履约得分 (ERP) */
    private Integer pickupFulfillmentScore;

    /** 付款信用得分 (ERP) */
    private Integer paymentCreditScore;

    /** 合作反馈得分 */
    private Integer feedbackScore;

    /** 多商家ERP一致性加分 */
    private Integer consistencyBonus;

    /** 各维度明细数据 (JSON) */
    private String scoreDetails;

    /** ERP数据来源商家数 */
    private Integer erpMerchantCount;

    /** 合作反馈条数 */
    private Integer feedbackCount;

    /** 评定日期 */
    private LocalDate ratingDate;

    /** 生效日期 */
    private LocalDate effectiveDate;
}
