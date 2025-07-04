package com.example.myapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceDto {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime issuedAt;
    private boolean paid;

    public InvoiceDto() {}

    public InvoiceDto(Long id, Long userId, BigDecimal amount, LocalDateTime issuedAt, boolean paid) {
        this.id       = id;
        this.userId   = userId;
        this.amount   = amount;
        this.issuedAt = issuedAt;
        this.paid     = paid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
