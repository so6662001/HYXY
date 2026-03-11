package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.ErpOrderRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ErpOrderRecordMapper extends BaseMapper<ErpOrderRecord> {

    @Select("SELECT * FROM erp_order_record WHERE buyer_enterprise_id = #{buyerId} AND order_date >= #{startDate} AND deleted = 0")
    List<ErpOrderRecord> selectByBuyerAndDateRange(@Param("buyerId") Long buyerId, @Param("startDate") String startDate);

    @Select("SELECT * FROM erp_order_record WHERE seller_enterprise_id = #{sellerId} AND order_date >= #{startDate} AND deleted = 0")
    List<ErpOrderRecord> selectBySellerAndDateRange(@Param("sellerId") Long sellerId, @Param("startDate") String startDate);

    @Select("SELECT COUNT(DISTINCT seller_enterprise_id) FROM erp_order_record WHERE buyer_enterprise_id = #{buyerId} AND deleted = 0")
    int countDistinctSellersByBuyer(@Param("buyerId") Long buyerId);

    @Select("SELECT COUNT(*) FROM erp_order_record WHERE buyer_enterprise_id = #{buyerId} AND deleted = 0")
    int countByBuyer(@Param("buyerId") Long buyerId);

    @Select("SELECT COUNT(*) FROM erp_order_record WHERE erp_order_no = #{erpOrderNo} AND seller_enterprise_id = #{sellerId} AND deleted = 0")
    int countByOrderNoAndSeller(@Param("erpOrderNo") String erpOrderNo, @Param("sellerId") Long sellerId);

    @Select("SELECT COUNT(DISTINCT buyer_enterprise_id) FROM erp_order_record WHERE seller_enterprise_id = #{sellerId} AND order_date >= #{startDate} AND deleted = 0")
    int countDistinctBuyersBySeller(@Param("sellerId") Long sellerId, @Param("startDate") String startDate);
}
