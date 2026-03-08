package com.steel.credit.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ERP订单数据同步请求
 */
@Data
public class ErpOrderSyncRequest {

    @NotNull
    private String erpOrderNo;
    @NotNull
    private Long buyerEnterpriseId;
    @NotNull
    private Long sellerEnterpriseId;
    private BigDecimal orderAmount;
    private BigDecimal orderQuantity;
    private LocalDate orderDate;
    private String orderStatus;
    private Boolean cancelled;
    private String cancelStage;
    private LocalDate agreedPickupDate;
    private LocalDate actualPickupDate;
    private BigDecimal agreedPickupQuantity;
    private BigDecimal actualPickupQuantity;
    private LocalDate agreedShipmentDate;
    private LocalDate shipmentDate;
    private Boolean qualityPassed;
    private Boolean returned;
}
