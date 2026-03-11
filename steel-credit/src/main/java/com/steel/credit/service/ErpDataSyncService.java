package com.steel.credit.service;

import com.steel.credit.dto.request.ErpOrderSyncRequest;
import com.steel.credit.dto.request.ErpPaymentSyncRequest;

import java.util.List;

public interface ErpDataSyncService {

    /**
     * 同步ERP订单数据（批量）
     */
    void syncOrderRecords(Long sellerEnterpriseId, List<ErpOrderSyncRequest> requests);

    /**
     * 同步ERP付款数据（批量）
     */
    void syncPaymentRecords(Long sellerEnterpriseId, List<ErpPaymentSyncRequest> requests);

    /**
     * 更新买家欠款标记
     */
    void updateOverdueTags(Long buyerEnterpriseId);

    /**
     * 聚合生成erp_overdue_summary快照（从erp_payment_record汇总）
     */
    void aggregateOverdueSummary(Long buyerEnterpriseId);

    /**
     * 批量聚合所有买家的欠款汇总快照
     */
    void batchAggregateOverdueSummaries();
}
