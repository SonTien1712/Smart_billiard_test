// Tệp: Backend/src/main/java/com/BillardManagement/Service/Impl/CustomerServiceImpl.java
package com.BillardManagement.Service.Impl;

import com.BillardManagement.DTO.Request.UpdateCustomerRequest;
import com.BillardManagement.DTO.Response.DashboardStatsDTO;
import com.BillardManagement.Entity.Customer;
import com.BillardManagement.Exception.ResourceNotFoundException;
import com.BillardManagement.Repository.*;
import com.BillardManagement.Service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // ✅ SỬA LỖI NGẦM: Import BigDecimal
import java.time.*;
import java.time.temporal.TemporalAdjusters; // ✅ SỬA LỖI 3: Thêm import
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepo customerRepository;

    @Autowired
    private BillRepo billRepo;

    @Autowired
    private BilliardTableRepo billiardTableRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private ProductRepository productRepo; // Tên biến này là 'productRepo'

    @Autowired
    private EmployeeshiftRepo employeeshiftRepo;

    // ... (Các phương thức từ getAllCustomers đến getCurrentUser không đổi) ...

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> getCustomerById(Integer id) {
        return customerRepository.findById(id);
    }

    @Override
    public boolean registerCustomer(String name, String email, String phone, String address, String rawPassword) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (customerRepository.existsByPhoneNumber(phone)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        Customer c = new Customer();
        c.setCustomerName(name);
        c.setEmail(email);
        c.setPhoneNumber(phone);
        c.setAddress(address);
        c.setPassword(rawPassword);
        c.setIsActive(true);

        customerRepository.save(c);
        return true;
    }

    @Override
    public long countAll() {
        return customerRepository.count();
    }

    @Override
    public long countActive() {
        return customerRepository.countByIsActiveTrue();
    }

    @Override
    public long countNewInMonth(YearMonth ym) {
        var zone = ZoneId.systemDefault();
        var start = ym.atDay(1).atStartOfDay(zone).toInstant();
        var end = ym.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant();
        return customerRepository.countByDateJoinedBetween(start, end);
    }

    @Override
    public long countJoinedBetween(Instant from, Instant to) {
        return customerRepository.countByDateJoinedBetween(from, to);
    }

    @Override
    public double growthRateInMonth(YearMonth ym) {
        var prev = ym.minusMonths(1);
        long cur = countNewInMonth(ym);
        long pre = countNewInMonth(prev);
        return pre == 0 ? (cur > 0 ? 100.0 : 0.0) : ((cur - pre) * 100.0 / pre);
    }

    @Override
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    public Optional<Customer> updateStatus(Integer id, boolean isActive) {
        return customerRepository.findById(id).map(c -> {
            c.setIsActive(isActive);
            return customerRepository.save(c);
        });
    }

    @Override
    public Optional<Customer> updateCustomer(Integer id, UpdateCustomerRequest req) {
        return customerRepository.findById(id).map(c -> {
            if (req.getName() != null) c.setCustomerName(req.getName().trim());
            if (req.getEmail() != null) c.setEmail(req.getEmail().trim());
            if (req.getPhone() != null) c.setPhoneNumber(req.getPhone().trim());
            if (req.getAddress() != null) c.setAddress(req.getAddress().trim());
            return customerRepository.save(c);
        });
    }

    @Override
    public void updateExpireDate(Integer id, String planId) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        int monthsToAdd = planId.equals("1") ? 3 : 12;
        LocalDate currentExpire = customer.getExpiryDate();
        LocalDate newExpire = (currentExpire != null && currentExpire.isAfter(LocalDate.now()))
                ? currentExpire.plusMonths(monthsToAdd)
                : LocalDate.now().plusMonths(monthsToAdd);

        customer.setExpiryDate(newExpire);
        customerRepository.save(customer);
    }

    @Override
    public Customer getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        String email = authentication.getName();
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Invalid user email");
        }

        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * ✅ SỬA LỖI 1: Triển khai phương thức này (đã bị thiếu)
     * Đây là logic chính để lấy DTO, sử dụng các truy vấn (queries) CỤ THỂ của customer
     */
    @Override
    @Transactional(readOnly = true) // Tối ưu hóa cho việc đọc
    public DashboardStatsDTO getDashboardStats(Integer customerId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfSevenDaysAgo = startOfToday.minusDays(7);

        // === 1. Lấy dữ liệu KPI từ các Repo (cho customerId) ===

        // (Lưu ý: Các repo này cần có phương thức đếm theo customerId)
        // Giả sử các Repo đã có phương thức countByCustomerId
        Long totalTables = billiardTableRepo.countByClubCustomerID(customerId);
        Long totalEmployees = employeeRepo.countByClubCustomerID(customerId);

        // ✅ SỬA LỖI 2: Sửa 'productRepository' thành 'productRepo'
        Long totalProducts = productRepo.countByCustomerId(customerId);

        // Giả sử active shifts là của customer
        Long activeShifts = employeeshiftRepo.countActiveShiftsByCustomerId(customerId);

        // === 2. Lấy dữ liệu Bill (cho customerId) ===

        // Doanh thu và số bill hôm nay
        Double todayRevenueDouble = billRepo.findTotalRevenueByCustomerIdAndDateRange(customerId, startOfToday, now);
        BigDecimal todayRevenue = (todayRevenueDouble != null) ? BigDecimal.valueOf(todayRevenueDouble) : BigDecimal.ZERO;
        Long todayBills = billRepo.countTodayBillsByCustomerId(customerId, startOfToday);

        // === 3. Tăng trưởng hàng tháng (cho customerId) ===
        Double monthlyGrowth = calculateMonthlyGrowth(customerId);

        // === 4. Dữ liệu biểu đồ (cho customerId) ===
        List<DashboardStatsDTO.RevenueData> rawRevenueData = billRepo.findDailyRevenueByCustomerId(customerId, startOfSevenDaysAgo);
        // Điền 7 ngày
        List<DashboardStatsDTO.RevenueData> revenueData = fillMissingDates(rawRevenueData, 7);

        List<DashboardStatsDTO.TableUsageData> tableUsageData = billRepo.findTodayTableUsageByCustomerId(customerId, startOfToday);

        // === 5. Xây dựng DTO trả về ===
        return DashboardStatsDTO.builder()
                .todayRevenue(todayRevenue)
                .todayBills(todayBills)
                .totalTables(totalTables)
                .totalEmployees(totalEmployees)
                .activeShifts(activeShifts)
                .totalProducts(totalProducts)
                .monthlyGrowth(monthlyGrowth)
                .revenueData(revenueData)
                .tableUsageData(tableUsageData)
                .build();
    }

    /**
     * Phương thức này (không tham số) sẽ lấy customer hiện tại và gọi phương thức (có tham số) ở trên
     */
    @Override
    public DashboardStatsDTO getDashboardStats() {
        // Lấy customer hiện tại từ Spring Security
        Customer currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("Customer not authenticated or not found");
        }
        // Gọi logic chính
        return getDashboardStats(currentUser.getId());
    }


    /**
     * Tính toán % tăng trưởng doanh thu so với tháng trước (cho customerId)
     */
    private Double calculateMonthlyGrowth(Integer customerId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);

        // Doanh thu tháng này (từ đầu tháng đến hiện tại)
        Double revenueThisMonth = billRepo.findTotalRevenueByCustomerIdAndDateRange(
                customerId, startOfThisMonth, now);

        // Doanh thu tháng trước (trọn vẹn)
        Double revenueLastMonth = billRepo.findTotalRevenueByCustomerIdAndDateRange(
                customerId, startOfLastMonth, startOfThisMonth); // (từ đầu tháng trước ĐẾN đầu tháng này)

        double thisMonth = (revenueThisMonth != null) ? revenueThisMonth : 0.0;
        double lastMonth = (revenueLastMonth != null) ? revenueLastMonth : 0.0;

        if (lastMonth == 0.0) {
            return (thisMonth > 0.0) ? 100.0 : 0.0;
        }

        double growth = ((thisMonth - lastMonth) / lastMonth) * 100.0;
        return Math.round(growth * 100.0) / 100.0; // Làm tròn 2 chữ số
    }

    /**
     * Điền đủ các ngày thiếu trong dữ liệu doanh thu
     */
    private List<DashboardStatsDTO.RevenueData> fillMissingDates(
            List<DashboardStatsDTO.RevenueData> data, int days) {

        List<DashboardStatsDTO.RevenueData> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1); // Lấy đủ 'days' ngày

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.toString(); // Định dạng 'YYYY-MM-DD'

            // Tìm dữ liệu cho ngày này (phải khớp định dạng YYYY-MM-DD)
            DashboardStatsDTO.RevenueData found = data.stream()
                    .filter(d -> d.getDate().equals(dateStr))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                result.add(found);
            } else {
                // ✅ SỬA LỖI 5 (Logic): Khởi tạo class, không phải interface
                // Dùng BigDecimal.ZERO thay vì 0.0
                result.add(new DashboardStatsDTO.RevenueData(dateStr, BigDecimal.ZERO));
            }
        }

        return result;
    }
}