package com.example.myapp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "invoice")
public class Invoice {
  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long userId;
  private BigDecimal amount;
  private LocalDateTime issuedAt;
  private boolean paid = false;
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

