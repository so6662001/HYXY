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
    @NotNull(message = "应付金额不能为空")
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    @NotNull(message = "约定付款日期不能为空")
    private LocalDate agreedPaymentDate;
    private LocalDate actualPaymentDate;
    private Integer agreedPaymentDays;
    @NotNull(message = "付款状态不能为空")
    private String paymentStatus;
}
