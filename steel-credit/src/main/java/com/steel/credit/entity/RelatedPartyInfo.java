package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 关联方信息表 — 企业的股东、法人、实控人等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("related_party_info")
public class RelatedPartyInfo extends BaseEntity {

    /** 企业ID */
    private Long enterpriseId;

    /** 关联方名称 */
    private String partyName;

    /** 关联方统一社会信用代码 (企业类型) */
    private String partyCreditCode;

    /** 关联类型: SHAREHOLDER/LEGAL_PERSON/CONTROLLER/GUARANTOR */
    private String relationType;

    /** 持股比例 */
    private BigDecimal shareholdingRatio;

    /** 关联层级: 1=直接关联 2=间接关联 */
    private Integer relationLevel;

    /** 关联方是否存在风险: 0=否 1=是 */
    private Integer hasRisk;

    /** 数据来源 */
    private String dataSource;
}
