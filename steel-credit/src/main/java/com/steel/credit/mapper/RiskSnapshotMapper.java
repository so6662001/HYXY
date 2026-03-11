package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.RiskSnapshot;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RiskSnapshotMapper extends BaseMapper<RiskSnapshot> {

    @Select("SELECT * FROM risk_snapshot WHERE enterprise_id = #{enterpriseId} AND deleted = 0 ORDER BY snapshot_date DESC LIMIT 1")
    RiskSnapshot selectLatestByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
}
