package com.steel.credit.controller;

import com.steel.credit.dto.response.Result;
import com.steel.credit.dto.response.RiskDetailVO;
import com.steel.credit.dto.response.RiskSummaryVO;
import com.steel.credit.service.RiskWarningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "风险预警", description = "企业风险预警相关接口（与信用评级独立，基于公开信息）")
@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
public class RiskWarningController {

    private final RiskWarningService riskWarningService;

    @Operation(summary = "获取企业风险预警概况")
    @GetMapping("/summary/{enterpriseId}")
    public Result<RiskSummaryVO> getRiskSummary(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        return Result.ok(riskWarningService.getRiskSummary(enterpriseId));
    }

    @Operation(summary = "获取企业风险预警详情", description = "包含企业自身风险、关联方风险、关联方图谱信息")
    @GetMapping("/detail/{enterpriseId}")
    public Result<RiskDetailVO> getRiskDetail(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        return Result.ok(riskWarningService.getRiskDetail(enterpriseId));
    }

    @Operation(summary = "手动刷新企业风险信息", description = "从企查查重新拉取最新风险数据")
    @PostMapping("/refresh/{enterpriseId}")
    public Result<Void> refreshRiskInfo(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        riskWarningService.refreshRiskInfo(enterpriseId);
        return Result.ok();
    }
}
