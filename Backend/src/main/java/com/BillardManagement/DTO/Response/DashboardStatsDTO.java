package com.BillardManagement.DTO.Response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsDTO {

    // Tổng quan thống kê
    private Double totalRevenue;
    private Long totalTables;
    private Long totalEmployees;
    private Long activeShifts;
    private Long totalProducts;
    private Double monthlyGrowth;

    // Dữ liệu biểu đồ
    private List<RevenueData> revenueData;
    private List<TableUsageData> tableUsageData;

    // ✅ CRITICAL: Must be STATIC for JPA constructor expressions
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueData {
        private String date;
        private Double revenue;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TableUsageData {
        private String table;
        private Double hours;
    }
}