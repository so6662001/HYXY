package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 风险预警推送记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("risk_alert_log")
public class RiskAlertLog extends BaseEntity {

    /** 接收方企业ID */
    private Long receiverEnterpriseId;

    /** 被预警企业ID */
    private Long targetEnterpriseId;

    /** 关联的风险事件ID */
    private Long riskEventId;

    /** 推送渠道: SITE/SMS/EMAIL */
    private String channel;

    /** 推送时间 */
    private LocalDateTime alertTime;

    /** 是否已读: 0=否 1=是 */
    private Integer readStatus;

    /** 已读时间 */
    private LocalDateTime readTime;
}
