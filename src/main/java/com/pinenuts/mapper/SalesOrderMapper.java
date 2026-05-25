package com.pinenuts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinenuts.dto.report.DailyRevenueResponse;
import com.pinenuts.dto.report.DishSalesTopResponse;
import com.pinenuts.dto.report.StoreRankingResponse;
import com.pinenuts.entity.SalesOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {
    List<DailyRevenueResponse> getDailyRevenue(@Param("tenantId") Long tenantId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("storeId") Long storeId);

    List<StoreRankingResponse> getStoreRanking(@Param("tenantId") Long tenantId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("topN") Integer topN);

    List<DishSalesTopResponse> getDishSalesTop(@Param("tenantId") Long tenantId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("storeId") Long storeId,
                                               @Param("topN") Integer topN);
}
