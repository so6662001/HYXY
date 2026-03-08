package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 信用评级变更历史
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_score_history")
public class CreditScoreHistory extends BaseEntity {

    /** 企业ID */
    private Long enterpriseId;

    /** 角色类型: BUYER/SELLER */
    private String roleType;

    /** 变更前等级 */
    private String previousGrade;

    /** 变更前得分 */
    private Integer previousScore;

    /** 变更后等级 */
    private String currentGrade;

    /** 变更后得分 */
    private Integer currentScore;

    /** 变更原因描述 */
    private String changeReason;

    /** 评级周期 */
    private String ratingPeriod;
}
