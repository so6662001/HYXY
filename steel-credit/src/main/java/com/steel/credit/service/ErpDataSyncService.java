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
}
