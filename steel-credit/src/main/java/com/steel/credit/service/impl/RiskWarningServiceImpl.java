package com.steel.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steel.credit.constant.RiskCategory;
import com.steel.credit.constant.RiskLevel;
import com.steel.credit.dto.response.RiskDetailVO;
import com.steel.credit.dto.response.RiskSummaryVO;
import com.steel.credit.entity.*;
import com.steel.credit.mapper.*;
import com.steel.credit.service.RiskWarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskWarningServiceImpl implements RiskWarningService {

    private static final String DISCLAIMER = "以下信息来源于公开工商司法数据，仅供参考，不构成信用评级依据。风险预警与信用评级相互独立。";

    private final RiskEventMapper riskEventMapper;
    private final RiskSnapshotMapper riskSnapshotMapper;
    private final RelatedPartyInfoMapper relatedPartyInfoMapper;
    private final EnterpriseInfoMapper enterpriseInfoMapper;

    @Override
    public RiskSummaryVO getRiskSummary(Long enterpriseId) {
        RiskSummaryVO summary = new RiskSummaryVO();
        summary.setEnterpriseId(enterpriseId);

        RiskSnapshot snapshot = riskSnapshotMapper.selectLatestByEnterpriseId(enterpriseId);
        if (snapshot != null) {
            summary.setOverallRiskLevel(snapshot.getOverallRiskLevel());
            summary.setHighRiskCount(snapshot.getHighRiskCount() != null ? snapshot.getHighRiskCount() : 0);
            summary.setMediumRiskCount(snapshot.getMediumRiskCount() != null ? snapshot.getMediumRiskCount() : 0);
            summary.setLowRiskCount(snapshot.getLowRiskCount() != null ? snapshot.getLowRiskCount() : 0);
            summary.setSelfRiskCount(snapshot.getSelfRiskCount() != null ? snapshot.getSelfRiskCount() : 0);
            summary.setRelatedPartyRiskCount(snapshot.getRelatedPartyRiskCount() != null ? snapshot.getRelatedPartyRiskCount() : 0);
            summary.setLastCheckDate(snapshot.getSnapshotDate());
        } else {
            summary.setOverallRiskLevel(RiskLevel.NONE.name());
            summary.setHighRiskCount(0);
            summary.setMediumRiskCount(0);
            summary.setLowRiskCount(0);
            summary.setSelfRiskCount(0);
            summary.setRelatedPartyRiskCount(0);
        }

        summary.setTotalRiskCount(summary.getHighRiskCount() + summary.getMediumRiskCount() + summary.getLowRiskCount());
        summary.setOverallRiskLabel(getRiskLabel(summary.getOverallRiskLevel()));
        summary.setDisclaimer(DISCLAIMER);

        return summary;
    }

    @Override
    public RiskDetailVO getRiskDetail(Long enterpriseId) {
        EnterpriseInfo enterprise = enterpriseInfoMapper.selectById(enterpriseId);
        if (enterprise == null) return null;

        RiskDetailVO detail = new RiskDetailVO();
        detail.setEnterpriseId(enterpriseId);
        detail.setEnterpriseName(enterprise.getEnterpriseName());
        detail.setSummary(getRiskSummary(enterpriseId));
        detail.setDisclaimer(DISCLAIMER);

        List<RiskEvent> events = riskEventMapper.selectByEnterpriseId(enterpriseId);

        detail.setSelfRiskEvents(events.stream()
                .filter(e -> !Integer.valueOf(1).equals(e.getIsRelatedParty()))
                .map(this::toEventItem)
                .collect(Collectors.toList()));

        detail.setRelatedPartyRiskEvents(events.stream()
                .filter(e -> Integer.valueOf(1).equals(e.getIsRelatedParty()))
                .map(this::toEventItem)
                .collect(Collectors.toList()));

        List<RelatedPartyInfo> parties = relatedPartyInfoMapper.selectList(
                new LambdaQueryWrapper<RelatedPartyInfo>()
                        .eq(RelatedPartyInfo::getEnterpriseId, enterpriseId));
        detail.setRelatedParties(parties.stream()
                .map(this::toPartyItem)
                .collect(Collectors.toList()));

        return detail;
    }

    @Override
    public void refreshRiskInfo(Long enterpriseId) {
        log.info("刷新企业风险信息 (企查查API对接预留), enterpriseId={}", enterpriseId);
        // TODO: 对接企查查API拉取最新风险数据
        // 1. 调用企查查API获取企业自身风险
        // 2. 获取关联方(股东/法人/实控人)信息
        // 3. 获取关联方风险信息
        // 4. 更新risk_event表
        // 5. 更新related_party_info表
        // 6. 重新计算并更新risk_snapshot

        updateRiskSnapshot(enterpriseId);
    }

    @Override
    public void batchRefreshAllRisks() {
        List<Long> allIds = enterpriseInfoMapper.selectAllIds();
        log.info("批量刷新风险信息, 企业总数={}", allIds.size());
        for (Long id : allIds) {
            try {
                refreshRiskInfo(id);
            } catch (Exception e) {
                log.error("刷新企业 {} 风险信息失败", id, e);
            }
        }
    }

    private void updateRiskSnapshot(Long enterpriseId) {
        int highCount = riskEventMapper.countByEnterpriseAndLevel(enterpriseId, RiskLevel.HIGH.name());
        int mediumCount = riskEventMapper.countByEnterpriseAndLevel(enterpriseId, RiskLevel.MEDIUM.name());
        int lowCount = riskEventMapper.countByEnterpriseAndLevel(enterpriseId, RiskLevel.LOW.name());

        List<RiskEvent> events = riskEventMapper.selectByEnterpriseId(enterpriseId);
        int selfCount = (int) events.stream().filter(e -> !Integer.valueOf(1).equals(e.getIsRelatedParty())).count();
        int relatedCount = (int) events.stream().filter(e -> Integer.valueOf(1).equals(e.getIsRelatedParty())).count();

        String overallLevel;
        if (highCount > 0) overallLevel = RiskLevel.HIGH.name();
        else if (mediumCount > 0) overallLevel = RiskLevel.MEDIUM.name();
        else if (lowCount > 0) overallLevel = RiskLevel.LOW.name();
        else overallLevel = RiskLevel.NONE.name();

        RiskSnapshot snapshot = new RiskSnapshot();
        snapshot.setEnterpriseId(enterpriseId);
        snapshot.setSnapshotDate(LocalDate.now());
        snapshot.setOverallRiskLevel(overallLevel);
        snapshot.setHighRiskCount(highCount);
        snapshot.setMediumRiskCount(mediumCount);
        snapshot.setLowRiskCount(lowCount);
        snapshot.setSelfRiskCount(selfCount);
        snapshot.setRelatedPartyRiskCount(relatedCount);

        riskSnapshotMapper.insert(snapshot);
    }

    private RiskDetailVO.RiskEventItem toEventItem(RiskEvent event) {
        RiskDetailVO.RiskEventItem item = new RiskDetailVO.RiskEventItem();
        item.setEventId(event.getId());
        item.setRiskCategory(event.getRiskCategory());
        item.setRiskCategoryDesc(getCategoryDesc(event.getRiskCategory()));
        item.setRiskLevel(event.getRiskLevel());
        item.setRiskLevelLabel(getRiskLabel(event.getRiskLevel()));
        item.setEventTitle(event.getEventTitle());
        item.setEventDetail(event.getEventDetail());
        item.setInvolvedAmount(event.getInvolvedAmount());
        item.setEventDate(event.getEventDate());
        item.setEventStatus(event.getEventStatus());
        item.setRelatedPartyName(event.getRelatedPartyName());
        return item;
    }

    private RiskDetailVO.RelatedPartyItem toPartyItem(RelatedPartyInfo party) {
        RiskDetailVO.RelatedPartyItem item = new RiskDetailVO.RelatedPartyItem();
        item.setPartyId(party.getId());
        item.setPartyName(party.getPartyName());
        item.setRelationType(party.getRelationType());
        item.setRelationTypeDesc(getRelationTypeDesc(party.getRelationType()));
        item.setShareholdingRatio(party.getShareholdingRatio());
        item.setRelationLevel(party.getRelationLevel());
        item.setHasRisk(Integer.valueOf(1).equals(party.getHasRisk()));
        return item;
    }

    private String getRiskLabel(String level) {
        if (level == null) return RiskLevel.NONE.getLabel();
        try {
            return RiskLevel.valueOf(level).getLabel();
        } catch (IllegalArgumentException e) {
            return level;
        }
    }

    private String getCategoryDesc(String category) {
        if (category == null) return "";
        try {
            return RiskCategory.valueOf(category).getDescription();
        } catch (IllegalArgumentException e) {
            return category;
        }
    }

    private String getRelationTypeDesc(String type) {
        return switch (type != null ? type : "") {
            case "SHAREHOLDER" -> "股东";
            case "LEGAL_PERSON" -> "法定代表人";
            case "CONTROLLER" -> "实际控制人";
            case "GUARANTOR" -> "担保方";
            default -> type;
        };
    }
}
