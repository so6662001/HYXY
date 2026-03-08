package com.steel.credit.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 询报价列表/报价详情中的信用角标VO — 精简版
 */
@Data
public class InquiryCreditBadgeVO implements Serializable {

    private Long enterpriseId;
    private String enterpriseName;

    /** 买家信用等级 (卖家视角使用) */
    private String buyerGrade;
    /** 买家数据充分度 */
    private Integer buyerDataSufficiency;

    /** 卖家信用等级 (买家视角使用) */
    private String sellerGrade;
    /** 卖家数据充分度 */
    private Integer sellerDataSufficiency;

    /** 风险预警等级 */
    private String riskLevel;

    /** 买家欠款标记 */
    private String overdueTag;

    /** 是否有严重超期 */
    private Boolean hasSevereOverdue;
}
