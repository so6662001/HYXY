package com.steel.credit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据充分度等级
 */
@Getter
@AllArgsConstructor
public enum DataSufficiency {

    FULL(5, "充分", "该评级基于ERP系统数据与平台交易数据综合评定，数据全面可信"),
    MOSTLY(4, "较充分", "该评级已纳入部分ERP数据与平台交易数据"),
    MEDIUM(3, "中等", "该评级基于平台数据与合作方评价评定，暂未接入ERP数据"),
    LIMITED(2, "较少", "该企业交易记录有限，评级仅供初步参考"),
    MINIMAL(1, "有限", "该企业交易数据不足，评级基于基础信息评定");

    private final int level;
    private final String label;
    private final String description;
}
