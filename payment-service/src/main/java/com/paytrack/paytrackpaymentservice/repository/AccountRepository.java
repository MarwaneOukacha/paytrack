package com.paytrack.paytrackpaymentservice.repository;

import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.shared.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
 
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
 
    Optional<Account> findByAccountNumber(String accountNumber);
 
    Optional<Account> findByOwnerEmail(String ownerEmail);
    boolean existsByOwnerEmail(String ownerEmail);
 
    boolean existsByAccountNumber(String accountNumber);

    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

    Page<Account> findByOwnerEmailContainingIgnoreCase(String ownerEmail, Pageable pageable);

    Page<Account> findByStatusAndOwnerEmailContainingIgnoreCase(
            AccountStatus status,
            String ownerEmail,
            Pageable pageable);
 

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") UUID id);
 
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
 