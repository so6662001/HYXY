package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.EnterpriseInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface EnterpriseInfoMapper extends BaseMapper<EnterpriseInfo> {

    @Select("SELECT id FROM enterprise_info WHERE role IN (#{role}, 3) AND deleted = 0")
    List<Long> selectIdsByRole(@Param("role") int role);

    @Select("SELECT id FROM enterprise_info WHERE deleted = 0")
    List<Long> selectAllIds();
}
