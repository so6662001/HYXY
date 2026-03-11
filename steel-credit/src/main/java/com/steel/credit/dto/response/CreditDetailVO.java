package com.steel.credit.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 信用评级详情VO
 */
@Data
public class CreditDetailVO implements Serializable {

    private Long enterpriseId;
    private String enterpriseName;

    /** 角色: BUYER/SELLER */
    private String roleType;
    private String creditGrade;
    private Integer totalScore;
    private String ratingMode;
    private String ratingModeDesc;
    private Integer dataSufficiency;
    private String dataSufficiencyLabel;
    private String dataSufficiencyDesc;
    private String gradeCeiling;
    private LocalDate ratingDate;

    /** ERP数据来源商家/买家数 */
    private Integer erpSourceCount;
    /** 合作反馈条数 */
    private Integer feedbackCount;

    /** 各维度得分明细 */
    private List<DimensionScore> dimensions;

    /** 信用趋势 (近12月) */
    private List<TrendPoint> trend;

    /** 评级变更历史 */
    private List<GradeChange> gradeHistory;

    /** 合作方评价摘要 (最多展示3条) */
    private List<FeedbackSummary> feedbackSummaries;

    /** 付款信用趋势 (仅买家) */
    private List<TrendPoint> paymentTrend;

    /** 关键缺失数据提示 */
    private List<String> missingDataHints;

    /** 建议 */
    private List<String> suggestions;

    @Data
    public static class DimensionScore implements Serializable {
        private String dimensionCode;
        private String dimensionName;
        private String dataSource;
        private Integer score;
        private Integer maxScore;
        private Integer percentage;
        private String trend;
        private List<IndicatorScore> indicators;
    }

    @Data
    public static class IndicatorScore implements Serializable {
        private String indicatorCode;
        private String indicatorName;
        private Integer score;
        private Integer maxScore;
        private String rawValueDisplay;
    }

    @Data
    public static class TrendPoint implements Serializable {
        private String period;
        private Integer score;
        private String grade;
    }

    @Data
    public static class GradeChange implements Serializable {
        private String period;
        private String fromGrade;
        private String toGrade;
        private Integer fromScore;
        private Integer toScore;
    }

    @Data
    public static class FeedbackSummary implements Serializable {
        private String comment;
        private Integer overallImpression;
        private String evaluatorRegion;
        private String evaluatorType;
    }
}
