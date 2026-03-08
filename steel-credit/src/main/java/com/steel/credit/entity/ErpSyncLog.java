package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * ERP数据同步日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("erp_sync_log")
public class ErpSyncLog extends BaseEntity {

    /** 同步批次号 */
    private String syncBatchNo;

    /** 商家企业ID */
    private Long sellerEnterpriseId;

    /** 同步时间 */
    private LocalDateTime syncTime;

    /** 数据类型: ORDER/PAYMENT/OVERDUE */
    private String dataType;

    /** 同步记录数 */
    private Integer recordCount;

    /** 同步状态: SUCCESS/FAILED/PARTIAL */
    private String syncStatus;

    /** 异常信息 */
    private String errorMessage;
}
