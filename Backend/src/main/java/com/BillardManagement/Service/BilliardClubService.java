package com.BillardManagement.Service;

import com.BillardManagement.Entity.Billardclub;
import java.util.List;
import java.util.Optional;

public interface BilliardClubService {

    List<Billardclub> getAllClubs();

    Optional<Billardclub> getClubById(Integer id);

    // ✅ SỬA LỖI 2: Đảm bảo phương thức là abstract (không có body, không static)
    List<Billardclub> getClubsByCustomerId(Integer customerId);

    Billardclub createClub(Billardclub club);

    Billardclub updateClub(Integer id, Billardclub updated);

    void deleteClub(Integer id);
}