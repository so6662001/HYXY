package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.ErpOverdueSummary;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ErpOverdueSummaryMapper extends BaseMapper<ErpOverdueSummary> {

    @Select("SELECT * FROM erp_overdue_summary WHERE buyer_enterprise_id = #{buyerId} AND snapshot_date = (SELECT MAX(snapshot_date) FROM erp_overdue_summary WHERE buyer_enterprise_id = #{buyerId} AND deleted = 0) AND deleted = 0")
    List<ErpOverdueSummary> selectLatestByBuyer(@Param("buyerId") Long buyerId);
}
