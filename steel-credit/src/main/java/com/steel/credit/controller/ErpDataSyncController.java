package com.steel.credit.controller;

import com.steel.credit.dto.request.ErpOrderSyncRequest;
import com.steel.credit.dto.request.ErpPaymentSyncRequest;
import com.steel.credit.dto.response.Result;
import com.steel.credit.service.ErpDataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ERP数据同步", description = "接收ERP推送的订单、付款、欠款数据")
@RestController
@RequestMapping("/erp/sync")
@RequiredArgsConstructor
public class ErpDataSyncController {

    private final ErpDataSyncService erpDataSyncService;

    @Operation(summary = "同步ERP订单数据（批量）")
    @PostMapping("/orders/{sellerEnterpriseId}")
    public Result<Void> syncOrders(
            @Parameter(description = "商家企业ID") @PathVariable Long sellerEnterpriseId,
            @Valid @RequestBody List<ErpOrderSyncRequest> requests) {
        erpDataSyncService.syncOrderRecords(sellerEnterpriseId, requests);
        return Result.ok();
    }

    @Operation(summary = "同步ERP付款数据（批量）")
    @PostMapping("/payments/{sellerEnterpriseId}")
    public Result<Void> syncPayments(
            @Parameter(description = "商家企业ID") @PathVariable Long sellerEnterpriseId,
            @Valid @RequestBody List<ErpPaymentSyncRequest> requests) {
        erpDataSyncService.syncPaymentRecords(sellerEnterpriseId, requests);
        return Result.ok();
    }
}
