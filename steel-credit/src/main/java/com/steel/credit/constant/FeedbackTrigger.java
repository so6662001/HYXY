package com.steel.credit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 合作反馈触发场景
 */
@Getter
@AllArgsConstructor
public enum FeedbackTrigger {

    RE_INQUIRY("RE_INQUIRY", "再次询价时触发"),
    VIEW_CREDIT("VIEW_CREDIT", "查看信用页时触发"),
    PERIODIC("PERIODIC", "季度回访推送"),
    INVITATION("INVITATION", "企业主动邀请");

    private final String code;
    private final String description;
}
