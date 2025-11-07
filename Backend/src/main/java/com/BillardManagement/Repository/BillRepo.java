package com.BillardManagement.Repository;

import com.BillardManagement.DTO.Response.DashboardStatsDTO;
import com.BillardManagement.Entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepo extends JpaRepository<Bill, Integer> {

    // Existing methods...
    List<Bill> findTop10ByOrderByCreatedDateDesc();
    List<Bill> findTop10ByBillStatusIgnoreCaseOrderByCreatedDateDesc(String billStatus);
    List<Bill> findTop10ByClubID_IdAndBillStatusIgnoreCaseOrderByCreatedDateDesc(Integer clubId, String billStatus);
    List<Bill> findTop10ByCustomerID_IdAndBillStatusIgnoreCaseOrderByCreatedDateDesc(Integer customerId, String billStatus);
    List<Bill> findTop10ByBillStatusIgnoreCaseOrderByEndTimeDesc(String billStatus);
    List<Bill> findTop10ByClubID_IdAndBillStatusIgnoreCaseOrderByEndTimeDesc(Integer clubId, String billStatus);
    List<Bill> findTop10ByCustomerID_IdAndBillStatusIgnoreCaseOrderByEndTimeDesc(Integer customerId, String billStatus);
    Optional<Bill> findFirstByTableID_IdAndEndTimeIsNullOrderByStartTimeDesc(Integer tableId);
    long countByCreatedDateBetween(Instant start, Instant end);
    long countByEndTimeBetween(Instant start, Instant end);
    List<Bill> findByBillStatusIgnoreCaseAndCreatedDateBetween(String billStatus, Instant start, Instant end);
    List<Bill> findByBillStatusIgnoreCaseAndEndTimeBetween(String billStatus, Instant start, Instant end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bill b where b.tableID.id = :tableId and b.endTime is null")
    Optional<Bill> lockActiveBillByTable(@Param("tableId") Integer tableId);

    Optional<Bill> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bill b where b.id = :id")
    Optional<Bill> lockById(@Param("id") Integer id);

    @Query("select b from Bill b left join fetch b.tableID where b.id = :id")
    Optional<Bill> findViewById(@Param("id") Integer id);

    // ==================== DASHBOARD QUERIES ====================

    /**
     * Tổng doanh thu của customer (từ tất cả clubs)
     */
    @Query("SELECT SUM(b.finalAmount) FROM Bill b " +
            "WHERE b.clubID.customerID = :customerId " +
            "AND b.billStatus = 'Paid'")
    Double findTotalRevenueByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Doanh thu trong khoảng thời gian
     */
    @Query("SELECT SUM(b.finalAmount) FROM Bill b " +
            "WHERE b.clubID.customerID = :customerId " +
            "AND b.billStatus = 'Paid' " +
            "AND b.endTime >= :startDate " +
            "AND b.endTime < :endDate")
    Double findTotalRevenueByCustomerIdAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Doanh thu theo ngày (7 ngày gần nhất)
     * Sử dụng endTime thay vì createdDate để chính xác hơn
     */
    @Query("SELECT new com.BillardManagement.DTO.Response.DashboardStatsDTO$RevenueData(" +
            "FUNCTION('DATE', b.endTime), " +
            "COALESCE(SUM(b.finalAmount), 0.0)) " +
            "FROM Bill b " +
            "WHERE b.clubID.customerID = :customerId " +
            "AND b.billStatus = 'Paid' " +
            "AND b.endTime >= :startDate " +
            "GROUP BY FUNCTION('DATE', b.endTime) " +
            "ORDER BY FUNCTION('DATE', b.endTime) ASC")
    List<DashboardStatsDTO.RevenueData> findDailyRevenueByCustomerId(
            @Param("customerId") Integer customerId,
            @Param("startDate") LocalDateTime startDate);

    /**
     * Số giờ sử dụng của từng bàn trong ngày hôm nay
     * Chỉ tính các bill đã paid hoặc đang active
     */
    @Query("SELECT new com.BillardManagement.DTO.Response.DashboardStatsDTO$TableUsageData(" +
            "t.tableName, " +
            "COALESCE(SUM(b.totalHours), 0.0)) " +
            "FROM Bill b " +
            "JOIN b.tableID t " +
            "WHERE b.clubID.customerID = :customerId " +
            "AND FUNCTION('DATE', b.startTime) = :today " +
            "AND b.billStatus IN ('Paid', 'Unpaid') " +
            "GROUP BY t.id, t.tableName " +
            "ORDER BY SUM(b.totalHours) DESC")
    List<DashboardStatsDTO.TableUsageData> findTodayTableUsageByCustomerId(
            @Param("customerId") Integer customerId,
            @Param("today") LocalDateTime today);

    /**
     * Đếm số bill trong ngày
     */
    @Query("SELECT COUNT(b) FROM Bill b " +
            "WHERE b.clubID.customerID = :customerId " +
            "AND FUNCTION('DATE', b.startTime) = :today")
    Long countTodayBillsByCustomerId(
            @Param("customerId") Integer customerId,
            @Param("today") LocalDateTime today);

    /**
     * Tính tổng doanh thu của các hóa đơn ĐÃ THANH TOÁN trong một khoảng thời gian.
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Bill b WHERE b.billDate BETWEEN :startDate AND :endDate AND b.status = 'PAID'")
    Double findTotalRevenueBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy dữ liệu doanh thu hàng ngày (cho biểu đồ) kể từ một ngày nhất định.
     * Sử dụng JPA constructor expression với DTO tĩnh lồng bên trong DashboardStatsDTO.
     */
    @Query("SELECT new com.BillardManagement.DTO.Response.DashboardStatsDTO$RevenueData(FUNCTION('DATE_FORMAT', b.billDate, '%Y-%m-%d'), SUM(b.totalAmount)) " +
            "FROM Bill b WHERE b.billDate >= :startDate AND b.status = 'PAID' " +
            "GROUP BY FUNCTION('DATE_FORMAT', b.billDate, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', b.billDate, '%Y-%m-%d') ASC")
    List<DashboardStatsDTO.RevenueData> findDailyRevenueSince(@Param("startDate") LocalDateTime startDate);

    /**
     * Lấy tổng số giờ chơi (dạng thập phân) theo từng bàn trong một khoảng thời gian.
     * Tính toán dựa trên endTime và startTime.
     */
    @Query("SELECT new com.BillardManagement.DTO.Response.DashboardStatsDTO$TableUsageData(b.billiardtable.tableName, SUM(FUNCTION('TIME_TO_SEC', FUNCTION('TIMEDIFF', b.endTime, b.startTime)) / 3600.0)) " +
            "FROM Bill b " +
            "WHERE b.billiardtable IS NOT NULL AND b.endTime IS NOT NULL AND b.startTime IS NOT NULL " +
            "AND b.billDate BETWEEN :startDate AND :endDate " +
            "GROUP BY b.billiardtable.tableName")
    List<DashboardStatsDTO.TableUsageData> findTableUsageBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}