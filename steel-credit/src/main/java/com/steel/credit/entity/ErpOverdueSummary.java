package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ERP欠款汇总表 — 每日快照
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("erp_overdue_summary")
public class ErpOverdueSummary extends BaseEntity {

    /** 买家企业ID */
    private Long buyerEnterpriseId;

    /** 商家(卖家)企业ID */
    private Long sellerEnterpriseId;

    /** 快照日期 */
    private LocalDate snapshotDate;

    /** 近12月累计应付总额 */
    private BigDecimal totalPayable;

    /** 近12月累计已付总额 */
    private BigDecimal totalPaid;

    /** 当前欠款余额 */
    private BigDecimal outstandingBalance;

    /** 当前超期欠款金额 */
    private BigDecimal overdueAmount;

    /** 最大超期天数 */
    private Integer maxOverdueDays;

    /** 超期次数 (近12月) */
    private Integer overdueCount;

    /** 严重超期次数 (>60天) */
    private Integer severeOverdueCount;

    /** 欠款等级: NORMAL/MILD/MODERATE/SEVERE/MALICIOUS */
    private String overdueLevel;
}
