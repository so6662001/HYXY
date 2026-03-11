package com.steel.credit.service.engine;

import com.steel.credit.constant.*;
import com.steel.credit.entity.*;
import com.steel.credit.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 买家信用评分引擎
 * <p>
 * 三种评级模式:
 * - FULL: 平台行为(30%) + ERP履约(50%) + 合作反馈(20%)
 * - PLATFORM_FEEDBACK: 平台行为(55%) + 合作反馈(45%)
 * - BASIC: 平台行为(100%)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BuyerCreditEngine {

    private final InquiryQuoteStatMapper inquiryQuoteStatMapper;
    private final EnterpriseRelationMapper enterpriseRelationMapper;
    private final ErpOrderRecordMapper erpOrderRecordMapper;
    private final ErpPaymentRecordMapper erpPaymentRecordMapper;
    private final ErpOverdueSummaryMapper erpOverdueSummaryMapper;
    private final CooperationFeedbackMapper cooperationFeedbackMapper;
    private final EnterpriseInfoMapper enterpriseInfoMapper;

    private static final int TOTAL_SCORE = 1000;
    private static final int MIN_ERP_RECORDS = 3;
    private static final int MIN_FEEDBACK_COUNT = 5;
    private static final int SEVERE_OVERDUE_DAYS = 60;

    /**
     * 计算买家信用评分
     */
    public BuyerCreditScore calculate(Long enterpriseId) {
        log.info("开始计算买家信用评分, enterpriseId={}", enterpriseId);

        EnterpriseInfo enterprise = enterpriseInfoMapper.selectById(enterpriseId);
        if (enterprise == null) {
            log.warn("企业不存在: {}", enterpriseId);
            return null;
        }

        RatingMode mode = determineRatingMode(enterpriseId);
        log.info("企业 {} 评级模式: {}", enterpriseId, mode);

        BuyerCreditScore score = new BuyerCreditScore();
        score.setEnterpriseId(enterpriseId);
        score.setRatingPeriod(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        score.setRatingMode(mode.getCode());
        score.setRatingDate(LocalDate.now());
        score.setEffectiveDate(LocalDate.now().plusDays(1));

        int basicScore = calcBasicScore(enterprise);
        int activityScore = calcActivityScore(enterpriseId);
        int behaviorQualityScore = calcBehaviorQualityScore(enterpriseId);
        int relationshipScore = calcRelationshipScore(enterpriseId);

        score.setBasicScore(basicScore);
        score.setActivityScore(activityScore);
        score.setBehaviorQualityScore(behaviorQualityScore);
        score.setRelationshipScore(relationshipScore);

        int platformRawTotal = basicScore + activityScore + behaviorQualityScore + relationshipScore;

        int totalScoreValue;

        switch (mode) {
            case FULL -> {
                int orderFulfillment = calcOrderFulfillmentScore(enterpriseId);
                int pickupFulfillment = calcPickupFulfillmentScore(enterpriseId);
                int paymentCredit = calcPaymentCreditScore(enterpriseId);
                int feedback = calcFeedbackScore(enterpriseId);
                int consistency = calcConsistencyBonus(enterpriseId);

                score.setOrderFulfillmentScore(orderFulfillment);
                score.setPickupFulfillmentScore(pickupFulfillment);
                score.setPaymentCreditScore(paymentCredit);
                score.setFeedbackScore(feedback);
                score.setConsistencyBonus(consistency);

                int erpRawTotal = orderFulfillment + pickupFulfillment + paymentCredit;

                totalScoreValue = (int) (platformRawTotal / 400.0 * 300
                        + erpRawTotal / 500.0 * 500
                        + feedback / 200.0 * 120
                        + consistency / 150.0 * 80);

                score.setErpMerchantCount(erpOrderRecordMapper.countDistinctSellersByBuyer(enterpriseId));
                score.setFeedbackCount(cooperationFeedbackMapper.countValidByTargetAndRole(enterpriseId, "SELLER_RATE_BUYER"));
                score.setDataSufficiency(DataSufficiency.FULL.getLevel());
                score.setGradeCeiling(CreditGrade.AAA.getCode());
            }
            case PLATFORM_FEEDBACK -> {
                int feedback = calcFeedbackScore(enterpriseId);
                score.setFeedbackScore(feedback);
                score.setOrderFulfillmentScore(0);
                score.setPickupFulfillmentScore(0);
                score.setPaymentCreditScore(0);
                score.setConsistencyBonus(0);

                totalScoreValue = (int) (platformRawTotal / 400.0 * 550
                        + feedback / 200.0 * 450);

                score.setErpMerchantCount(0);
                score.setFeedbackCount(cooperationFeedbackMapper.countValidByTargetAndRole(enterpriseId, "SELLER_RATE_BUYER"));
                score.setDataSufficiency(DataSufficiency.MEDIUM.getLevel());
                score.setGradeCeiling(CreditGrade.AA.getCode());
            }
            default -> {
                score.setOrderFulfillmentScore(0);
                score.setPickupFulfillmentScore(0);
                score.setPaymentCreditScore(0);
                score.setFeedbackScore(0);
                score.setConsistencyBonus(0);

                totalScoreValue = (int) (platformRawTotal / 400.0 * TOTAL_SCORE);

                score.setErpMerchantCount(0);
                score.setFeedbackCount(0);
                score.setDataSufficiency(DataSufficiency.MINIMAL.getLevel());
                score.setGradeCeiling(CreditGrade.A.getCode());
            }
        }

        totalScoreValue = Math.min(totalScoreValue, TOTAL_SCORE);
        score.setTotalScore(totalScoreValue);

        CreditGrade grade = CreditGrade.fromScore(totalScoreValue);
        CreditGrade ceiling = CreditGrade.valueOf(score.getGradeCeiling());
        if (grade.exceeds(ceiling)) {
            grade = ceiling;
        }
        score.setCreditGrade(grade.getCode());

        log.info("买家信用评分完成: enterpriseId={}, score={}, grade={}", enterpriseId, totalScoreValue, grade.getCode());
        return score;
    }

    /**
     * 判定评级模式
     */
    private RatingMode determineRatingMode(Long enterpriseId) {
        int erpRecords = erpOrderRecordMapper.countByBuyer(enterpriseId);
        int feedbackCount = cooperationFeedbackMapper.countValidByTargetAndRole(enterpriseId, "SELLER_RATE_BUYER");

        if (erpRecords >= MIN_ERP_RECORDS) {
            return RatingMode.FULL;
        } else if (feedbackCount >= MIN_FEEDBACK_COUNT) {
            return RatingMode.PLATFORM_FEEDBACK;
        } else {
            return RatingMode.BASIC;
        }
    }

    // ================ 平台行为层评分 ================

    /**
     * A. 企业基础实力 (满分100)
     */
    private int calcBasicScore(EnterpriseInfo enterprise) {
        int score = 0;

        int completeness = enterprise.getProfileCompleteness() != null ? enterprise.getProfileCompleteness() : 0;
        score += (int) (completeness / 100.0 * 25);

        if (Integer.valueOf(1).equals(enterprise.getLicenseVerified())) score += 15;
        if (Integer.valueOf(1).equals(enterprise.getQualificationVerified())) score += 15;

        if (enterprise.getJoinDate() != null) {
            long months = ChronoUnit.MONTHS.between(enterprise.getJoinDate(), LocalDate.now());
            if (months > 24) score += 20;
            else if (months > 12) score += 16;
            else if (months > 6) score += 12;
            else if (months > 3) score += 8;
            else score += 5;
        }

        // 库存信息质量按固定给15分（因买家不一定有库存）
        score += 15;

        return Math.min(score, 100);
    }

    /**
     * B. 采购活跃度 (满分120)
     */
    private int calcActivityScore(Long enterpriseId) {
        String startMonth = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<InquiryQuoteStat> stats = inquiryQuoteStatMapper.selectByEnterpriseAndMonthRange(enterpriseId, startMonth);

        if (stats.isEmpty()) return 0;

        double avgInquiry = stats.stream()
                .mapToInt(s -> s.getInquirySentCount() != null ? s.getInquirySentCount() : 0)
                .average().orElse(0);
        int inquiryScore = scoreByPercentile(avgInquiry, 30);

        int distinctPartners = stats.stream()
                .mapToInt(s -> s.getDistinctPartnerCount() != null ? s.getDistinctPartnerCount() : 0)
                .max().orElse(0);
        int partnerScore = Math.min(distinctPartners * 3, 30);

        int activeMonths = stats.size();
        int continuityScore = (int) (activeMonths / 12.0 * 30);

        int totalActivity = stats.stream()
                .mapToInt(s -> (s.getInquirySentCount() != null ? s.getInquirySentCount() : 0)
                        + (s.getQuoteReceivedCount() != null ? s.getQuoteReceivedCount() : 0))
                .sum();
        int volumeScore = Math.min(totalActivity / 10, 30);

        return Math.min(inquiryScore + partnerScore + continuityScore + volumeScore, 120);
    }

    /**
     * C. 采购行为质量 (满分100)
     */
    private int calcBehaviorQualityScore(Long enterpriseId) {
        String startMonth = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<InquiryQuoteStat> stats = inquiryQuoteStatMapper.selectByEnterpriseAndMonthRange(enterpriseId, startMonth);

        if (stats.isEmpty()) return 0;

        int totalReceived = stats.stream()
                .mapToInt(s -> s.getQuoteReceivedCount() != null ? s.getQuoteReceivedCount() : 0)
                .sum();
        int quoteViewScore = Math.min(totalReceived > 0 ? 40 : 0, 40);

        double avgDeviation = stats.stream()
                .map(InquiryQuoteStat::getQuotePriceDeviationPct)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(50);
        int deviationScore = avgDeviation < 5 ? 30 : avgDeviation < 10 ? 22 : avgDeviation < 20 ? 15 : 5;

        int monthsWithActivity = (int) stats.stream()
                .filter(s -> (s.getInquirySentCount() != null && s.getInquirySentCount() > 0))
                .count();
        int stabilityScore = Math.min((int) (monthsWithActivity / 12.0 * 30), 30);

        return Math.min(quoteViewScore + deviationScore + stabilityScore, 100);
    }

    /**
     * D. 合作关系持续性 (满分80)
     */
    private int calcRelationshipScore(Long enterpriseId) {
        int totalPartners = enterpriseRelationMapper.countTotalPartners(enterpriseId);
        if (totalPartners == 0) return 0;

        int repeatPartners = enterpriseRelationMapper.countRepeatPartners(enterpriseId);
        double repeatRate = (double) repeatPartners / totalPartners;
        int repeatScore = (int) (repeatRate * 40);

        int longTermPartners = enterpriseRelationMapper.countLongTermPartners(enterpriseId);
        int longTermScore = Math.min(longTermPartners * 8, 40);

        return Math.min(repeatScore + longTermScore, 80);
    }

    // ================ ERP 履约层评分 ================

    /**
     * E. 订单履约 (满分120)
     */
    private int calcOrderFulfillmentScore(Long enterpriseId) {
        String startDate = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ErpOrderRecord> orders = erpOrderRecordMapper.selectByBuyerAndDateRange(enterpriseId, startDate);

        if (orders.isEmpty()) return 0;

        long totalOrders = orders.size();
        long cancelledOrders = orders.stream().filter(o -> Integer.valueOf(1).equals(o.getCancelled())).count();
        double cancelRate = (double) cancelledOrders / totalOrders;

        int cancelScore;
        if (cancelRate == 0) cancelScore = 50;
        else if (cancelRate < 0.05) cancelScore = 40;
        else if (cancelRate < 0.10) cancelScore = 28;
        else if (cancelRate < 0.20) cancelScore = 15;
        else cancelScore = 0;

        long partialPickup = orders.stream()
                .filter(o -> o.getAgreedPickupQuantity() != null && o.getActualPickupQuantity() != null)
                .filter(o -> o.getActualPickupQuantity().compareTo(o.getAgreedPickupQuantity()) < 0)
                .count();
        long ordersWithQuantity = orders.stream()
                .filter(o -> o.getAgreedPickupQuantity() != null && o.getActualPickupQuantity() != null)
                .count();
        double partialRate = ordersWithQuantity > 0 ? (double) partialPickup / ordersWithQuantity : 0;

        int partialScore;
        if (partialRate == 0) partialScore = 40;
        else if (partialRate < 0.10) partialScore = 30;
        else if (partialRate < 0.20) partialScore = 18;
        else partialScore = 5;

        long cancelAfterStock = orders.stream()
                .filter(o -> Integer.valueOf(1).equals(o.getCancelled()))
                .filter(o -> "AFTER_STOCK".equals(o.getCancelStage()))
                .count();
        int cancelTimingScore;
        if (cancelAfterStock == 0) cancelTimingScore = 30;
        else if (cancelAfterStock <= 1) cancelTimingScore = 15;
        else cancelTimingScore = 0;

        return Math.min(cancelScore + partialScore + cancelTimingScore, 120);
    }

    /**
     * F. 提货履约 (满分100)
     */
    private int calcPickupFulfillmentScore(Long enterpriseId) {
        String startDate = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ErpOrderRecord> orders = erpOrderRecordMapper.selectByBuyerAndDateRange(enterpriseId, startDate);

        List<ErpOrderRecord> completedOrders = orders.stream()
                .filter(o -> !Integer.valueOf(1).equals(o.getCancelled()))
                .filter(o -> o.getAgreedPickupDate() != null && o.getActualPickupDate() != null)
                .collect(Collectors.toList());

        if (completedOrders.isEmpty()) return 50;

        long onTimePickup = completedOrders.stream()
                .filter(o -> !o.getActualPickupDate().isAfter(o.getAgreedPickupDate()))
                .count();
        double pickupOnTimeRate = (double) onTimePickup / completedOrders.size();
        int pickupTimeScore = (int) (pickupOnTimeRate * 50);

        long fullQuantityPickup = completedOrders.stream()
                .filter(o -> o.getAgreedPickupQuantity() != null && o.getActualPickupQuantity() != null)
                .filter(o -> o.getActualPickupQuantity().compareTo(
                        o.getAgreedPickupQuantity().multiply(BigDecimal.valueOf(0.95))) >= 0)
                .count();
        long ordersWithQty = completedOrders.stream()
                .filter(o -> o.getAgreedPickupQuantity() != null && o.getActualPickupQuantity() != null)
                .count();
        double fullQtyRate = ordersWithQty > 0 ? (double) fullQuantityPickup / ordersWithQty : 1.0;
        int fullQtyScore = (int) (fullQtyRate * 30);

        int cooperationScore = 20;

        return Math.min(pickupTimeScore + fullQtyScore + cooperationScore, 100);
    }

    /**
     * G. 付款信用 (满分280) — 核心维度
     */
    private int calcPaymentCreditScore(Long enterpriseId) {
        String startDate = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ErpPaymentRecord> payments = erpPaymentRecordMapper.selectByBuyerAndDateRange(enterpriseId, startDate);

        if (payments.isEmpty()) return 0;

        // 付款及时率 (满分80)
        long totalPayments = payments.size();
        long onTimePayments = payments.stream()
                .filter(p -> "PAID".equals(p.getPaymentStatus()))
                .filter(p -> p.getOverdueDays() == null || p.getOverdueDays() <= 0)
                .count();
        double onTimeRate = (double) onTimePayments / totalPayments;
        int onTimeScore = (int) (onTimeRate * 80);

        // 平均付款周期偏差 (满分40)
        double avgOverdueDays = payments.stream()
                .filter(p -> p.getOverdueDays() != null)
                .mapToInt(ErpPaymentRecord::getOverdueDays)
                .average().orElse(0);
        int deviationScore;
        if (avgOverdueDays <= 0) deviationScore = 40;
        else if (avgOverdueDays <= 7) deviationScore = 30;
        else if (avgOverdueDays <= 15) deviationScore = 18;
        else if (avgOverdueDays <= 30) deviationScore = 8;
        else deviationScore = 0;

        // 超期欠款率 (满分50)
        List<ErpOverdueSummary> summaries = erpOverdueSummaryMapper.selectLatestByBuyer(enterpriseId);
        BigDecimal totalPayable = summaries.stream()
                .map(ErpOverdueSummary::getTotalPayable)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overdueAmount = summaries.stream()
                .map(ErpOverdueSummary::getOverdueAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double overdueRate = totalPayable.compareTo(BigDecimal.ZERO) > 0
                ? overdueAmount.divide(totalPayable, 4, RoundingMode.HALF_UP).doubleValue() : 0;
        int overdueRateScore;
        if (overdueRate == 0) overdueRateScore = 50;
        else if (overdueRate < 0.05) overdueRateScore = 38;
        else if (overdueRate < 0.15) overdueRateScore = 22;
        else if (overdueRate < 0.30) overdueRateScore = 10;
        else overdueRateScore = 0;

        // 严重超期次数 (满分60)
        long severeCount = payments.stream()
                .filter(p -> Integer.valueOf(1).equals(p.getSeverelyOverdue()))
                .count();
        int severeScore;
        if (severeCount == 0) severeScore = 60;
        else if (severeCount == 1) severeScore = 30;
        else if (severeCount == 2) severeScore = 10;
        else severeScore = 0;

        // 当前欠款状态 (满分50)
        boolean hasCurrentOverdue = summaries.stream()
                .anyMatch(s -> s.getOverdueAmount() != null && s.getOverdueAmount().compareTo(BigDecimal.ZERO) > 0);
        boolean hasCurrentOutstanding = summaries.stream()
                .anyMatch(s -> s.getOutstandingBalance() != null && s.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0);
        int currentStatusScore;
        if (!hasCurrentOutstanding) currentStatusScore = 50;
        else if (!hasCurrentOverdue) currentStatusScore = 35;
        else {
            int maxDays = summaries.stream()
                    .mapToInt(s -> s.getMaxOverdueDays() != null ? s.getMaxOverdueDays() : 0)
                    .max().orElse(0);
            if (maxDays <= 30) currentStatusScore = 15;
            else currentStatusScore = 0;
        }

        return Math.min(onTimeScore + deviationScore + overdueRateScore + severeScore + currentStatusScore, 280);
    }

    /**
     * 合作反馈评分 (满分200)
     */
    private int calcFeedbackScore(Long enterpriseId) {
        List<CooperationFeedback> feedbacks = cooperationFeedbackMapper.selectValidByTargetAndRole(
                enterpriseId, "SELLER_RATE_BUYER");

        if (feedbacks.size() < MIN_FEEDBACK_COUNT) return 0;

        // 合作印象加权均分 (满分80) — 使用credibilityWeight加权
        double weightedImpressionSum = feedbacks.stream()
                .filter(f -> f.getOverallImpression() != null)
                .mapToDouble(f -> f.getOverallImpression() * (f.getCredibilityWeight() != null ? f.getCredibilityWeight() : 0.5))
                .sum();
        double totalImpressionWeight = feedbacks.stream()
                .filter(f -> f.getOverallImpression() != null)
                .mapToDouble(f -> f.getCredibilityWeight() != null ? f.getCredibilityWeight() : 0.5)
                .sum();
        double avgImpression = totalImpressionWeight > 0 ? weightedImpressionSum / totalImpressionWeight : 2.5;
        int impressionScore = (int) (avgImpression / 4.0 * 80);

        // 分项评价均分 (满分60)
        double avgDetail = feedbacks.stream()
                .mapToDouble(f -> {
                    int count = 0;
                    double sum = 0;
                    if (f.getPaymentRating() != null) { sum += f.getPaymentRating(); count++; }
                    if (f.getPickupRating() != null) { sum += f.getPickupRating(); count++; }
                    if (f.getOrderStabilityRating() != null) { sum += f.getOrderStabilityRating(); count++; }
                    if (f.getCommunicationRating() != null) { sum += f.getCommunicationRating(); count++; }
                    return count > 0 ? sum / count : 2.5;
                })
                .average().orElse(2.5);
        int detailScore = (int) (avgDetail / 4.0 * 60);

        // 继续合作意愿 (满分40)
        long willContinueCount = feedbacks.stream()
                .filter(f -> f.getWillContinue() != null && f.getWillContinue() == 1)
                .count();
        double willContinueRate = (double) willContinueCount / feedbacks.size();
        int willScore = (int) (willContinueRate * 40);

        // 评价覆盖度 (满分20)
        int totalPartners = enterpriseRelationMapper.countTotalPartners(enterpriseId);
        double coverageRate = totalPartners > 0 ? (double) feedbacks.size() / totalPartners : 0;
        int coverageScore = (int) (Math.min(coverageRate, 1.0) * 20);

        return Math.min(impressionScore + detailScore + willScore + coverageScore, 200);
    }

    /**
     * 多商家ERP一致性加分 (满分150)
     */
    private int calcConsistencyBonus(Long enterpriseId) {
        int distinctSellers = erpOrderRecordMapper.countDistinctSellersByBuyer(enterpriseId);
        if (distinctSellers < 3) return 0;

        List<ErpOverdueSummary> summaries = erpOverdueSummaryMapper.selectLatestByBuyer(enterpriseId);
        if (summaries.isEmpty()) return 0;

        // 多商家一致性加分 (满分80)
        long goodSellers = summaries.stream()
                .filter(s -> s.getSevereOverdueCount() == null || s.getSevereOverdueCount() == 0)
                .count();
        int consistencyScore = (int) ((double) goodSellers / summaries.size() * 80);

        // 零超期加分 (满分40)
        boolean allNoOverdue = summaries.stream()
                .allMatch(s -> (s.getOverdueCount() == null || s.getOverdueCount() == 0));
        int zeroOverdueScore = allNoOverdue ? 40 : 0;

        // 稳定性加分 (满分30)
        List<Double> overdueRates = summaries.stream()
                .map(s -> {
                    if (s.getTotalPayable() == null || s.getTotalPayable().compareTo(BigDecimal.ZERO) == 0) return 0.0;
                    BigDecimal od = s.getOverdueAmount() != null ? s.getOverdueAmount() : BigDecimal.ZERO;
                    return od.divide(s.getTotalPayable(), 4, RoundingMode.HALF_UP).doubleValue();
                })
                .collect(Collectors.toList());
        double variance = calcVariance(overdueRates);
        int stabilityScore = variance < 0.01 ? 30 : variance < 0.05 ? 20 : variance < 0.10 ? 10 : 0;

        return Math.min(consistencyScore + zeroOverdueScore + stabilityScore, 150);
    }

    // ================ 工具方法 ================

    private int scoreByPercentile(double value, int maxScore) {
        if (value >= 50) return maxScore;
        if (value >= 20) return (int) (maxScore * 0.8);
        if (value >= 10) return (int) (maxScore * 0.6);
        if (value >= 5) return (int) (maxScore * 0.4);
        if (value > 0) return (int) (maxScore * 0.2);
        return 0;
    }

    private double calcVariance(List<Double> values) {
        if (values.isEmpty()) return 0;
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
    }
}
