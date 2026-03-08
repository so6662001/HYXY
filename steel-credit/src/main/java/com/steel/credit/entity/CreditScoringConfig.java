package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 信用评分配置表 — 权重、公式可配置可版本化
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_scoring_config")
public class CreditScoringConfig extends BaseEntity {

    /** 角色类型: BUYER/SELLER */
    private String roleType;

    /** 评分模式: FULL/PLATFORM_FEEDBACK/BASIC */
    private String ratingMode;

    /** 维度编码 */
    private String dimensionCode;

    /** 维度名称 */
    private String dimensionName;

    /** 子指标编码 */
    private String indicatorCode;

    /** 子指标名称 */
    private String indicatorName;

    /** 满分 */
    private Integer maxScore;

    /** 权重 (百分比整数, 如 28 表示 28%) */
    private Integer weightPercent;

    /** 计算公式版本 */
    private String formulaVersion;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 失效日期 (null=永久有效) */
    private LocalDate expireDate;

    /** 是否启用: 1=是 0=否 */
    private Integer enabled;
}
