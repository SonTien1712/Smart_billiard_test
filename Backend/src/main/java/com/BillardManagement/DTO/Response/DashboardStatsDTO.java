package com.BillardManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {

    // Các chỉ số KPI Cards
    private BigDecimal todayRevenue;
    private Long todayBills;
    private Long totalTables;
    private Long totalEmployees;
    private Long activeShifts;
    private Long totalProducts;
    private Double monthlyGrowth; // % tăng trưởng so với tháng trước

    // Dữ liệu cho biểu đồ (Sử dụng DTOs được định nghĩa trong BillRepo query)
    private List<RevenueData> revenueData;
    private List<TableUsageData> tableUsageData;

    /**
     * Interface cho Spring Data Projection khớp với query:
     * findDailyRevenueByCustomerId trong BillRepo
     */
    public interface RevenueData {
        String getDate();
        BigDecimal getRevenue();
    }

    /**
     * Interface cho Spring Data Projection khớp với query:
     * findTodayTableUsageByCustomerId trong BillRepo
     */
    public interface TableUsageData {
        String getTable();
        Double getHours();
    }
}