package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.CreditScoreHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CreditScoreHistoryMapper extends BaseMapper<CreditScoreHistory> {

    @Select("SELECT * FROM credit_score_history WHERE enterprise_id = #{enterpriseId} AND role_type = #{roleType} AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<CreditScoreHistory> selectByEnterpriseAndRole(@Param("enterpriseId") Long enterpriseId,
                                                       @Param("roleType") String roleType,
                                                       @Param("limit") int limit);
}
