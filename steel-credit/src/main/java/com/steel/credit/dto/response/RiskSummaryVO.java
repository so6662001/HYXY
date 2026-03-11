package com.steel.credit.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 风险预警概况VO
 */
@Data
public class RiskSummaryVO implements Serializable {

    private Long enterpriseId;
    private String overallRiskLevel;
    private String overallRiskLabel;
    private Integer highRiskCount;
    private Integer mediumRiskCount;
    private Integer lowRiskCount;
    private Integer totalRiskCount;
    private Integer selfRiskCount;
    private Integer relatedPartyRiskCount;
    private LocalDate lastCheckDate;
    private String disclaimer;
}
