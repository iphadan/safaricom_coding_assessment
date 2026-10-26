package com.safaricom.mpessa_coding_interview.controller;

import com.safaricom.mpessa_coding_interview.dto.ReversalResponse;
import com.safaricom.mpessa_coding_interview.dto.TransferRequest;
import com.safaricom.mpessa_coding_interview.dto.TransferResponse;
import com.safaricom.mpessa_coding_interview.service.TransferReversalService;
import com.safaricom.mpessa_coding_interview.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mpesa/v1/p2p/transfers")
public class TransferController {

    private final TransferService transferService;
    private final TransferReversalService reversalService;

    public TransferController(TransferService transferService, TransferReversalService reversalService) {
        this.transferService = transferService;
        this.reversalService = reversalService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> executeTransfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.executeTransfer(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/reversal")
    public ResponseEntity<ReversalResponse> reverseTransfer(@PathVariable String id) {
        return ResponseEntity.ok(reversalService.reverseTransfer(id));
    }
}