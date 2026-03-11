package com.steel.credit.controller;

import com.steel.credit.dto.response.*;
import com.steel.credit.service.CreditRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "信用评级", description = "企业信用评级相关接口（与风险预警独立）")
@Validated
@RestController
@RequestMapping("/credit")
@RequiredArgsConstructor
public class CreditRatingController {

    private final CreditRatingService creditRatingService;

    @Operation(summary = "获取企业信用名片", description = "包含买家信用+卖家信用+风险概况，用于企业主页展示")
    @GetMapping("/card/{enterpriseId}")
    public Result<CreditCardVO> getCreditCard(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        return Result.ok(creditRatingService.getCreditCard(enterpriseId));
    }

    @Operation(summary = "获取买家信用详情", description = "包含各维度得分、趋势、评价摘要等完整信息")
    @GetMapping("/buyer/detail/{enterpriseId}")
    public Result<CreditDetailVO> getBuyerCreditDetail(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        return Result.ok(creditRatingService.getBuyerCreditDetail(enterpriseId));
    }

    @Operation(summary = "获取卖家信用详情")
    @GetMapping("/seller/detail/{enterpriseId}")
    public Result<CreditDetailVO> getSellerCreditDetail(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        return Result.ok(creditRatingService.getSellerCreditDetail(enterpriseId));
    }

    @Operation(summary = "批量获取信用角标", description = "用于询报价列表中展示信用等级、风险状态等角标信息")
    @PostMapping("/badges")
    public Result<List<InquiryCreditBadgeVO>> getCreditBadges(
            @RequestBody @Size(max = 100, message = "单次查询不超过100家企业") List<Long> enterpriseIds) {
        return Result.ok(creditRatingService.getCreditBadges(enterpriseIds));
    }

    @Operation(summary = "手动触发单个企业信用评分计算")
    @PostMapping("/calculate/{enterpriseId}")
    public Result<Void> calculateCreditScore(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        creditRatingService.calculateCreditScore(enterpriseId);
        return Result.ok();
    }

    @Operation(summary = "手动触发全量信用评分计算（管理员）")
    @PostMapping("/calculate/all")
    public Result<Void> batchCalculateAll() {
        creditRatingService.batchCalculateAllScores();
        return Result.ok();
    }
}
