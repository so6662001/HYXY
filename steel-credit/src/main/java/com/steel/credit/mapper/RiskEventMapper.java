package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.RiskEvent;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RiskEventMapper extends BaseMapper<RiskEvent> {

    @Select("SELECT * FROM risk_event WHERE enterprise_id = #{enterpriseId} AND deleted = 0 ORDER BY event_date DESC")
    List<RiskEvent> selectByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    @Select("SELECT COUNT(*) FROM risk_event WHERE enterprise_id = #{enterpriseId} AND risk_level = #{level} AND deleted = 0")
    int countByEnterpriseAndLevel(@Param("enterpriseId") Long enterpriseId, @Param("level") String level);
}
