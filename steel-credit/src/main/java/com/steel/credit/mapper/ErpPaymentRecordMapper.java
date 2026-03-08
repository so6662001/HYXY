package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.ErpPaymentRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ErpPaymentRecordMapper extends BaseMapper<ErpPaymentRecord> {

    @Select("SELECT * FROM erp_payment_record WHERE buyer_enterprise_id = #{buyerId} AND agreed_payment_date >= #{startDate} AND deleted = 0")
    List<ErpPaymentRecord> selectByBuyerAndDateRange(@Param("buyerId") Long buyerId, @Param("startDate") String startDate);

    @Select("SELECT COUNT(DISTINCT seller_enterprise_id) FROM erp_payment_record WHERE buyer_enterprise_id = #{buyerId} AND deleted = 0")
    int countDistinctSellersByBuyer(@Param("buyerId") Long buyerId);
}
