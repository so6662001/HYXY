package com.steel.credit.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 合作印象评价提交请求
 */
@Data
public class FeedbackSubmitRequest {

    @NotNull(message = "被评方企业ID不能为空")
    private Long targetEnterpriseId;

    @NotNull(message = "合作次数档位不能为空")
    @Min(1) @Max(4)
    private Integer cooperationLevel;

    @NotNull(message = "整体印象不能为空")
    @Min(1) @Max(4)
    private Integer overallImpression;

    // 卖家评买家
    @Min(1) @Max(4)
    private Integer paymentRating;
    @Min(1) @Max(4)
    private Integer pickupRating;
    @Min(1) @Max(4)
    private Integer orderStabilityRating;

    // 买家评卖家
    @Min(1) @Max(4)
    private Integer deliveryRating;
    @Min(1) @Max(4)
    private Integer qualityRating;
    @Min(1) @Max(4)
    private Integer pricingRating;

    // 通用
    @Min(1) @Max(4)
    private Integer communicationRating;

    @Min(1) @Max(3)
    private Integer willContinue;

    private String comment;

    private String triggerScene;
}
