package com.steel.credit.controller;

import com.steel.credit.dto.request.FeedbackSubmitRequest;
import com.steel.credit.dto.response.CooperationFeedbackVO;
import com.steel.credit.dto.response.Result;
import com.steel.credit.service.CooperationFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "合作印象评价", description = "买卖双方的合作印象评价（角色化评价维度）")
@Validated
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class CooperationFeedbackController {

    private final CooperationFeedbackService cooperationFeedbackService;

    @Operation(summary = "提交合作印象评价")
    @PostMapping("/submit/{evaluatorEnterpriseId}")
    public Result<Void> submitFeedback(
            @Parameter(description = "评价方企业ID") @PathVariable Long evaluatorEnterpriseId,
            @Valid @RequestBody FeedbackSubmitRequest request) {
        cooperationFeedbackService.submitFeedback(evaluatorEnterpriseId, request);
        return Result.ok();
    }

    @Operation(summary = "获取某企业收到的评价列表")
    @GetMapping("/list/{targetEnterpriseId}")
    public Result<List<CooperationFeedbackVO>> getFeedbacks(
            @Parameter(description = "被评方企业ID") @PathVariable Long targetEnterpriseId,
            @Parameter(description = "评价方角色: SELLER_RATE_BUYER / BUYER_RATE_SELLER")
            @RequestParam @Pattern(regexp = "SELLER_RATE_BUYER|BUYER_RATE_SELLER", message = "evaluatorRole必须为SELLER_RATE_BUYER或BUYER_RATE_SELLER") String evaluatorRole) {
        return Result.ok(cooperationFeedbackService.getFeedbacksByTarget(targetEnterpriseId, evaluatorRole));
    }

    @Operation(summary = "检查是否可以评价")
    @GetMapping("/can-evaluate")
    public Result<Boolean> canEvaluate(
            @RequestParam Long evaluatorId,
            @RequestParam Long targetId) {
        return Result.ok(cooperationFeedbackService.canEvaluate(evaluatorId, targetId));
    }
}
