package com.pinenuts.service;

import com.pinenuts.dto.report.*;

import java.util.List;

public interface ReportService {
    ReportOverviewResponse getOverview();
    List<DailyRevenueResponse> getDailyRevenue(ReportQueryRequest request);
    List<StoreRankingResponse> getStoreRanking(ReportQueryRequest request);
    List<DishSalesTopResponse> getDishSalesTop(ReportQueryRequest request);
}
