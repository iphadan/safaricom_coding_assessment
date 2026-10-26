package com.safaricom.mpessa_coding_interview.repo;

import com.safaricom.mpessa_coding_interview.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerEntryRepo extends JpaRepository<LedgerEntry, String> {
    List<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(String walletId);
}