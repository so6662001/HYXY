package com.steel.credit.service;

import com.steel.credit.dto.response.CreditCardVO;
import com.steel.credit.dto.response.CreditDetailVO;
import com.steel.credit.dto.response.InquiryCreditBadgeVO;

import java.util.List;

public interface CreditRatingService {

    /**
     * 获取企业信用名片（买家信用+卖家信用+风险概况）
     */
    CreditCardVO getCreditCard(Long enterpriseId);

    /**
     * 获取买家信用详情
     */
    CreditDetailVO getBuyerCreditDetail(Long enterpriseId);

    /**
     * 获取卖家信用详情
     */
    CreditDetailVO getSellerCreditDetail(Long enterpriseId);

    /**
     * 获取询报价列表中的信用角标
     */
    List<InquiryCreditBadgeVO> getCreditBadges(List<Long> enterpriseIds);

    /**
     * 计算单个企业的信用评分（买家+卖家）
     */
    void calculateCreditScore(Long enterpriseId);

    /**
     * 批量计算所有企业的信用评分
     */
    void batchCalculateAllScores();
}
