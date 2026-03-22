package com.library.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.library.entity.BorrowRecord;
import com.library.entity.User;

public interface BorrowRepository extends JpaRepository<BorrowRecord, Long> {

    // Used by scheduler to find all borrowed books
    List<BorrowRecord> findByStatus(String status);

    // Used to fetch all borrow records by specific user
    List<BorrowRecord> findByUser(User user);
    List<BorrowRecord> findByStatusIgnoreCase(String status);

}
