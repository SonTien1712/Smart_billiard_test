package com.BillardManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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

    // Dữ liệu biểu đồ doanh thu 7 ngày
    private List<RevenueData> revenueData;

    // Dữ liệu biểu đồ sử dụng bàn
    private List<TableUsageData> tableUsageData;

    // DTO cho dữ liệu doanh thu theo ngày
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueData {
        private String date;  // Format: yyyy-MM-dd
        private Double revenue;
    }

    // DTO cho dữ liệu sử dụng bàn
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TableUsageData {
        private String table;  // Tên bàn
        private Double hours;  // Số giờ sử dụng
    }
}