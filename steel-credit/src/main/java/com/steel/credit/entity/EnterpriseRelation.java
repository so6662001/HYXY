package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 企业关系图谱表 — 记录询报价往来形成的业务关系
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enterprise_relation")
public class EnterpriseRelation extends BaseEntity {

    /** 企业A ID */
    private Long enterpriseAId;

    /** 企业B ID */
    private Long enterpriseBId;

    /** 首次询报价时间 */
    private LocalDateTime firstInteractionTime;

    /** 最近询报价时间 */
    private LocalDateTime lastInteractionTime;

    /** 累计询报价次数 */
    private Integer totalInteractionCount;

    /** 近90天内询报价次数 (用于计算回头率) */
    private Integer recent90dCount;

    /** 关系持续月数 */
    private Integer relationMonths;

    /** 关系活跃状态: 1=活跃 0=沉寂 */
    private Integer activeStatus;
}
