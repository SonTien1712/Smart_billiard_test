package com.BillardManagement.DTO.Response;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class DashboardStatsDTO {
    private Integer todayBills;
    private BigDecimal todayRevenue;
    private Integer totalTables;
    private Integer totalEmployees;
    private Integer activeShifts;
    private Integer totalProducts;
    private BigDecimal monthlyGrowth;
//    private List<RevenueDataPoint> revenueData; // Last 7 days
//    private List<TableUsageData> tableUsage;
}

