package com.steel.credit.service.impl;

import com.steel.credit.dto.request.FeedbackSubmitRequest;
import com.steel.credit.entity.CooperationFeedback;
import com.steel.credit.entity.EnterpriseInfo;
import com.steel.credit.entity.EnterpriseRelation;
import com.steel.credit.mapper.CooperationFeedbackMapper;
import com.steel.credit.mapper.EnterpriseInfoMapper;
import com.steel.credit.mapper.EnterpriseRelationMapper;
import com.steel.credit.service.CooperationFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CooperationFeedbackServiceImpl implements CooperationFeedbackService {

    private final CooperationFeedbackMapper cooperationFeedbackMapper;
    private final EnterpriseRelationMapper enterpriseRelationMapper;
    private final EnterpriseInfoMapper enterpriseInfoMapper;

    @Override
    public void submitFeedback(Long evaluatorEnterpriseId, FeedbackSubmitRequest request) {
        if (!canEvaluate(evaluatorEnterpriseId, request.getTargetEnterpriseId())) {
            throw new IllegalArgumentException("您与该企业无询报价往来记录，无法进行评价");
        }

        int existingCount = cooperationFeedbackMapper.countByEvaluatorAndTarget(
                evaluatorEnterpriseId, request.getTargetEnterpriseId());
        if (existingCount >= 4) {
            throw new IllegalArgumentException("您对该企业的评价次数已达上限");
        }

        EnterpriseInfo evaluator = enterpriseInfoMapper.selectById(evaluatorEnterpriseId);
        EnterpriseInfo target = enterpriseInfoMapper.selectById(request.getTargetEnterpriseId());
        if (evaluator == null || target == null) {
            throw new IllegalArgumentException("企业信息不存在");
        }

        String evaluatorRole = determineEvaluatorRole(evaluator, target);

        CooperationFeedback feedback = new CooperationFeedback();
        feedback.setEvaluatorEnterpriseId(evaluatorEnterpriseId);
        feedback.setTargetEnterpriseId(request.getTargetEnterpriseId());
        feedback.setEvaluatorRole(evaluatorRole);
        feedback.setCooperationLevel(request.getCooperationLevel());
        feedback.setOverallImpression(request.getOverallImpression());
        feedback.setPaymentRating(request.getPaymentRating());
        feedback.setPickupRating(request.getPickupRating());
        feedback.setOrderStabilityRating(request.getOrderStabilityRating());
        feedback.setDeliveryRating(request.getDeliveryRating());
        feedback.setQualityRating(request.getQualityRating());
        feedback.setPricingRating(request.getPricingRating());
        feedback.setCommunicationRating(request.getCommunicationRating());
        feedback.setWillContinue(request.getWillContinue());
        feedback.setComment(request.getComment());
        feedback.setTriggerScene(request.getTriggerScene());
        feedback.setValid(1);

        double credibilityWeight = calcCredibilityWeight(evaluatorEnterpriseId, request.getTargetEnterpriseId());
        feedback.setCredibilityWeight(credibilityWeight);

        cooperationFeedbackMapper.insert(feedback);
        log.info("合作评价提交成功: evaluator={}, target={}, role={}",
                evaluatorEnterpriseId, request.getTargetEnterpriseId(), evaluatorRole);
    }

    @Override
    public List<CooperationFeedback> getFeedbacksByTarget(Long targetEnterpriseId, String evaluatorRole) {
        return cooperationFeedbackMapper.selectValidByTargetAndRole(targetEnterpriseId, evaluatorRole);
    }

    @Override
    public boolean canEvaluate(Long evaluatorId, Long targetId) {
        List<EnterpriseRelation> relations = enterpriseRelationMapper.selectByEnterpriseId(evaluatorId);
        return relations.stream().anyMatch(r ->
                (r.getEnterpriseAId().equals(targetId) || r.getEnterpriseBId().equals(targetId)));
    }

    /**
     * 判断评价方角色: 卖家评买家 / 买家评卖家
     */
    private String determineEvaluatorRole(EnterpriseInfo evaluator, EnterpriseInfo target) {
        int evaluatorRole = evaluator.getRole() != null ? evaluator.getRole() : 3;
        int targetRole = target.getRole() != null ? target.getRole() : 3;

        if (evaluatorRole == 2 || (evaluatorRole == 3 && targetRole == 1)) {
            return "SELLER_RATE_BUYER";
        }
        return "BUYER_RATE_SELLER";
    }

    /**
     * 计算评价可信度权重 — 询报价往来次数越多权重越高
     */
    private double calcCredibilityWeight(Long evaluatorId, Long targetId) {
        List<EnterpriseRelation> relations = enterpriseRelationMapper.selectByEnterpriseId(evaluatorId);
        int interactionCount = relations.stream()
                .filter(r -> r.getEnterpriseAId().equals(targetId) || r.getEnterpriseBId().equals(targetId))
                .mapToInt(r -> r.getTotalInteractionCount() != null ? r.getTotalInteractionCount() : 0)
                .sum();

        if (interactionCount >= 20) return 1.0;
        if (interactionCount >= 10) return 0.8;
        if (interactionCount >= 5) return 0.6;
        if (interactionCount >= 2) return 0.4;
        return 0.2;
    }
}
