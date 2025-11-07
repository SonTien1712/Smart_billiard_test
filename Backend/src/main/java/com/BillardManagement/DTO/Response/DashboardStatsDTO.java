// Tệp: Backend/src/main/java/com/BillardManagement/DTO/Response/DashboardStatsDTO.java
package com.BillardManagement.DTO.Response;

import lombok.*; // Import thêm Builder

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder // ✅ SỬA LỖI 4: Thêm @Builder
public class DashboardStatsDTO {

    // Các chỉ số KPI Cards
    private BigDecimal todayRevenue;
    private Long todayBills;
    private Long totalTables;
    private Long totalEmployees;
    private Long activeShifts;
    private Long totalProducts;
    private Double monthlyGrowth; // % tăng trưởng so với tháng trước

    // Dữ liệu cho biểu đồ
    private List<RevenueData> revenueData;
    private List<TableUsageData> tableUsageData;

    /**
     * ✅ SỬA LỖI 5: Chuyển từ 'interface' thành 'static class'
     * Phải là class tĩnh để JPA constructor (new) trong BillRepo hoạt động
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueData {
        private String date;
        private BigDecimal revenue; // Giữ BigDecimal để khớp với truy vấn Repo
    }

    /**
     * ✅ SỬA LỖI 5: Chuyển từ 'interface' thành 'static class'
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TableUsageData {
        private String table;
        private Double hours;
    }
}