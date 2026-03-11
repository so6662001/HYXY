package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 询报价行为统计表 — 按月汇总
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inquiry_quote_stat")
public class InquiryQuoteStat extends BaseEntity {

    /** 企业ID */
    private Long enterpriseId;

    /** 统计月份 (yyyy-MM) */
    private String statMonth;

    /** 发出询价数 */
    private Integer inquirySentCount;

    /** 收到询价数 */
    private Integer inquiryReceivedCount;

    /** 发出报价数 */
    private Integer quoteSentCount;

    /** 收到报价数 */
    private Integer quoteReceivedCount;

    /** 询价响应数 (收到询价后做了报价的次数) */
    private Integer inquiryRespondedCount;

    /** 平均响应时长 (小时) */
    private BigDecimal avgResponseHours;

    /** 报价偏离市场均价比例 (百分比) */
    private BigDecimal quotePriceDeviationPct;

    /** 询报价往来的不同企业数 */
    private Integer distinctPartnerCount;

    /** 库存更新次数 */
    private Integer inventoryUpdateCount;
}
