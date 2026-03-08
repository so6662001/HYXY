package com.steel.credit.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 信用名片VO — 用于企业主页/询报价列表等场景的概要展示
 */
@Data
public class CreditCardVO implements Serializable {

    private Long enterpriseId;
    private String enterpriseName;

    /** 企业角色: BUYER/SELLER/BOTH */
    private String role;

    /** 买家信用 (角色为BUYER或BOTH时有值) */
    private CreditSummary buyerCredit;

    /** 卖家信用 (角色为SELLER或BOTH时有值) */
    private CreditSummary sellerCredit;

    /** 风险预警概况 */
    private RiskSummaryVO riskSummary;

    @Data
    public static class CreditSummary implements Serializable {
        /** 信用等级 */
        private String creditGrade;
        /** 信用得分 */
        private Integer totalScore;
        /** 数据充分度等级 (1-5) */
        private Integer dataSufficiency;
        /** 数据充分度标签 */
        private String dataSufficiencyLabel;
        /** 评级模式 */
        private String ratingMode;
        /** 评级数据来源列表 */
        private List<DataSourceItem> dataSources;
        /** 核心指标 (买家: 付款/订单/提货; 卖家: 报价/供应/评价) */
        private List<CoreIndicator> coreIndicators;
        /** 欠款标记 (仅买家) */
        private String overdueTag;
        /** 欠款标记描述 (仅买家) */
        private String overdueTagDesc;
    }

    @Data
    public static class DataSourceItem implements Serializable {
        private String name;
        private boolean available;
        private String detail;
    }

    @Data
    public static class CoreIndicator implements Serializable {
        private String name;
        private String code;
        private Integer score;
        private Integer maxScore;
        private Integer percentage;
        private String level;
    }
}
