package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.EnterpriseRelation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface EnterpriseRelationMapper extends BaseMapper<EnterpriseRelation> {

    @Select("SELECT * FROM enterprise_relation WHERE (enterprise_a_id = #{enterpriseId} OR enterprise_b_id = #{enterpriseId}) AND deleted = 0")
    List<EnterpriseRelation> selectByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    @Select("SELECT COUNT(*) FROM enterprise_relation WHERE (enterprise_a_id = #{enterpriseId} OR enterprise_b_id = #{enterpriseId}) AND relation_months >= 6 AND active_status = 1 AND deleted = 0")
    int countLongTermPartners(@Param("enterpriseId") Long enterpriseId);

    @Select("SELECT COUNT(*) FROM enterprise_relation WHERE (enterprise_a_id = #{enterpriseId} OR enterprise_b_id = #{enterpriseId}) AND recent_90d_count >= 2 AND deleted = 0")
    int countRepeatPartners(@Param("enterpriseId") Long enterpriseId);

    @Select("SELECT COUNT(*) FROM enterprise_relation WHERE (enterprise_a_id = #{enterpriseId} OR enterprise_b_id = #{enterpriseId}) AND deleted = 0")
    int countTotalPartners(@Param("enterpriseId") Long enterpriseId);
}
