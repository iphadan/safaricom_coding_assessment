package com.safaricom.mpessa_coding_interview.service;

import com.safaricom.mpessa_coding_interview.model.IdempotencyRecord;
import com.safaricom.mpessa_coding_interview.repo.IdempotencyRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private final IdempotencyRepo idempotencyRepo;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepo idempotencyRepo, ObjectMapper objectMapper) {
        this.idempotencyRepo = idempotencyRepo;
        this.objectMapper = objectMapper;
    }

    public <T> T executeWithIdempotency(String idempotencyKey, Object requestPayload, Class<T> responseType, Supplier<T> action) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }

        String requestHash = hashPayload(requestPayload);

        // 1. Attempt to claim the idempotency key atomically via DB unique constraint
        try {
            IdempotencyRecord record = new IdempotencyRecord();
            record.setIdempotencyKey(idempotencyKey);
            record.setRequestHash(requestHash);
            record.setStatus("RUNNING");
            idempotencyRepo.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            // Key already exists (either completed or currently running on another instance)
            return handleExistingKey(idempotencyKey, requestHash, responseType);
        }

        try {
            // 2. Execute actual business logic
            T result = action.get();
            String responseBody = objectMapper.writeValueAsString(result);

            // 3. Mark as COMPLETED and cache response
            IdempotencyRecord record = idempotencyRepo.findById(idempotencyKey).orElseThrow();
            record.setStatus("COMPLETED");
            record.setResponseCode(200);
            record.setResponseBody(responseBody);
            idempotencyRepo.save(record);

            return result;
        } catch (Exception e) {
            // Mark record as FAILED so it doesn't stay stuck in RUNNING
            IdempotencyRecord record = idempotencyRepo.findById(idempotencyKey).orElse(null);
            if (record != null) {
                record.setStatus("FAILED");
                idempotencyRepo.save(record);
            }
            throw e;
        }
    }

    private <T> T handleExistingKey(String idempotencyKey, String requestHash, Class<T> responseType) {
        int attempts = 0;
        // Poll for up to 3 seconds to let the in-flight request finish
        while (attempts < 30) {
            IdempotencyRecord record = idempotencyRepo.findById(idempotencyKey).orElse(null);
            if (record != null) {
                // Verify request body hash matches (prevents body tampering with same key)
                if (!record.getRequestHash().equals(requestHash)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT");
                }
                if ("COMPLETED".equals(record.getStatus())) {
                    try {
                        return objectMapper.readValue(record.getResponseBody(), responseType);
                    } catch (Exception ex) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse cached response");
                    }
                }
                if ("FAILED".equals(record.getStatus())) {
                    throw new ResponseStatusException(HttpStatus.valueOf(422), "PREVIOUS_REQUEST_FAILED");
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
            attempts++;
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "REQUEST_IN_PROGRESS");
    }

    private String hashPayload(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash request payload", e);
        }
    }
}