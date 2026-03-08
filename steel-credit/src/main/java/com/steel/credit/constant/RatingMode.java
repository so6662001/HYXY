package com.steel.credit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评级模式
 */
@Getter
@AllArgsConstructor
public enum RatingMode {

    FULL("FULL", "全量数据模式", "平台行为+ERP+合作反馈"),
    PLATFORM_FEEDBACK("PLATFORM_FEEDBACK", "平台+反馈模式", "平台行为+合作反馈"),
    BASIC("BASIC", "基础模式", "仅平台行为数据");

    private final String code;
    private final String name;
    private final String description;
}
