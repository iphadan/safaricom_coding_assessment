package com.safaricom.mpessa_coding_interview.repo;

import com.safaricom.mpessa_coding_interview.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepo extends JpaRepository<Transfer, String> {
}
