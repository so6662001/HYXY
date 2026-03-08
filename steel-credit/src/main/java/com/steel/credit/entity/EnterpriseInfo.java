package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 企业基本信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enterprise_info")
public class EnterpriseInfo extends BaseEntity {

    /** 企业名称 */
    private String enterpriseName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 企业角色: 1=买家 2=卖家 3=双角色 */
    private Integer role;

    /** 主营品类 */
    private String mainProducts;

    /** 所在地区 */
    private String region;

    /** 入驻日期 */
    private LocalDate joinDate;

    /** 营业执照认证: 0=未认证 1=已认证 */
    private Integer licenseVerified;

    /** 行业资质认证: 0=未认证 1=已认证 */
    private Integer qualificationVerified;

    /** 实地认证: 0=未认证 1=已认证 */
    private Integer fieldVerified;

    /** 企业简介 */
    private String introduction;

    /** 联系方式 */
    private String contactInfo;

    /** 经营范围 */
    private String businessScope;

    /** 企业信息完整度 (0-100) */
    private Integer profileCompleteness;

    /** 是否已接入ERP: 0=否 1=是 */
    private Integer erpConnected;
}
