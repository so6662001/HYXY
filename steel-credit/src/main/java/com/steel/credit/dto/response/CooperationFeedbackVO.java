package com.steel.credit.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 合作评价展示VO — 仅包含可公开字段，不暴露内部信息
 */
@Data
public class CooperationFeedbackVO implements Serializable {

    private String evaluatorRole;
    private Integer cooperationLevel;
    private Integer overallImpression;
    private Integer paymentRating;
    private Integer pickupRating;
    private Integer orderStabilityRating;
    private Integer deliveryRating;
    private Integer qualityRating;
    private Integer pricingRating;
    private Integer communicationRating;
    private Integer willContinue;
    private String comment;
    private String createTime;
}
