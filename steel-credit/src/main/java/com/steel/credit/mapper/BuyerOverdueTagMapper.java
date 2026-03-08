package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.BuyerOverdueTag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BuyerOverdueTagMapper extends BaseMapper<BuyerOverdueTag> {

    @Select("SELECT * FROM buyer_overdue_tag WHERE buyer_enterprise_id = #{buyerId} AND active_tag = 1 AND deleted = 0 LIMIT 1")
    BuyerOverdueTag selectActiveByBuyer(@Param("buyerId") Long buyerId);
}
