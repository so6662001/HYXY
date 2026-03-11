package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ERP订单履约记录 — 从ERP同步
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("erp_order_record")
public class ErpOrderRecord extends BaseEntity {

    /** ERP中的订单号 */
    private String erpOrderNo;

    /** 买家企业ID */
    private Long buyerEnterpriseId;

    /** 商家(卖家)企业ID */
    private Long sellerEnterpriseId;

    /** 订单金额 */
    private BigDecimal orderAmount;

    /** 订单数量 (吨) */
    private BigDecimal orderQuantity;

    /** 下单日期 */
    private LocalDate orderDate;

    /** 订单状态: COMPLETED/CANCELLED/PARTIAL/IN_PROGRESS */
    private String orderStatus;

    /** 是否取消: 0=否 1=是 */
    private Integer cancelled;

    /** 取消时点: BEFORE_STOCK=备货前, AFTER_STOCK=备货后 */
    private String cancelStage;

    /** 约定提货日期 */
    private LocalDate agreedPickupDate;

    /** 实际提货日期 */
    private LocalDate actualPickupDate;

    /** 约定提货量 */
    private BigDecimal agreedPickupQuantity;

    /** 实际提货量 */
    private BigDecimal actualPickupQuantity;

    /** 提货是否超期: 0=否 1=是 */
    private Integer pickupOverdue;

    /** 发货日期 (卖家发货) */
    private LocalDate shipmentDate;

    /** 约定发货日期 */
    private LocalDate agreedShipmentDate;

    /** 发货是否超期: 0=否 1=是 */
    private Integer shipmentOverdue;

    /** 质量合格: 0=不合格 1=合格 */
    private Integer qualityPassed;

    /** 是否退货: 0=否 1=是 */
    private Integer returned;

    /** ERP同步批次号 */
    private String syncBatchNo;
}
