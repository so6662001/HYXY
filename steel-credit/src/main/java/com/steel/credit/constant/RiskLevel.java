package com.steel.credit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 风险预警等级
 */
@Getter
@AllArgsConstructor
public enum RiskLevel {

    HIGH(4, "高危预警", "建议暂停信用交易，改为预付款"),
    MEDIUM(3, "中危预警", "建议加强交易审核，缩短账期"),
    LOW(2, "低危提示", "建议关注，正常交易"),
    NONE(1, "暂无风险", "正常交易");

    private final int level;
    private final String label;
    private final String suggestion;
}
