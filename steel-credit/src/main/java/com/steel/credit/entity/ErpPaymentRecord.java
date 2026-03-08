package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ERP付款记录 — 从ERP同步
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("erp_payment_record")
public class ErpPaymentRecord extends BaseEntity {

    /** ERP中的订单号 */
    private String erpOrderNo;

    /** 买家企业ID */
    private Long buyerEnterpriseId;

    /** 商家(卖家)企业ID */
    private Long sellerEnterpriseId;

    /** 应付金额 */
    private BigDecimal payableAmount;

    /** 实付金额 */
    private BigDecimal paidAmount;

    /** 约定付款日期 */
    private LocalDate agreedPaymentDate;

    /** 实际付款日期 (为null表示未付款) */
    private LocalDate actualPaymentDate;

    /** 约定账期天数 */
    private Integer agreedPaymentDays;

    /** 超期天数 (负数表示提前付款) */
    private Integer overdueDays;

    /** 是否严重超期(>60天): 0=否 1=是 */
    private Integer severelyOverdue;

    /** 付款状态: PAID/UNPAID/PARTIAL */
    private String paymentStatus;

    /** 当前是否仍有欠款: 0=否 1=是 */
    private Integer hasOutstanding;

    /** ERP同步批次号 */
    private String syncBatchNo;
}
