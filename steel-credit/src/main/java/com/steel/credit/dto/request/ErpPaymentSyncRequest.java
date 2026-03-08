package com.steel.credit.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ERP付款数据同步请求
 */
@Data
public class ErpPaymentSyncRequest {

    @NotNull
    private String erpOrderNo;
    @NotNull
    private Long buyerEnterpriseId;
    @NotNull
    private Long sellerEnterpriseId;
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    private LocalDate agreedPaymentDate;
    private LocalDate actualPaymentDate;
    private Integer agreedPaymentDays;
    private String paymentStatus;
}
