package com.pinenuts.controller;

import com.pinenuts.common.Result;
import com.pinenuts.common.annotation.RequiresPermission;
import com.pinenuts.dto.report.*;
import com.pinenuts.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "营收报表", description = "营收报表相关接口")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    @Operation(summary = "报表概览")
    @RequiresPermission("report:list")
    public Result<ReportOverviewResponse> getOverview() {
        return Result.success(reportService.getOverview());
    }

    @GetMapping("/daily-revenue")
    @Operation(summary = "日营收趋势")
    @RequiresPermission("report:list")
    public Result<List<DailyRevenueResponse>> getDailyRevenue(ReportQueryRequest request) {
        return Result.success(reportService.getDailyRevenue(request));
    }

    @GetMapping("/store-ranking")
    @Operation(summary = "门店营业额排名")
    @RequiresPermission("report:list")
    public Result<List<StoreRankingResponse>> getStoreRanking(ReportQueryRequest request) {
        return Result.success(reportService.getStoreRanking(request));
    }

    @GetMapping("/dish-sales-top")
    @Operation(summary = "菜品销量Top")
    @RequiresPermission("report:list")
    public Result<List<DishSalesTopResponse>> getDishSalesTop(ReportQueryRequest request) {
        return Result.success(reportService.getDishSalesTop(request));
    }
}
