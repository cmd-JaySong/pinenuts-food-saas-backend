package com.pinenuts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinenuts.common.TenantContext;
import com.pinenuts.dto.report.*;
import com.pinenuts.entity.SalesOrder;
import com.pinenuts.mapper.SalesOrderMapper;
import com.pinenuts.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SalesOrderMapper salesOrderMapper;

    @Override
    @Cacheable(value = "reportOverview", key = "'tenant:' + T(com.pinenuts.common.TenantContext).getTenantId()")
    public ReportOverviewResponse getOverview() {
        Long tenantId = TenantContext.getTenantId();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate monthStart = today.withDayOfMonth(1);

        // 今日营收和订单数
        BigDecimal todayRevenue = getRevenueByDateRange(tenantId, today, today);
        Integer todayOrderCount = getOrderCountByDateRange(tenantId, today, today);

        // 本周营收
        BigDecimal weekRevenue = getRevenueByDateRange(tenantId, weekStart, today);

        // 本月营收
        BigDecimal monthRevenue = getRevenueByDateRange(tenantId, monthStart, today);

        // 昨日营收（计算增长率）
        BigDecimal yesterdayRevenue = getRevenueByDateRange(tenantId, yesterday, yesterday);

        // 计算同比增长率
        BigDecimal growthRate = BigDecimal.ZERO;
        if (yesterdayRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = todayRevenue.subtract(yesterdayRevenue)
                    .divide(yesterdayRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return ReportOverviewResponse.builder()
                .todayRevenue(todayRevenue)
                .weekRevenue(weekRevenue)
                .monthRevenue(monthRevenue)
                .todayOrderCount(todayOrderCount)
                .revenueGrowthRate(growthRate)
                .build();
    }

    @Override
    @Cacheable(value = "report", key = "'dailyRevenue:' + T(com.pinenuts.common.TenantContext).getTenantId() + ':' + #request.startDate + ':' + #request.endDate + ':' + #request.storeId")
    public List<DailyRevenueResponse> getDailyRevenue(ReportQueryRequest request) {
        Long tenantId = TenantContext.getTenantId();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : endDate.minusDays(29);
        return salesOrderMapper.getDailyRevenue(tenantId, startDate, endDate, request.getStoreId());
    }

    @Override
    @Cacheable(value = "report", key = "'storeRanking:' + T(com.pinenuts.common.TenantContext).getTenantId() + ':' + #request.startDate + ':' + #request.endDate + ':' + #request.topN")
    public List<StoreRankingResponse> getStoreRanking(ReportQueryRequest request) {
        Long tenantId = TenantContext.getTenantId();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : endDate.minusDays(29);
        Integer topN = request.getTopN() != null ? request.getTopN() : 10;
        return salesOrderMapper.getStoreRanking(tenantId, startDate, endDate, topN);
    }

    @Override
    @Cacheable(value = "report", key = "'dishSalesTop:' + T(com.pinenuts.common.TenantContext).getTenantId() + ':' + #request.startDate + ':' + #request.endDate + ':' + #request.storeId + ':' + #request.topN")
    public List<DishSalesTopResponse> getDishSalesTop(ReportQueryRequest request) {
        Long tenantId = TenantContext.getTenantId();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : endDate.minusDays(29);
        Integer topN = request.getTopN() != null ? request.getTopN() : 10;
        return salesOrderMapper.getDishSalesTop(tenantId, startDate, endDate, request.getStoreId(), topN);
    }

    private BigDecimal getRevenueByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getOrderTime, LocalDateTime.of(startDate, LocalTime.MIN))
                .le(SalesOrder::getOrderTime, LocalDateTime.of(endDate, LocalTime.MAX));

        List<SalesOrder> orders = salesOrderMapper.selectList(wrapper);
        return orders.stream()
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer getOrderCountByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getOrderTime, LocalDateTime.of(startDate, LocalTime.MIN))
                .le(SalesOrder::getOrderTime, LocalDateTime.of(endDate, LocalTime.MAX));

        return Math.toIntExact(salesOrderMapper.selectCount(wrapper));
    }
}
