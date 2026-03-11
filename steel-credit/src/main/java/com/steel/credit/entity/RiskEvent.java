package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 风险事件主表 — 来源企查查
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("risk_event")
public class RiskEvent extends BaseEntity {

    /** 企业ID */
    private Long enterpriseId;

    /** 风险类别: JUDICIAL/OPERATIONAL/FINANCIAL/CHANGE */
    private String riskCategory;

    /** 风险等级: HIGH/MEDIUM/LOW/NONE */
    private String riskLevel;

    /** 事件标题 */
    private String eventTitle;

    /** 事件详情 (JSON) */
    private String eventDetail;

    /** 涉案/涉事金额 */
    private BigDecimal involvedAmount;

    /** 事件日期 */
    private LocalDate eventDate;

    /** 发现日期 */
    private LocalDate discoveryDate;

    /** 数据来源 */
    private String dataSource;

    /** 案件/事件状态 */
    private String eventStatus;

    /** 是否为关联方风险: 0=企业自身 1=关联方 */
    private Integer isRelatedParty;

    /** 关联方名称 (仅关联方风险时有值) */
    private String relatedPartyName;

    /** 关联方ID */
    private Long relatedPartyInfoId;
}
