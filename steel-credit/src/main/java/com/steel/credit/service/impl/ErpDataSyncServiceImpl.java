package com.steel.credit.service.impl;

import com.steel.credit.constant.OverdueLevel;
import com.steel.credit.dto.request.ErpOrderSyncRequest;
import com.steel.credit.dto.request.ErpPaymentSyncRequest;
import com.steel.credit.entity.*;
import com.steel.credit.mapper.*;
import com.steel.credit.service.ErpDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErpDataSyncServiceImpl implements ErpDataSyncService {

    private final ErpOrderRecordMapper erpOrderRecordMapper;
    private final ErpPaymentRecordMapper erpPaymentRecordMapper;
    private final ErpOverdueSummaryMapper erpOverdueSummaryMapper;
    private final ErpSyncLogMapper erpSyncLogMapper;
    private final BuyerOverdueTagMapper buyerOverdueTagMapper;

    @Override
    @Transactional
    public void syncOrderRecords(Long sellerEnterpriseId, List<ErpOrderSyncRequest> requests) {
        String batchNo = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("同步ERP订单数据: seller={}, count={}, batch={}", sellerEnterpriseId, requests.size(), batchNo);

        int successCount = 0;
        for (ErpOrderSyncRequest req : requests) {
            try {
                ErpOrderRecord record = new ErpOrderRecord();
                record.setErpOrderNo(req.getErpOrderNo());
                record.setBuyerEnterpriseId(req.getBuyerEnterpriseId());
                record.setSellerEnterpriseId(sellerEnterpriseId);
                record.setOrderAmount(req.getOrderAmount());
                record.setOrderQuantity(req.getOrderQuantity());
                record.setOrderDate(req.getOrderDate());
                record.setOrderStatus(req.getOrderStatus());
                record.setCancelled(Boolean.TRUE.equals(req.getCancelled()) ? 1 : 0);
                record.setCancelStage(req.getCancelStage());
                record.setAgreedPickupDate(req.getAgreedPickupDate());
                record.setActualPickupDate(req.getActualPickupDate());
                record.setAgreedPickupQuantity(req.getAgreedPickupQuantity());
                record.setActualPickupQuantity(req.getActualPickupQuantity());
                record.setAgreedShipmentDate(req.getAgreedShipmentDate());
                record.setShipmentDate(req.getShipmentDate());
                record.setQualityPassed(req.getQualityPassed() == null || Boolean.TRUE.equals(req.getQualityPassed()) ? 1 : 0);
                record.setReturned(Boolean.TRUE.equals(req.getReturned()) ? 1 : 0);
                record.setSyncBatchNo(batchNo);

                if (record.getAgreedPickupDate() != null && record.getActualPickupDate() != null) {
                    record.setPickupOverdue(record.getActualPickupDate().isAfter(record.getAgreedPickupDate()) ? 1 : 0);
                }
                if (record.getAgreedShipmentDate() != null && record.getShipmentDate() != null) {
                    record.setShipmentOverdue(record.getShipmentDate().isAfter(record.getAgreedShipmentDate()) ? 1 : 0);
                }

                erpOrderRecordMapper.insert(record);
                successCount++;
            } catch (Exception e) {
                log.error("同步订单失败: erpOrderNo={}", req.getErpOrderNo(), e);
            }
        }

        saveSyncLog(batchNo, sellerEnterpriseId, "ORDER", requests.size(),
                successCount == requests.size() ? "SUCCESS" : "PARTIAL", null);
    }

    @Override
    @Transactional
    public void syncPaymentRecords(Long sellerEnterpriseId, List<ErpPaymentSyncRequest> requests) {
        String batchNo = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("同步ERP付款数据: seller={}, count={}, batch={}", sellerEnterpriseId, requests.size(), batchNo);

        int successCount = 0;
        for (ErpPaymentSyncRequest req : requests) {
            try {
                ErpPaymentRecord record = new ErpPaymentRecord();
                record.setErpOrderNo(req.getErpOrderNo());
                record.setBuyerEnterpriseId(req.getBuyerEnterpriseId());
                record.setSellerEnterpriseId(sellerEnterpriseId);
                record.setPayableAmount(req.getPayableAmount());
                record.setPaidAmount(req.getPaidAmount());
                record.setAgreedPaymentDate(req.getAgreedPaymentDate());
                record.setActualPaymentDate(req.getActualPaymentDate());
                record.setAgreedPaymentDays(req.getAgreedPaymentDays());
                record.setPaymentStatus(req.getPaymentStatus());
                record.setSyncBatchNo(batchNo);

                if (req.getAgreedPaymentDate() != null) {
                    LocalDate effectivePayDate = req.getActualPaymentDate() != null
                            ? req.getActualPaymentDate() : LocalDate.now();
                    int overdueDays = (int) ChronoUnit.DAYS.between(req.getAgreedPaymentDate(), effectivePayDate);
                    record.setOverdueDays(Math.max(overdueDays, 0));
                    record.setSeverelyOverdue(overdueDays > 60 ? 1 : 0);
                }

                boolean unpaid = !"PAID".equals(req.getPaymentStatus());
                boolean partialPaid = req.getPayableAmount() != null && req.getPaidAmount() != null
                        && req.getPaidAmount().compareTo(req.getPayableAmount()) < 0;
                record.setHasOutstanding(unpaid || partialPaid ? 1 : 0);

                erpPaymentRecordMapper.insert(record);
                successCount++;
            } catch (Exception e) {
                log.error("同步付款记录失败: erpOrderNo={}", req.getErpOrderNo(), e);
            }
        }

        saveSyncLog(batchNo, sellerEnterpriseId, "PAYMENT", requests.size(),
                successCount == requests.size() ? "SUCCESS" : "PARTIAL", null);

        requests.stream()
                .map(ErpPaymentSyncRequest::getBuyerEnterpriseId)
                .distinct()
                .forEach(this::updateOverdueTags);
    }

    @Override
    public void updateOverdueTags(Long buyerEnterpriseId) {
        List<ErpOverdueSummary> summaries = erpOverdueSummaryMapper.selectLatestByBuyer(buyerEnterpriseId);
        if (summaries.isEmpty()) return;

        int totalOverdueCount = summaries.stream()
                .mapToInt(s -> s.getOverdueCount() != null ? s.getOverdueCount() : 0).sum();
        int totalSevereCount = summaries.stream()
                .mapToInt(s -> s.getSevereOverdueCount() != null ? s.getSevereOverdueCount() : 0).sum();
        int maxDays = summaries.stream()
                .mapToInt(s -> s.getMaxOverdueDays() != null ? s.getMaxOverdueDays() : 0).max().orElse(0);

        BigDecimal totalPayable = summaries.stream()
                .map(s -> s.getTotalPayable() != null ? s.getTotalPayable() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOverdueAmount = summaries.stream()
                .map(s -> s.getOverdueAmount() != null ? s.getOverdueAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OverdueLevel level;
        String reason;

        if (maxDays > 120 || (summaries.size() >= 2 && totalSevereCount >= 2)) {
            level = OverdueLevel.MALICIOUS;
            reason = "超期超过120天或多家商家存在严重超期";
        } else if (maxDays > 60 || (totalPayable.compareTo(BigDecimal.ZERO) > 0
                && totalOverdueAmount.doubleValue() / totalPayable.doubleValue() > 0.30)) {
            level = OverdueLevel.SEVERE;
            reason = "超期超过60天或超期金额占比超过30%";
        } else if (totalOverdueCount >= 3 || maxDays > 15) {
            level = OverdueLevel.MODERATE;
            reason = "多次超期(≥3次)或超期超过15天";
        } else if (totalOverdueCount > 0) {
            level = OverdueLevel.MILD;
            reason = "偶有超期，超期≤15天";
        } else {
            level = OverdueLevel.NORMAL;
            reason = "无超期记录";
        }

        BuyerOverdueTag existing = buyerOverdueTagMapper.selectActiveByBuyer(buyerEnterpriseId);
        if (existing != null) {
            existing.setOverdueLevel(level.name());
            existing.setTagReason(reason);
            existing.setTagTime(LocalDateTime.now());
            existing.setOverdueCount(totalOverdueCount);
            existing.setMaxOverdueDays(maxDays);
            buyerOverdueTagMapper.updateById(existing);
        } else if (level != OverdueLevel.NORMAL) {
            BuyerOverdueTag tag = new BuyerOverdueTag();
            tag.setBuyerEnterpriseId(buyerEnterpriseId);
            tag.setOverdueLevel(level.name());
            tag.setTagReason(reason);
            tag.setTagTime(LocalDateTime.now());
            tag.setActiveTag(1);
            tag.setOverdueCount(totalOverdueCount);
            tag.setMaxOverdueDays(maxDays);
            buyerOverdueTagMapper.insert(tag);
        }
    }

    @Override
    public void aggregateOverdueSummary(Long buyerEnterpriseId) {
        String startDate = LocalDate.now().minusMonths(12).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<Long> sellerIds = erpPaymentRecordMapper.selectDistinctSellerIdsByBuyer(buyerEnterpriseId);

        for (Long sellerId : sellerIds) {
            List<ErpPaymentRecord> payments = erpPaymentRecordMapper.selectByBuyerSellerAndDate(
                    buyerEnterpriseId, sellerId, startDate);
            if (payments.isEmpty()) continue;

            BigDecimal totalPayable = payments.stream()
                    .map(p -> p.getPayableAmount() != null ? p.getPayableAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPaid = payments.stream()
                    .map(p -> p.getPaidAmount() != null ? p.getPaidAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstanding = totalPayable.subtract(totalPaid).max(BigDecimal.ZERO);

            BigDecimal overdueAmount = payments.stream()
                    .filter(p -> p.getOverdueDays() != null && p.getOverdueDays() > 0)
                    .filter(p -> Integer.valueOf(1).equals(p.getHasOutstanding()))
                    .map(p -> {
                        BigDecimal payable = p.getPayableAmount() != null ? p.getPayableAmount() : BigDecimal.ZERO;
                        BigDecimal paid = p.getPaidAmount() != null ? p.getPaidAmount() : BigDecimal.ZERO;
                        return payable.subtract(paid).max(BigDecimal.ZERO);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int overdueCount = (int) payments.stream()
                    .filter(p -> p.getOverdueDays() != null && p.getOverdueDays() > 0).count();
            int severeCount = (int) payments.stream()
                    .filter(p -> Integer.valueOf(1).equals(p.getSeverelyOverdue())).count();
            int maxDays = payments.stream()
                    .mapToInt(p -> p.getOverdueDays() != null ? p.getOverdueDays() : 0).max().orElse(0);

            ErpOverdueSummary summary = new ErpOverdueSummary();
            summary.setBuyerEnterpriseId(buyerEnterpriseId);
            summary.setSellerEnterpriseId(sellerId);
            summary.setSnapshotDate(LocalDate.now());
            summary.setTotalPayable(totalPayable);
            summary.setTotalPaid(totalPaid);
            summary.setOutstandingBalance(outstanding);
            summary.setOverdueAmount(overdueAmount);
            summary.setMaxOverdueDays(maxDays);
            summary.setOverdueCount(overdueCount);
            summary.setSevereOverdueCount(severeCount);

            String level;
            if (maxDays > 120) level = "MALICIOUS";
            else if (maxDays > 60) level = "SEVERE";
            else if (overdueCount >= 3 || maxDays > 15) level = "MODERATE";
            else if (overdueCount > 0) level = "MILD";
            else level = "NORMAL";
            summary.setOverdueLevel(level);

            erpOverdueSummaryMapper.insert(summary);
        }

        updateOverdueTags(buyerEnterpriseId);
    }

    @Override
    public void batchAggregateOverdueSummaries() {
        List<Long> buyerIds = erpPaymentRecordMapper.selectDistinctBuyerIds();
        log.info("批量聚合欠款汇总, 买家数={}", buyerIds.size());
        for (Long buyerId : buyerIds) {
            try {
                aggregateOverdueSummary(buyerId);
            } catch (Exception e) {
                log.error("聚合买家 {} 欠款汇总失败", buyerId, e);
            }
        }
    }

    private void saveSyncLog(String batchNo, Long sellerId, String dataType,
                             int count, String status, String error) {
        ErpSyncLog syncLog = new ErpSyncLog();
        syncLog.setSyncBatchNo(batchNo);
        syncLog.setSellerEnterpriseId(sellerId);
        syncLog.setSyncTime(LocalDateTime.now());
        syncLog.setDataType(dataType);
        syncLog.setRecordCount(count);
        syncLog.setSyncStatus(status);
        syncLog.setErrorMessage(error);
        erpSyncLogMapper.insert(syncLog);
    }
}
