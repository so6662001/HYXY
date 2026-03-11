package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 风险快照表 — 每日汇总企业风险概况
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("risk_snapshot")
public class RiskSnapshot extends BaseEntity {

    /** 企业ID */
    private Long enterpriseId;

    /** 快照日期 */
    private LocalDate snapshotDate;

    /** 综合风险等级: HIGH/MEDIUM/LOW/NONE */
    private String overallRiskLevel;

    /** 高危风险数量 */
    private Integer highRiskCount;

    /** 中危风险数量 */
    private Integer mediumRiskCount;

    /** 低危风险数量 */
    private Integer lowRiskCount;

    /** 企业自身风险数 */
    private Integer selfRiskCount;

    /** 关联方风险数 */
    private Integer relatedPartyRiskCount;

    /** 风险汇总 (JSON: 各类别数量、关键风险摘要) */
    private String riskSummary;
}
