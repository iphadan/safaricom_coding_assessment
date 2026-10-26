package com.safaricom.mpessa_coding_interview.repo;

import com.safaricom.mpessa_coding_interview.model.Reversal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReversalMapper extends JpaRepository<Reversal,String> {
}
