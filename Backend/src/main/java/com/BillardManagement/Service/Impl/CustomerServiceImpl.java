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

import java.time.*;
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
    private ProductRepository productRepo;

    @Autowired
    private EmployeeshiftRepo employeeshiftRepo;

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
     * Lấy thống kê dashboard cho customer
     * Dữ liệu được tổng hợp từ TẤT CẢ các clubs thuộc customer này
     */
    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(Integer customerId) {
        // 1. Đảm bảo customer tồn tại
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        // 2. Tính toán các chỉ số tổng quan

        // Tổng doanh thu (từ tất cả clubs của customer)
        Double totalRevenue = billRepo.findTotalRevenueByCustomerId(customerId);

        // Tổng số bàn (từ tất cả clubs)
        Long totalTables = billiardTableRepo.countByCustomerId(customerId);

        // Tổng số nhân viên
        Long totalEmployees = employeeRepo.countByCustomerId(customerId);

        // Tổng số sản phẩm
        Long totalProducts = productRepo.countByCustomerId(customerId);

        // Số ca đang hoạt động (có actualStartTime nhưng chưa có actualEndTime)
        Long activeShifts = employeeshiftRepo.countActiveShiftsByCustomerId(customerId);

        // Tăng trưởng doanh thu theo tháng
        Double monthlyGrowth = calculateMonthlyGrowth(customerId);

        // 3. Lấy dữ liệu biểu đồ doanh thu (7 ngày gần nhất)
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        List<DashboardStatsDTO.RevenueData> revenueData =
                billRepo.findDailyRevenueByCustomerId(customerId, sevenDaysAgo);

        // Điền đủ 7 ngày nếu có ngày không có dữ liệu
        revenueData = fillMissingDates(revenueData, 7);

        // 4. Lấy dữ liệu sử dụng bàn (hôm nay)
        LocalDateTime today = LocalDate.now().atStartOfDay();
        List<DashboardStatsDTO.TableUsageData> tableUsageData =
                billRepo.findTodayTableUsageByCustomerId(customerId, today);

        // Giới hạn top 5 bàn được sử dụng nhiều nhất
        if (tableUsageData.size() > 5) {
            tableUsageData = tableUsageData.subList(0, 5);
        }

        // 5. Xây dựng DTO trả về
        return DashboardStatsDTO.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .totalTables(totalTables != null ? totalTables : 0L)
                .totalEmployees(totalEmployees != null ? totalEmployees : 0L)
                .totalProducts(totalProducts != null ? totalProducts : 0L)
                .activeShifts(activeShifts != null ? activeShifts : 0L)
                .monthlyGrowth(monthlyGrowth)
                .revenueData(revenueData)
                .tableUsageData(tableUsageData)
                .build();
    }

    /**
     * Tính toán % tăng trưởng doanh thu so với tháng trước
     */
    private Double calculateMonthlyGrowth(Integer customerId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth;

        // Doanh thu tháng này (từ đầu tháng đến hiện tại)
        Double revenueThisMonth = billRepo.findTotalRevenueByCustomerIdAndDateRange(
                customerId, startOfThisMonth, LocalDateTime.now());

        // Doanh thu tháng trước (trọn vẹn)
        Double revenueLastMonth = billRepo.findTotalRevenueByCustomerIdAndDateRange(
                customerId, startOfLastMonth, endOfLastMonth);

        double thisMonth = (revenueThisMonth != null) ? revenueThisMonth : 0.0;
        double lastMonth = (revenueLastMonth != null) ? revenueLastMonth : 0.0;

        if (lastMonth == 0.0) {
            return (thisMonth > 0.0) ? 100.0 : 0.0;
        }

        double growth = ((thisMonth - lastMonth) / lastMonth) * 100.0;
        return Math.round(growth * 100.0) / 100.0;
    }

    /**
     * Điền đủ các ngày thiếu trong dữ liệu doanh thu
     */
    private List<DashboardStatsDTO.RevenueData> fillMissingDates(
            List<DashboardStatsDTO.RevenueData> data, int days) {

        List<DashboardStatsDTO.RevenueData> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.toString();

            // Tìm dữ liệu cho ngày này
            DashboardStatsDTO.RevenueData found = data.stream()
                    .filter(d -> d.getDate().equals(dateStr))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                result.add(found);
            } else {
                // Thêm ngày với revenue = 0
                result.add(new DashboardStatsDTO.RevenueData(dateStr, 0.0));
            }
        }

        return result;
    }
}