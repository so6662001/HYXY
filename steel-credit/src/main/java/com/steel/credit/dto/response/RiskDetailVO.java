package com.steel.credit.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 风险预警详情VO
 */
@Data
public class RiskDetailVO implements Serializable {

    private Long enterpriseId;
    private String enterpriseName;
    private RiskSummaryVO summary;

    /** 企业自身风险事件列表 */
    private List<RiskEventItem> selfRiskEvents;

    /** 关联方风险事件列表 */
    private List<RiskEventItem> relatedPartyRiskEvents;

    /** 关联方信息 */
    private List<RelatedPartyItem> relatedParties;

    private String disclaimer;

    @Data
    public static class RiskEventItem implements Serializable {
        private Long eventId;
        private String riskCategory;
        private String riskCategoryDesc;
        private String riskLevel;
        private String riskLevelLabel;
        private String eventTitle;
        private String eventDetail;
        private BigDecimal involvedAmount;
        private LocalDate eventDate;
        private String eventStatus;
        private String relatedPartyName;
    }

    @Data
    public static class RelatedPartyItem implements Serializable {
        private Long partyId;
        private String partyName;
        private String relationType;
        private String relationTypeDesc;
        private BigDecimal shareholdingRatio;
        private Integer relationLevel;
        private Boolean hasRisk;
        private Integer riskCount;
    }
}
