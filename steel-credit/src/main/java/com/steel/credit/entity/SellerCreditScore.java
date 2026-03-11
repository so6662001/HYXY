package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 卖家信用评分主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seller_credit_score")
public class SellerCreditScore extends BaseEntity {

    /** 企业ID */
    private Long enterpriseId;

    /** 评级周期 (yyyy-MM) */
    private String ratingPeriod;

    /** 评级模式: FULL/PLATFORM_FEEDBACK/BASIC */
    private String ratingMode;

    /** 总得分 (0-1000) */
    private Integer totalScore;

    /** 信用等级 */
    private String creditGrade;

    /** 数据充分度等级 (1-5) */
    private Integer dataSufficiency;

    /** 等级天花板 */
    private String gradeCeiling;

    // ====== 各维度得分 ======

    /** 企业基础实力得分 */
    private Integer basicScore;

    /** 报价服务质量得分 */
    private Integer quoteServiceScore;

    /** 供应能力得分 */
    private Integer supplyCapabilityScore;

    /** 交货履约得分 (ERP) */
    private Integer deliveryFulfillmentScore;

    /** 售后与纠纷得分 (ERP) */
    private Integer afterSalesScore;

    /** 合作反馈得分 */
    private Integer feedbackScore;

    /** 关系持续性得分 */
    private Integer relationshipScore;

    /** 多方一致性得分 */
    private Integer consistencyScore;

    /** 各维度明细数据 (JSON) */
    private String scoreDetails;

    /** ERP数据来源买家数 */
    private Integer erpBuyerCount;

    /** 合作反馈条数 */
    private Integer feedbackCount;

    /** 评定日期 */
    private LocalDate ratingDate;

    /** 生效日期 */
    private LocalDate effectiveDate;
}
