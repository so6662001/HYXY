package com.steel.credit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 欠款行为分级
 */
@Getter
@AllArgsConstructor
public enum OverdueLevel {

    NORMAL(0, "正常", "所有款项均在账期内支付"),
    MILD(1, "轻度超期", "偶有超期，超期≤15天，已结清"),
    MODERATE(2, "中度超期", "多次超期≥3次/年，或超期16-60天"),
    SEVERE(3, "严重超期", "超期>60天，或超期金额>应付30%"),
    MALICIOUS(4, "恶意欠款", "超期>120天无还款计划，或多家投诉");

    private final int level;
    private final String label;
    private final String description;
}
