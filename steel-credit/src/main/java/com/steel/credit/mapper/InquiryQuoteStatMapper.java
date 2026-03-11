package com.steel.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.credit.entity.InquiryQuoteStat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface InquiryQuoteStatMapper extends BaseMapper<InquiryQuoteStat> {

    @Select("SELECT * FROM inquiry_quote_stat WHERE enterprise_id = #{enterpriseId} AND stat_month >= #{startMonth} AND deleted = 0 ORDER BY stat_month")
    List<InquiryQuoteStat> selectByEnterpriseAndMonthRange(@Param("enterpriseId") Long enterpriseId, @Param("startMonth") String startMonth);

    @Select("SELECT AVG(inquiry_sent_count + quote_sent_count) FROM inquiry_quote_stat WHERE stat_month >= #{startMonth} AND deleted = 0")
    Double selectAvgActivityAllEnterprises(@Param("startMonth") String startMonth);
}
