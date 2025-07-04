package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  List<Invoice> findByUserId(Long userId);
}