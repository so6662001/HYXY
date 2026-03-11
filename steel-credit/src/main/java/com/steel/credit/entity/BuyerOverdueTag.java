package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 买家欠款标记表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("buyer_overdue_tag")
public class BuyerOverdueTag extends BaseEntity {

    /** 买家企业ID */
    private Long buyerEnterpriseId;

    /** 标记等级: NORMAL/MILD/MODERATE/SEVERE/MALICIOUS */
    private String overdueLevel;

    /** 标记原因 */
    private String tagReason;

    /** 标记时间 */
    private LocalDateTime tagTime;

    /** 是否活跃标记: 1=是(当前生效) 0=否(已解除) */
    private Integer activeTag;

    /** 超期次数 (近12月) */
    private Integer overdueCount;

    /** 最大超期天数 */
    private Integer maxOverdueDays;
}
