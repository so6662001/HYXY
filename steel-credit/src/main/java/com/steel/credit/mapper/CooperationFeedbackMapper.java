package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.CooperationFeedback;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CooperationFeedbackMapper extends BaseMapper<CooperationFeedback> {

    @Select("SELECT * FROM cooperation_feedback WHERE target_enterprise_id = #{targetId} AND evaluator_role = #{role} AND valid = 1 AND deleted = 0")
    List<CooperationFeedback> selectValidByTargetAndRole(@Param("targetId") Long targetId, @Param("role") String role);

    @Select("SELECT COUNT(*) FROM cooperation_feedback WHERE target_enterprise_id = #{targetId} AND evaluator_role = #{role} AND valid = 1 AND deleted = 0")
    int countValidByTargetAndRole(@Param("targetId") Long targetId, @Param("role") String role);

    @Select("SELECT COUNT(*) FROM cooperation_feedback WHERE evaluator_enterprise_id = #{evaluatorId} AND target_enterprise_id = #{targetId} AND deleted = 0")
    int countByEvaluatorAndTarget(@Param("evaluatorId") Long evaluatorId, @Param("targetId") Long targetId);
}
