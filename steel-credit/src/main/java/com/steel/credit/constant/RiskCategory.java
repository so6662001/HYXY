package com.steel.credit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 风险类别
 */
@Getter
@AllArgsConstructor
public enum RiskCategory {

    JUDICIAL("JUDICIAL", "司法风险"),
    OPERATIONAL("OPERATIONAL", "经营风险"),
    FINANCIAL("FINANCIAL", "财务风险"),
    CHANGE("CHANGE", "变更风险");

    private final String code;
    private final String description;
}
