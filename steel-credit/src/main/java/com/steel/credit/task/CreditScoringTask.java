package com.steel.credit.task;

import com.steel.credit.service.CreditRatingService;
import com.steel.credit.service.ErpDataSyncService;
import com.steel.credit.service.RiskWarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 信用评分与风险预警定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditScoringTask {

    private final CreditRatingService creditRatingService;
    private final RiskWarningService riskWarningService;
    private final ErpDataSyncService erpDataSyncService;

    /**
     * 每日凌晨1:30 聚合ERP欠款汇总快照（需在信用评分之前执行）
     */
    @Scheduled(cron = "0 30 1 * * ?")
    public void dailyOverdueAggregation() {
        log.info("========== 定时任务: 开始聚合欠款汇总快照 ==========");
        long start = System.currentTimeMillis();

        try {
            erpDataSyncService.batchAggregateOverdueSummaries();
        } catch (Exception e) {
            log.error("欠款汇总聚合异常", e);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("========== 定时任务: 欠款汇总聚合完成, 耗时{}ms ==========", elapsed);
    }

    /**
     * 每日凌晨2:00 批量计算所有企业的信用评分
     */
    @Scheduled(cron = "${credit.scoring.cron:0 0 2 * * ?}")
    public void dailyCreditScoring() {
        log.info("========== 定时任务: 开始批量计算信用评分 ==========");
        long start = System.currentTimeMillis();

        try {
            creditRatingService.batchCalculateAllScores();
        } catch (Exception e) {
            log.error("批量信用评分计算异常", e);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("========== 定时任务: 信用评分计算完成, 耗时{}ms ==========", elapsed);
    }

    /**
     * 每日凌晨3:00 批量刷新企业风险信息
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void dailyRiskRefresh() {
        log.info("========== 定时任务: 开始批量刷新风险信息 ==========");
        long start = System.currentTimeMillis();

        try {
            riskWarningService.batchRefreshAllRisks();
        } catch (Exception e) {
            log.error("批量风险信息刷新异常", e);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("========== 定时任务: 风险信息刷新完成, 耗时{}ms ==========", elapsed);
    }
}
