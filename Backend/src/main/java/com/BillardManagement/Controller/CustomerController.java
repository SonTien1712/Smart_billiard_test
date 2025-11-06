package com.BillardManagement.Controller;

import com.BillardManagement.DTO.Response.DashboardStatsDTO;
import com.BillardManagement.Entity.Billardclub;
import com.BillardManagement.Entity.Customer;
import com.BillardManagement.Service.BilliardClubService;
import com.BillardManagement.Service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.BillardManagement.DTO.Response.DashboardStatsDTO;
import com.BillardManagement.Entity.Customer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000") // Cho phép React frontend truy cập
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BilliardClubService billiardClubService;

    // Lấy tất cả khách hàng
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return customerService.getCustomerById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Customer not found")));
    }

    // Lấy danh sách clubs của 1 customer
    @GetMapping("/{id}/clubs")
    public ResponseEntity<?> getClubsByCustomer(@PathVariable Integer id) {
        List<Billardclub> clubs = billiardClubService.getClubsByCustomerId(id);
        return ResponseEntity.ok(clubs);
    }

//    @GetMapping("/dashboard")
//    public ResponseEntity<DashboardStatsDTO> getDashboard() {
//        Customer user = customerService.getCurrentUser();
//        return ResponseEntity.ok(customerService.getDashboardStats(user.getId()));
//    }
    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStatsDTO> getDashboard(
            @AuthenticationPrincipal Customer authenticatedCustomer
    ) {
        if (authenticatedCustomer == null) {
            // Trường hợp này hiếm khi xảy ra nếu Spring Security được cấu hình đúng,
            // nhưng vẫn nên kiểm tra
            return ResponseEntity.status(401).build();
        }

        // Gọi service với ID của customer đang đăng nhập
        Integer customerId = authenticatedCustomer.getId();
        DashboardStatsDTO stats = customerService.getDashboardStats(customerId);

        return ResponseEntity.ok(stats);
    }
}