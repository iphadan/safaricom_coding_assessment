package com.safaricom.mpessa_coding_interview.service;

import com.safaricom.mpessa_coding_interview.repo.WalletRepo;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
    private final WalletRepo walletRepo;
    public WalletService(WalletRepo walletRepo){
        this.walletRepo = walletRepo;
    }



}
