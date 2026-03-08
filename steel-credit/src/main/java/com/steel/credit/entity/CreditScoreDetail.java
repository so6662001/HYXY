package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 信用评分明细表 — 每一子项的计算过程可追溯
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_score_detail")
public class CreditScoreDetail extends BaseEntity {

    /** 关联评分主表ID (买家或卖家) */
    private Long scoreId;

    /** 企业ID */
    private Long enterpriseId;

    /** 角色类型: BUYER/SELLER */
    private String roleType;

    /** 评级周期 */
    private String ratingPeriod;

    /** 维度编码 */
    private String dimensionCode;

    /** 维度名称 */
    private String dimensionName;

    /** 子指标编码 */
    private String indicatorCode;

    /** 子指标名称 */
    private String indicatorName;

    /** 原始数据值 */
    private BigDecimal rawValue;

    /** 满分 */
    private Integer maxScore;

    /** 计算得分 */
    private Integer calculatedScore;

    /** 数据来源: PLATFORM/ERP/FEEDBACK */
    private String dataSource;

    /** 计算公式版本 */
    private String formulaVersion;
}
