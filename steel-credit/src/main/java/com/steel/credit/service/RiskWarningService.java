package com.steel.credit.service;

import com.steel.credit.dto.response.RiskDetailVO;
import com.steel.credit.dto.response.RiskSummaryVO;

public interface RiskWarningService {

    /**
     * 获取企业风险预警概况
     */
    RiskSummaryVO getRiskSummary(Long enterpriseId);

    /**
     * 获取企业风险预警详情
     */
    RiskDetailVO getRiskDetail(Long enterpriseId);

    /**
     * 刷新企业风险信息（从企查查拉取）
     */
    void refreshRiskInfo(Long enterpriseId);

    /**
     * 批量刷新所有企业风险信息
     */
    void batchRefreshAllRisks();
}
