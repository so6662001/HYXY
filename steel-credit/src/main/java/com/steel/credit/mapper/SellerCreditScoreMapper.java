package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.SellerCreditScore;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SellerCreditScoreMapper extends BaseMapper<SellerCreditScore> {

    @Select("SELECT * FROM seller_credit_score WHERE enterprise_id = #{enterpriseId} AND deleted = 0 ORDER BY rating_date DESC LIMIT 1")
    SellerCreditScore selectLatestByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    @Select("SELECT * FROM seller_credit_score WHERE enterprise_id = #{enterpriseId} AND deleted = 0 ORDER BY rating_date DESC LIMIT #{limit}")
    List<SellerCreditScore> selectHistoryByEnterpriseId(@Param("enterpriseId") Long enterpriseId, @Param("limit") int limit);
}
