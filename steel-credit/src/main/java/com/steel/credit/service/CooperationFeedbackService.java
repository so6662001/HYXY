package com.steel.credit.service;

import com.steel.credit.dto.request.FeedbackSubmitRequest;
import com.steel.credit.entity.CooperationFeedback;

import java.util.List;

public interface CooperationFeedbackService {

    /**
     * 提交合作印象评价
     */
    void submitFeedback(Long evaluatorEnterpriseId, FeedbackSubmitRequest request);

    /**
     * 获取某企业收到的评价列表
     */
    List<CooperationFeedback> getFeedbacksByTarget(Long targetEnterpriseId, String evaluatorRole);

    /**
     * 检查是否可以评价（必须有询报价往来）
     */
    boolean canEvaluate(Long evaluatorId, Long targetId);
}
