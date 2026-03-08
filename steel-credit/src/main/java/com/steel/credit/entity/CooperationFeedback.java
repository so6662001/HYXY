package com.steel.credit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合作印象评价表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cooperation_feedback")
public class CooperationFeedback extends BaseEntity {

    /** 评价方企业ID */
    private Long evaluatorEnterpriseId;

    /** 被评方企业ID */
    private Long targetEnterpriseId;

    /** 评价方角色: BUYER_RATE_SELLER / SELLER_RATE_BUYER */
    private String evaluatorRole;

    /** 合作次数档位: 1=1-2次, 2=3-5次, 3=5次以上, 4=长期合作 */
    private Integer cooperationLevel;

    /** 整体印象: 4=非常好 3=不错 2=一般 1=较差 */
    private Integer overallImpression;

    // === 卖家评买家的分项 ===
    /** 付款表现: 4=从不拖欠 3=偶有小延迟 2=经常催款 1=严重拖欠 */
    private Integer paymentRating;

    /** 提货表现: 4=按时足量 3=基本按时 2=常推迟 1=不配合 */
    private Integer pickupRating;

    /** 订单稳定性: 4=从不取消 3=偶尔调整 2=常改量 1=常取消 */
    private Integer orderStabilityRating;

    // === 买家评卖家的分项 ===
    /** 交货及时性: 4=准时可靠 3=基本准时 2=常延迟 1=严重延迟 */
    private Integer deliveryRating;

    /** 货物品质: 4=质量稳定 3=基本合格 2=偶有问题 1=常不达标 */
    private Integer qualityRating;

    /** 报价诚信: 4=报实价 3=基本合理 2=偏高 1=虚报价格 */
    private Integer pricingRating;

    // === 通用分项 ===
    /** 沟通配合: 4=顺畅高效 3=一般 2=较被动 1=困难 */
    private Integer communicationRating;

    /** 是否愿意继续合作: 1=是 2=视情况 3=不太愿意 */
    private Integer willContinue;

    /** 一句话评价 */
    private String comment;

    /** 触发场景: RE_INQUIRY/VIEW_CREDIT/PERIODIC/INVITATION */
    private String triggerScene;

    /** 可信度权重 (系统计算，基于双方询报价往来次数等) */
    private Double credibilityWeight;

    /** 是否有效: 1=有效 0=无效(被标记异常) */
    private Integer valid;
}
