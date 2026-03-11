package com.steel.credit.service.impl;

import com.steel.credit.constant.*;
import com.steel.credit.dto.response.*;
import com.steel.credit.entity.*;
import com.steel.credit.mapper.*;
import com.steel.credit.service.CreditRatingService;
import com.steel.credit.service.RiskWarningService;
import com.steel.credit.service.engine.BuyerCreditEngine;
import com.steel.credit.service.engine.SellerCreditEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditRatingServiceImpl implements CreditRatingService {

    private final BuyerCreditEngine buyerCreditEngine;
    private final SellerCreditEngine sellerCreditEngine;
    private final EnterpriseInfoMapper enterpriseInfoMapper;
    private final BuyerCreditScoreMapper buyerCreditScoreMapper;
    private final SellerCreditScoreMapper sellerCreditScoreMapper;
    private final CreditScoreHistoryMapper creditScoreHistoryMapper;
    private final BuyerOverdueTagMapper buyerOverdueTagMapper;
    private final CooperationFeedbackMapper cooperationFeedbackMapper;
    private final RiskWarningService riskWarningService;

    @Override
    public CreditCardVO getCreditCard(Long enterpriseId) {
        EnterpriseInfo enterprise = enterpriseInfoMapper.selectById(enterpriseId);
        if (enterprise == null) return null;

        CreditCardVO card = new CreditCardVO();
        card.setEnterpriseId(enterpriseId);
        card.setEnterpriseName(enterprise.getEnterpriseName());

        EnterpriseRole role = determineRole(enterprise);
        card.setRole(role.name());

        if (role == EnterpriseRole.BUYER || role == EnterpriseRole.BOTH) {
            card.setBuyerCredit(buildBuyerCreditSummary(enterpriseId));
        }
        if (role == EnterpriseRole.SELLER || role == EnterpriseRole.BOTH) {
            card.setSellerCredit(buildSellerCreditSummary(enterpriseId));
        }

        card.setRiskSummary(riskWarningService.getRiskSummary(enterpriseId));

        return card;
    }

    @Override
    public CreditDetailVO getBuyerCreditDetail(Long enterpriseId) {
        BuyerCreditScore score = buyerCreditScoreMapper.selectLatestByEnterpriseId(enterpriseId);
        EnterpriseInfo enterprise = enterpriseInfoMapper.selectById(enterpriseId);
        if (score == null || enterprise == null) return null;

        CreditDetailVO detail = new CreditDetailVO();
        detail.setEnterpriseId(enterpriseId);
        detail.setEnterpriseName(enterprise.getEnterpriseName());
        detail.setRoleType("BUYER");
        detail.setCreditGrade(score.getCreditGrade());
        detail.setTotalScore(score.getTotalScore());
        detail.setRatingMode(score.getRatingMode());
        detail.setRatingModeDesc(getRatingModeDesc(score.getRatingMode()));
        detail.setDataSufficiency(score.getDataSufficiency());
        detail.setDataSufficiencyLabel(getDataSufficiencyLabel(score.getDataSufficiency()));
        detail.setDataSufficiencyDesc(getDataSufficiencyDesc(score.getDataSufficiency()));
        detail.setGradeCeiling(score.getGradeCeiling());
        detail.setRatingDate(score.getRatingDate());
        detail.setErpSourceCount(score.getErpMerchantCount());
        detail.setFeedbackCount(score.getFeedbackCount());

        detail.setDimensions(buildBuyerDimensions(score));
        detail.setTrend(buildBuyerTrend(enterpriseId));
        detail.setGradeHistory(buildGradeHistory(enterpriseId, "BUYER"));
        detail.setFeedbackSummaries(buildFeedbackSummaries(enterpriseId, "SELLER_RATE_BUYER"));
        detail.setMissingDataHints(buildBuyerMissingHints(score));
        detail.setSuggestions(buildBuyerSuggestions(score));

        return detail;
    }

    @Override
    public CreditDetailVO getSellerCreditDetail(Long enterpriseId) {
        SellerCreditScore score = sellerCreditScoreMapper.selectLatestByEnterpriseId(enterpriseId);
        EnterpriseInfo enterprise = enterpriseInfoMapper.selectById(enterpriseId);
        if (score == null || enterprise == null) return null;

        CreditDetailVO detail = new CreditDetailVO();
        detail.setEnterpriseId(enterpriseId);
        detail.setEnterpriseName(enterprise.getEnterpriseName());
        detail.setRoleType("SELLER");
        detail.setCreditGrade(score.getCreditGrade());
        detail.setTotalScore(score.getTotalScore());
        detail.setRatingMode(score.getRatingMode());
        detail.setRatingModeDesc(getRatingModeDesc(score.getRatingMode()));
        detail.setDataSufficiency(score.getDataSufficiency());
        detail.setDataSufficiencyLabel(getDataSufficiencyLabel(score.getDataSufficiency()));
        detail.setDataSufficiencyDesc(getDataSufficiencyDesc(score.getDataSufficiency()));
        detail.setGradeCeiling(score.getGradeCeiling());
        detail.setRatingDate(score.getRatingDate());
        detail.setErpSourceCount(score.getErpBuyerCount());
        detail.setFeedbackCount(score.getFeedbackCount());

        detail.setDimensions(buildSellerDimensions(score));
        detail.setTrend(buildSellerTrend(enterpriseId));
        detail.setGradeHistory(buildGradeHistory(enterpriseId, "SELLER"));
        detail.setFeedbackSummaries(buildFeedbackSummaries(enterpriseId, "BUYER_RATE_SELLER"));
        detail.setMissingDataHints(buildSellerMissingHints(score));
        detail.setSuggestions(buildSellerSuggestions(score));

        return detail;
    }

    @Override
    public List<InquiryCreditBadgeVO> getCreditBadges(List<Long> enterpriseIds) {
        return enterpriseIds.stream().map(id -> {
            InquiryCreditBadgeVO badge = new InquiryCreditBadgeVO();
            badge.setEnterpriseId(id);

            EnterpriseInfo enterprise = enterpriseInfoMapper.selectById(id);
            if (enterprise != null) {
                badge.setEnterpriseName(enterprise.getEnterpriseName());
            }

            BuyerCreditScore buyerScore = buyerCreditScoreMapper.selectLatestByEnterpriseId(id);
            if (buyerScore != null) {
                badge.setBuyerGrade(buyerScore.getCreditGrade());
                badge.setBuyerDataSufficiency(buyerScore.getDataSufficiency());
            }

            SellerCreditScore sellerScore = sellerCreditScoreMapper.selectLatestByEnterpriseId(id);
            if (sellerScore != null) {
                badge.setSellerGrade(sellerScore.getCreditGrade());
                badge.setSellerDataSufficiency(sellerScore.getDataSufficiency());
            }

            RiskSummaryVO riskSummary = riskWarningService.getRiskSummary(id);
            if (riskSummary != null) {
                badge.setRiskLevel(riskSummary.getOverallRiskLevel());
            }

            BuyerOverdueTag tag = buyerOverdueTagMapper.selectActiveByBuyer(id);
            if (tag != null) {
                badge.setOverdueTag(tag.getOverdueLevel());
                badge.setHasSevereOverdue("SEVERE".equals(tag.getOverdueLevel())
                        || "MALICIOUS".equals(tag.getOverdueLevel()));
            }

            return badge;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void calculateCreditScore(Long enterpriseId) {
        EnterpriseInfo enterprise = enterpriseInfoMapper.selectById(enterpriseId);
        if (enterprise == null) return;

        EnterpriseRole role = determineRole(enterprise);

        if (role == EnterpriseRole.BUYER || role == EnterpriseRole.BOTH) {
            BuyerCreditScore oldScore = buyerCreditScoreMapper.selectLatestByEnterpriseId(enterpriseId);
            BuyerCreditScore newScore = buyerCreditEngine.calculate(enterpriseId);
            if (newScore != null) {
                buyerCreditScoreMapper.insert(newScore);
                recordHistoryIfChanged(enterpriseId, "BUYER", oldScore, newScore);
            }
        }

        if (role == EnterpriseRole.SELLER || role == EnterpriseRole.BOTH) {
            SellerCreditScore oldScore = sellerCreditScoreMapper.selectLatestByEnterpriseId(enterpriseId);
            SellerCreditScore newScore = sellerCreditEngine.calculate(enterpriseId);
            if (newScore != null) {
                sellerCreditScoreMapper.insert(newScore);
                recordSellerHistoryIfChanged(enterpriseId, oldScore, newScore);
            }
        }
    }

    @Override
    public void batchCalculateAllScores() {
        List<Long> allIds = enterpriseInfoMapper.selectAllIds();
        log.info("开始批量计算信用评分, 企业总数={}", allIds.size());

        for (Long id : allIds) {
            try {
                calculateCreditScore(id);
            } catch (Exception e) {
                log.error("计算企业 {} 信用评分失败", id, e);
            }
        }

        log.info("批量信用评分计算完成");
    }

    // ============ 私有方法 ============

    private EnterpriseRole determineRole(EnterpriseInfo enterprise) {
        if (enterprise.getRole() != null) {
            return switch (enterprise.getRole()) {
                case 1 -> EnterpriseRole.BUYER;
                case 2 -> EnterpriseRole.SELLER;
                case 3 -> EnterpriseRole.BOTH;
                default -> EnterpriseRole.BOTH;
            };
        }
        return EnterpriseRole.BOTH;
    }

    private CreditCardVO.CreditSummary buildBuyerCreditSummary(Long enterpriseId) {
        BuyerCreditScore score = buyerCreditScoreMapper.selectLatestByEnterpriseId(enterpriseId);
        if (score == null) return null;

        CreditCardVO.CreditSummary summary = new CreditCardVO.CreditSummary();
        summary.setCreditGrade(score.getCreditGrade());
        summary.setTotalScore(score.getTotalScore());
        summary.setDataSufficiency(score.getDataSufficiency());
        summary.setDataSufficiencyLabel(getDataSufficiencyLabel(score.getDataSufficiency()));
        summary.setRatingMode(score.getRatingMode());
        summary.setDataSources(buildDataSources(score.getRatingMode(), score.getErpMerchantCount(), score.getFeedbackCount()));

        List<CreditCardVO.CoreIndicator> indicators = new ArrayList<>();
        addIndicator(indicators, "付款信用", "PAYMENT", score.getPaymentCreditScore(), 280);
        addIndicator(indicators, "订单履约", "ORDER", score.getOrderFulfillmentScore(), 120);
        addIndicator(indicators, "提货履约", "PICKUP", score.getPickupFulfillmentScore(), 100);
        summary.setCoreIndicators(indicators);

        BuyerOverdueTag tag = buyerOverdueTagMapper.selectActiveByBuyer(enterpriseId);
        if (tag != null && !"NORMAL".equals(tag.getOverdueLevel())) {
            summary.setOverdueTag(tag.getOverdueLevel());
            summary.setOverdueTagDesc(OverdueLevel.valueOf(tag.getOverdueLevel()).getDescription());
        }

        return summary;
    }

    private CreditCardVO.CreditSummary buildSellerCreditSummary(Long enterpriseId) {
        SellerCreditScore score = sellerCreditScoreMapper.selectLatestByEnterpriseId(enterpriseId);
        if (score == null) return null;

        CreditCardVO.CreditSummary summary = new CreditCardVO.CreditSummary();
        summary.setCreditGrade(score.getCreditGrade());
        summary.setTotalScore(score.getTotalScore());
        summary.setDataSufficiency(score.getDataSufficiency());
        summary.setDataSufficiencyLabel(getDataSufficiencyLabel(score.getDataSufficiency()));
        summary.setRatingMode(score.getRatingMode());
        summary.setDataSources(buildDataSources(score.getRatingMode(), score.getErpBuyerCount(), score.getFeedbackCount()));

        List<CreditCardVO.CoreIndicator> indicators = new ArrayList<>();
        addIndicator(indicators, "报价服务", "QUOTE_SERVICE", score.getQuoteServiceScore(), 150);
        addIndicator(indicators, "供应能力", "SUPPLY", score.getSupplyCapabilityScore(), 100);
        addIndicator(indicators, "合作评价", "FEEDBACK", score.getFeedbackScore(), 150);
        summary.setCoreIndicators(indicators);

        return summary;
    }

    private List<CreditCardVO.DataSourceItem> buildDataSources(String mode, Integer erpCount, Integer feedbackCount) {
        List<CreditCardVO.DataSourceItem> sources = new ArrayList<>();

        CreditCardVO.DataSourceItem platform = new CreditCardVO.DataSourceItem();
        platform.setName("平台询报价数据");
        platform.setAvailable(true);
        sources.add(platform);

        CreditCardVO.DataSourceItem erp = new CreditCardVO.DataSourceItem();
        erp.setName("ERP履约数据");
        erp.setAvailable("FULL".equals(mode));
        if (erpCount != null && erpCount > 0) {
            erp.setDetail(erpCount + "家商家");
        }
        sources.add(erp);

        CreditCardVO.DataSourceItem feedback = new CreditCardVO.DataSourceItem();
        feedback.setName("合作方评价");
        feedback.setAvailable(feedbackCount != null && feedbackCount >= 5);
        if (feedbackCount != null && feedbackCount > 0) {
            feedback.setDetail(feedbackCount + "条");
        }
        sources.add(feedback);

        return sources;
    }

    private void addIndicator(List<CreditCardVO.CoreIndicator> list, String name, String code, Integer score, int maxScore) {
        CreditCardVO.CoreIndicator ind = new CreditCardVO.CoreIndicator();
        ind.setName(name);
        ind.setCode(code);
        ind.setScore(score != null ? score : 0);
        ind.setMaxScore(maxScore);
        ind.setPercentage(maxScore > 0 ? (int) ((score != null ? score : 0) * 100.0 / maxScore) : 0);
        if (ind.getPercentage() >= 80) ind.setLevel("优秀");
        else if (ind.getPercentage() >= 60) ind.setLevel("良好");
        else if (ind.getPercentage() >= 40) ind.setLevel("中等");
        else ind.setLevel("较弱");
        list.add(ind);
    }

    private List<CreditDetailVO.DimensionScore> buildBuyerDimensions(BuyerCreditScore score) {
        List<CreditDetailVO.DimensionScore> dims = new ArrayList<>();
        dims.add(buildDim("BASIC", "企业基础实力", "平台", score.getBasicScore(), 100));
        dims.add(buildDim("ACTIVITY", "采购活跃度", "平台", score.getActivityScore(), 120));
        dims.add(buildDim("BEHAVIOR", "采购行为质量", "平台", score.getBehaviorQualityScore(), 100));
        dims.add(buildDim("RELATION", "合作关系持续性", "平台", score.getRelationshipScore(), 80));

        if ("FULL".equals(score.getRatingMode())) {
            dims.add(buildDim("ORDER_FULFILL", "订单履约", "ERP", score.getOrderFulfillmentScore(), 120));
            dims.add(buildDim("PICKUP_FULFILL", "提货履约", "ERP", score.getPickupFulfillmentScore(), 100));
            dims.add(buildDim("PAYMENT_CREDIT", "付款信用", "ERP", score.getPaymentCreditScore(), 280));
            dims.add(buildDim("CONSISTENCY", "多商家一致性", "ERP", score.getConsistencyBonus(), 150));
        }

        if ("FULL".equals(score.getRatingMode()) || "PLATFORM_FEEDBACK".equals(score.getRatingMode())) {
            dims.add(buildDim("FEEDBACK", "合作方评价", "反馈", score.getFeedbackScore(), 200));
        }

        return dims;
    }

    private List<CreditDetailVO.DimensionScore> buildSellerDimensions(SellerCreditScore score) {
        List<CreditDetailVO.DimensionScore> dims = new ArrayList<>();
        dims.add(buildDim("BASIC", "企业基础实力", "平台", score.getBasicScore(), 100));
        dims.add(buildDim("QUOTE_SERVICE", "报价服务质量", "平台", score.getQuoteServiceScore(), 150));
        dims.add(buildDim("SUPPLY", "供应能力", "平台", score.getSupplyCapabilityScore(), 100));
        dims.add(buildDim("RELATION", "关系持续性", "平台", score.getRelationshipScore(), 100));

        if ("FULL".equals(score.getRatingMode())) {
            dims.add(buildDim("DELIVERY", "交货履约", "ERP", score.getDeliveryFulfillmentScore(), 200));
            dims.add(buildDim("AFTER_SALES", "售后与纠纷", "ERP", score.getAfterSalesScore(), 100));
            dims.add(buildDim("CONSISTENCY", "多方一致性", "综合", score.getConsistencyScore(), 100));
        }

        if ("FULL".equals(score.getRatingMode()) || "PLATFORM_FEEDBACK".equals(score.getRatingMode())) {
            dims.add(buildDim("FEEDBACK", "合作方评价", "反馈", score.getFeedbackScore(), 150));
        }

        return dims;
    }

    private CreditDetailVO.DimensionScore buildDim(String code, String name, String source, Integer score, int maxScore) {
        CreditDetailVO.DimensionScore dim = new CreditDetailVO.DimensionScore();
        dim.setDimensionCode(code);
        dim.setDimensionName(name);
        dim.setDataSource(source);
        dim.setScore(score != null ? score : 0);
        dim.setMaxScore(maxScore);
        dim.setPercentage(maxScore > 0 ? (int) ((score != null ? score : 0) * 100.0 / maxScore) : 0);
        dim.setTrend("→");
        return dim;
    }

    private List<CreditDetailVO.TrendPoint> buildBuyerTrend(Long enterpriseId) {
        List<BuyerCreditScore> history = buyerCreditScoreMapper.selectHistoryByEnterpriseId(enterpriseId, 12);
        return history.stream()
                .map(h -> {
                    CreditDetailVO.TrendPoint point = new CreditDetailVO.TrendPoint();
                    point.setPeriod(h.getRatingPeriod());
                    point.setScore(h.getTotalScore());
                    point.setGrade(h.getCreditGrade());
                    return point;
                })
                .collect(Collectors.toList());
    }

    private List<CreditDetailVO.TrendPoint> buildSellerTrend(Long enterpriseId) {
        List<SellerCreditScore> history = sellerCreditScoreMapper.selectHistoryByEnterpriseId(enterpriseId, 12);
        return history.stream()
                .map(h -> {
                    CreditDetailVO.TrendPoint point = new CreditDetailVO.TrendPoint();
                    point.setPeriod(h.getRatingPeriod());
                    point.setScore(h.getTotalScore());
                    point.setGrade(h.getCreditGrade());
                    return point;
                })
                .collect(Collectors.toList());
    }

    private List<CreditDetailVO.GradeChange> buildGradeHistory(Long enterpriseId, String roleType) {
        List<CreditScoreHistory> records = creditScoreHistoryMapper.selectByEnterpriseAndRole(enterpriseId, roleType, 10);
        return records.stream().map(h -> {
            CreditDetailVO.GradeChange change = new CreditDetailVO.GradeChange();
            change.setPeriod(h.getRatingPeriod());
            change.setFromGrade(h.getPreviousGrade());
            change.setToGrade(h.getCurrentGrade());
            change.setFromScore(h.getPreviousScore());
            change.setToScore(h.getCurrentScore());
            return change;
        }).collect(Collectors.toList());
    }

    private List<CreditDetailVO.FeedbackSummary> buildFeedbackSummaries(Long enterpriseId, String role) {
        List<CooperationFeedback> feedbacks = cooperationFeedbackMapper.selectValidByTargetAndRole(enterpriseId, role);
        return feedbacks.stream()
                .filter(f -> f.getComment() != null && !f.getComment().isEmpty())
                .limit(3)
                .map(f -> {
                    CreditDetailVO.FeedbackSummary s = new CreditDetailVO.FeedbackSummary();
                    s.setComment(f.getComment());
                    s.setOverallImpression(f.getOverallImpression());
                    return s;
                })
                .collect(Collectors.toList());
    }

    private List<String> buildBuyerMissingHints(BuyerCreditScore score) {
        List<String> hints = new ArrayList<>();
        if (!"FULL".equals(score.getRatingMode())) {
            hints.add("付款信用数据 — 暂无（需商家接入ERP）");
            hints.add("订单履约数据 — 暂无（需商家接入ERP）");
            hints.add("提货履约数据 — 暂无（需商家接入ERP）");
        }
        if (score.getFeedbackCount() == null || score.getFeedbackCount() < 5) {
            hints.add("合作方评价 — 不足5条（当前" + (score.getFeedbackCount() != null ? score.getFeedbackCount() : 0) + "条）");
        }
        return hints;
    }

    private List<String> buildBuyerSuggestions(BuyerCreditScore score) {
        List<String> suggestions = new ArrayList<>();
        if ("BASIC".equals(score.getRatingMode())) {
            suggestions.add("该买家目前无法评估付款信用和订单履约情况。如果是首次合作，建议采用预付款或货到付款方式");
            suggestions.add("建议从小额交易开始建立信任");
        }
        if (!"FULL".equals(score.getRatingMode())) {
            suggestions.add("如您已接入ERP且与该企业有交易记录，其履约数据将自动纳入信用评估");
        }
        return suggestions;
    }

    private List<String> buildSellerMissingHints(SellerCreditScore score) {
        List<String> hints = new ArrayList<>();
        if (!"FULL".equals(score.getRatingMode())) {
            hints.add("交货履约数据 — 暂无（需接入ERP）");
            hints.add("售后纠纷数据 — 暂无（需接入ERP）");
        }
        if (score.getFeedbackCount() == null || score.getFeedbackCount() < 5) {
            hints.add("合作方评价 — 不足5条");
        }
        return hints;
    }

    private List<String> buildSellerSuggestions(SellerCreditScore score) {
        List<String> suggestions = new ArrayList<>();
        if ("BASIC".equals(score.getRatingMode())) {
            suggestions.add("该卖家目前仅基于平台行为评级，建议关注其报价响应率和库存维护情况");
        }
        return suggestions;
    }

    private void recordHistoryIfChanged(Long enterpriseId, String roleType,
                                        BuyerCreditScore oldScore, BuyerCreditScore newScore) {
        if (oldScore != null && !Objects.equals(oldScore.getCreditGrade(), newScore.getCreditGrade())) {
            CreditScoreHistory history = new CreditScoreHistory();
            history.setEnterpriseId(enterpriseId);
            history.setRoleType(roleType);
            history.setPreviousGrade(oldScore.getCreditGrade());
            history.setPreviousScore(oldScore.getTotalScore());
            history.setCurrentGrade(newScore.getCreditGrade());
            history.setCurrentScore(newScore.getTotalScore());
            history.setRatingPeriod(newScore.getRatingPeriod());
            history.setChangeReason("定期评估等级变更");
            creditScoreHistoryMapper.insert(history);
        }
    }

    private void recordSellerHistoryIfChanged(Long enterpriseId,
                                              SellerCreditScore oldScore, SellerCreditScore newScore) {
        if (oldScore != null && !Objects.equals(oldScore.getCreditGrade(), newScore.getCreditGrade())) {
            CreditScoreHistory history = new CreditScoreHistory();
            history.setEnterpriseId(enterpriseId);
            history.setRoleType("SELLER");
            history.setPreviousGrade(oldScore.getCreditGrade());
            history.setPreviousScore(oldScore.getTotalScore());
            history.setCurrentGrade(newScore.getCreditGrade());
            history.setCurrentScore(newScore.getTotalScore());
            history.setRatingPeriod(newScore.getRatingPeriod());
            history.setChangeReason("定期评估等级变更");
            creditScoreHistoryMapper.insert(history);
        }
    }

    private String getRatingModeDesc(String mode) {
        return switch (mode) {
            case "FULL" -> "全量数据模式（平台行为+ERP+合作反馈）";
            case "PLATFORM_FEEDBACK" -> "平台+反馈模式（平台行为+合作反馈）";
            case "BASIC" -> "基础模式（仅平台行为数据）";
            default -> mode;
        };
    }

    private String getDataSufficiencyLabel(Integer level) {
        if (level == null) return "未知";
        for (DataSufficiency ds : DataSufficiency.values()) {
            if (ds.getLevel() == level) return ds.getLabel();
        }
        return "未知";
    }

    private String getDataSufficiencyDesc(Integer level) {
        if (level == null) return "";
        for (DataSufficiency ds : DataSufficiency.values()) {
            if (ds.getLevel() == level) return ds.getDescription();
        }
        return "";
    }
}
