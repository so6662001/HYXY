package com.steel.credit.service.engine;

import com.steel.credit.constant.*;
import com.steel.credit.entity.*;
import com.steel.credit.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 卖家信用评分引擎
 * <p>
 * 三种评级模式:
 * - FULL: 平台行为(30%) + ERP(40%) + 合作反馈(15%) + 一致性(15%)
 * - PLATFORM_FEEDBACK: 平台行为(55%) + 合作反馈(45%)
 * - BASIC: 平台行为(100%)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SellerCreditEngine {

    private final InquiryQuoteStatMapper inquiryQuoteStatMapper;
    private final EnterpriseRelationMapper enterpriseRelationMapper;
    private final ErpOrderRecordMapper erpOrderRecordMapper;
    private final CooperationFeedbackMapper cooperationFeedbackMapper;
    private final EnterpriseInfoMapper enterpriseInfoMapper;

    private static final int TOTAL_SCORE = 1000;
    private static final int MIN_ERP_RECORDS = 3;
    private static final int MIN_FEEDBACK_COUNT = 5;

    public SellerCreditScore calculate(Long enterpriseId) {
        log.info("开始计算卖家信用评分, enterpriseId={}", enterpriseId);

        EnterpriseInfo enterprise = enterpriseInfoMapper.selectById(enterpriseId);
        if (enterprise == null) return null;

        RatingMode mode = determineRatingMode(enterpriseId);

        SellerCreditScore score = new SellerCreditScore();
        score.setEnterpriseId(enterpriseId);
        score.setRatingPeriod(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        score.setRatingMode(mode.getCode());
        score.setRatingDate(LocalDate.now());
        score.setEffectiveDate(LocalDate.now().plusDays(1));

        int basicScore = calcBasicScore(enterprise);
        int quoteServiceScore = calcQuoteServiceScore(enterpriseId);
        int supplyCapabilityScore = calcSupplyCapabilityScore(enterpriseId, enterprise);
        int relationshipScore = calcRelationshipScore(enterpriseId);

        score.setBasicScore(basicScore);
        score.setQuoteServiceScore(quoteServiceScore);
        score.setSupplyCapabilityScore(supplyCapabilityScore);
        score.setRelationshipScore(relationshipScore);

        int platformRawTotal = basicScore + quoteServiceScore + supplyCapabilityScore + relationshipScore;

        int totalScoreValue;

        switch (mode) {
            case FULL -> {
                int deliveryFulfillment = calcDeliveryFulfillmentScore(enterpriseId);
                int afterSales = calcAfterSalesScore(enterpriseId);
                int feedback = calcFeedbackScore(enterpriseId);
                int consistency = calcConsistencyScore(enterpriseId);

                score.setDeliveryFulfillmentScore(deliveryFulfillment);
                score.setAfterSalesScore(afterSales);
                score.setFeedbackScore(feedback);
                score.setConsistencyScore(consistency);

                int erpRawTotal = deliveryFulfillment + afterSales;

                totalScoreValue = (int) (platformRawTotal / 450.0 * 300
                        + erpRawTotal / 300.0 * 400
                        + feedback / 150.0 * 150
                        + consistency / 100.0 * 150);

                score.setErpBuyerCount(countDistinctBuyers(enterpriseId));
                score.setFeedbackCount(cooperationFeedbackMapper.countValidByTargetAndRole(enterpriseId, "BUYER_RATE_SELLER"));
                score.setDataSufficiency(DataSufficiency.FULL.getLevel());
                score.setGradeCeiling(CreditGrade.AAA.getCode());
            }
            case PLATFORM_FEEDBACK -> {
                int feedback = calcFeedbackScore(enterpriseId);
                score.setFeedbackScore(feedback);
                score.setDeliveryFulfillmentScore(0);
                score.setAfterSalesScore(0);
                score.setConsistencyScore(0);

                totalScoreValue = (int) (platformRawTotal / 450.0 * 550
                        + feedback / 150.0 * 450);

                score.setErpBuyerCount(0);
                score.setFeedbackCount(cooperationFeedbackMapper.countValidByTargetAndRole(enterpriseId, "BUYER_RATE_SELLER"));
                score.setDataSufficiency(DataSufficiency.MEDIUM.getLevel());
                score.setGradeCeiling(CreditGrade.AA.getCode());
            }
            default -> {
                score.setDeliveryFulfillmentScore(0);
                score.setAfterSalesScore(0);
                score.setFeedbackScore(0);
                score.setConsistencyScore(0);

                totalScoreValue = (int) (platformRawTotal / 450.0 * TOTAL_SCORE);

                score.setErpBuyerCount(0);
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

        log.info("卖家信用评分完成: enterpriseId={}, score={}, grade={}", enterpriseId, totalScoreValue, grade.getCode());
        return score;
    }

    private RatingMode determineRatingMode(Long enterpriseId) {
        String startDate = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ErpOrderRecord> orders = erpOrderRecordMapper.selectBySellerAndDateRange(enterpriseId, startDate);
        int feedbackCount = cooperationFeedbackMapper.countValidByTargetAndRole(enterpriseId, "BUYER_RATE_SELLER");

        if (orders.size() >= MIN_ERP_RECORDS) {
            return RatingMode.FULL;
        } else if (feedbackCount >= MIN_FEEDBACK_COUNT) {
            return RatingMode.PLATFORM_FEEDBACK;
        } else {
            return RatingMode.BASIC;
        }
    }

    // ================ 平台行为层 ================

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

        score += 25;
        return Math.min(score, 100);
    }

    /**
     * J. 报价服务质量 (满分150)
     */
    private int calcQuoteServiceScore(Long enterpriseId) {
        String startMonth = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<InquiryQuoteStat> stats = inquiryQuoteStatMapper.selectByEnterpriseAndMonthRange(enterpriseId, startMonth);
        if (stats.isEmpty()) return 0;

        int totalReceived = stats.stream()
                .mapToInt(s -> s.getInquiryReceivedCount() != null ? s.getInquiryReceivedCount() : 0).sum();
        int totalResponded = stats.stream()
                .mapToInt(s -> s.getInquiryRespondedCount() != null ? s.getInquiryRespondedCount() : 0).sum();
        double responseRate = totalReceived > 0 ? (double) totalResponded / totalReceived : 0;
        int responseRateScore = (int) (responseRate * 40);

        double avgHours = stats.stream()
                .map(InquiryQuoteStat::getAvgResponseHours)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(24);
        int responseSpeedScore;
        if (avgHours <= 2) responseSpeedScore = 40;
        else if (avgHours <= 8) responseSpeedScore = 30;
        else if (avgHours <= 24) responseSpeedScore = 18;
        else responseSpeedScore = 8;

        double avgDeviation = stats.stream()
                .map(InquiryQuoteStat::getQuotePriceDeviationPct)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(20);
        int deviationScore;
        if (avgDeviation < 3) deviationScore = 35;
        else if (avgDeviation < 5) deviationScore = 28;
        else if (avgDeviation < 10) deviationScore = 18;
        else deviationScore = 8;

        int activeMonths = (int) stats.stream()
                .filter(s -> s.getQuoteSentCount() != null && s.getQuoteSentCount() > 0).count();
        int stabilityScore = (int) (activeMonths / 12.0 * 35);

        return Math.min(responseRateScore + responseSpeedScore + deviationScore + stabilityScore, 150);
    }

    /**
     * K. 供应能力 (满分100)
     */
    private int calcSupplyCapabilityScore(Long enterpriseId, EnterpriseInfo enterprise) {
        String startMonth = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<InquiryQuoteStat> stats = inquiryQuoteStatMapper.selectByEnterpriseAndMonthRange(enterpriseId, startMonth);

        int beenInquired = stats.stream()
                .mapToInt(s -> s.getInquiryReceivedCount() != null ? s.getInquiryReceivedCount() : 0).sum();
        int heatScore = Math.min(beenInquired / 5, 30);

        int totalUpdates = stats.stream()
                .mapToInt(s -> s.getInventoryUpdateCount() != null ? s.getInventoryUpdateCount() : 0).sum();
        int inventoryScore = Math.min(totalUpdates / 2, 40);

        int partnerCount = enterpriseRelationMapper.countTotalPartners(enterpriseId);
        int breadthScore = Math.min(partnerCount * 3, 30);

        return Math.min(heatScore + inventoryScore + breadthScore, 100);
    }

    /**
     * O. 关系持续性 (满分100)
     */
    private int calcRelationshipScore(Long enterpriseId) {
        int totalPartners = enterpriseRelationMapper.countTotalPartners(enterpriseId);
        if (totalPartners == 0) return 0;

        int repeatPartners = enterpriseRelationMapper.countRepeatPartners(enterpriseId);
        double repeatRate = (double) repeatPartners / totalPartners;
        int repeatScore = (int) (repeatRate * 50);

        int longTermPartners = enterpriseRelationMapper.countLongTermPartners(enterpriseId);
        int longTermScore = Math.min(longTermPartners * 10, 50);

        return Math.min(repeatScore + longTermScore, 100);
    }

    // ================ ERP 层 ================

    /**
     * L. 交货履约 (满分200)
     */
    private int calcDeliveryFulfillmentScore(Long enterpriseId) {
        String startDate = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ErpOrderRecord> orders = erpOrderRecordMapper.selectBySellerAndDateRange(enterpriseId, startDate);

        List<ErpOrderRecord> completed = orders.stream()
                .filter(o -> !Integer.valueOf(1).equals(o.getCancelled()))
                .collect(Collectors.toList());
        if (completed.isEmpty()) return 100;

        List<ErpOrderRecord> withShipment = completed.stream()
                .filter(o -> o.getAgreedShipmentDate() != null && o.getShipmentDate() != null)
                .collect(Collectors.toList());
        long onTime = withShipment.stream()
                .filter(o -> !o.getShipmentDate().isAfter(o.getAgreedShipmentDate()))
                .count();
        double onTimeRate = !withShipment.isEmpty() ? (double) onTime / withShipment.size() : 1.0;
        int onTimeScore = (int) (onTimeRate * 80);

        List<ErpOrderRecord> withQty = completed.stream()
                .filter(o -> o.getAgreedPickupQuantity() != null && o.getActualPickupQuantity() != null)
                .collect(Collectors.toList());
        long fullQty = withQty.stream()
                .filter(o -> o.getActualPickupQuantity().compareTo(
                        o.getAgreedPickupQuantity().multiply(BigDecimal.valueOf(0.95))) >= 0)
                .count();
        double fullRate = !withQty.isEmpty() ? (double) fullQty / withQty.size() : 1.0;
        int fullQtyScore = (int) (fullRate * 60);

        long qualityOk = completed.stream()
                .filter(o -> !Integer.valueOf(0).equals(o.getQualityPassed()))
                .count();
        double qualityRate = (double) qualityOk / completed.size();
        int qualityScore = (int) (qualityRate * 60);

        return Math.min(onTimeScore + fullQtyScore + qualityScore, 200);
    }

    /**
     * M. 售后与纠纷 (满分100)
     */
    private int calcAfterSalesScore(Long enterpriseId) {
        String startDate = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ErpOrderRecord> orders = erpOrderRecordMapper.selectBySellerAndDateRange(enterpriseId, startDate);

        List<ErpOrderRecord> completed = orders.stream()
                .filter(o -> !Integer.valueOf(1).equals(o.getCancelled()))
                .collect(Collectors.toList());
        if (completed.isEmpty()) return 80;

        long returnCount = completed.stream()
                .filter(o -> Integer.valueOf(1).equals(o.getReturned()))
                .count();
        double returnRate = (double) returnCount / completed.size();
        int returnScore;
        if (returnRate == 0) returnScore = 50;
        else if (returnRate < 0.02) returnScore = 40;
        else if (returnRate < 0.05) returnScore = 25;
        else returnScore = 10;

        long qualityIssueCount = completed.stream()
                .filter(o -> Integer.valueOf(0).equals(o.getQualityPassed()))
                .count();
        double issueRate = (double) qualityIssueCount / completed.size();
        int issueScore;
        if (issueRate == 0) issueScore = 50;
        else if (issueRate < 0.03) issueScore = 38;
        else if (issueRate < 0.08) issueScore = 22;
        else issueScore = 8;

        return Math.min(returnScore + issueScore, 100);
    }

    /**
     * N. 合作反馈 (满分150)
     */
    private int calcFeedbackScore(Long enterpriseId) {
        List<CooperationFeedback> feedbacks = cooperationFeedbackMapper.selectValidByTargetAndRole(
                enterpriseId, "BUYER_RATE_SELLER");
        if (feedbacks.size() < MIN_FEEDBACK_COUNT) return 0;

        double weightedSum = feedbacks.stream()
                .filter(f -> f.getOverallImpression() != null)
                .mapToDouble(f -> f.getOverallImpression() * (f.getCredibilityWeight() != null ? f.getCredibilityWeight() : 0.5))
                .sum();
        double totalWeight = feedbacks.stream()
                .filter(f -> f.getOverallImpression() != null)
                .mapToDouble(f -> f.getCredibilityWeight() != null ? f.getCredibilityWeight() : 0.5)
                .sum();
        double avgImpression = totalWeight > 0 ? weightedSum / totalWeight : 2.5;
        int impressionScore = (int) (avgImpression / 4.0 * 60);

        double avgDetail = feedbacks.stream()
                .mapToDouble(f -> {
                    int count = 0; double sum = 0;
                    if (f.getDeliveryRating() != null) { sum += f.getDeliveryRating(); count++; }
                    if (f.getQualityRating() != null) { sum += f.getQualityRating(); count++; }
                    if (f.getPricingRating() != null) { sum += f.getPricingRating(); count++; }
                    if (f.getCommunicationRating() != null) { sum += f.getCommunicationRating(); count++; }
                    return count > 0 ? sum / count : 2.5;
                }).average().orElse(2.5);
        int detailScore = (int) (avgDetail / 4.0 * 50);

        long willCont = feedbacks.stream()
                .filter(f -> f.getWillContinue() != null && f.getWillContinue() == 1).count();
        int willScore = (int) ((double) willCont / feedbacks.size() * 40);

        return Math.min(impressionScore + detailScore + willScore, 150);
    }

    /**
     * P. 多方一致性 (满分100)
     */
    private int calcConsistencyScore(Long enterpriseId) {
        List<CooperationFeedback> feedbacks = cooperationFeedbackMapper.selectValidByTargetAndRole(
                enterpriseId, "BUYER_RATE_SELLER");
        if (feedbacks.size() < 3) return 50;

        List<Integer> impressions = feedbacks.stream()
                .map(CooperationFeedback::getOverallImpression)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        double mean = impressions.stream().mapToInt(Integer::intValue).average().orElse(2.5);
        double variance = impressions.stream().mapToDouble(i -> Math.pow(i - mean, 2)).average().orElse(0);

        if (variance < 0.3) return 100;
        if (variance < 0.6) return 75;
        if (variance < 1.0) return 50;
        return 25;
    }

    private int countDistinctBuyers(Long enterpriseId) {
        String startDate = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return erpOrderRecordMapper.countDistinctBuyersBySeller(enterpriseId, startDate);
    }

}
