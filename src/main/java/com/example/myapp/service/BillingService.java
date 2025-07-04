package com.example.myapp.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.myapp.dto.InvoiceDto;

public interface BillingService {
  InvoiceDto issueInvoice(Long userId, BigDecimal amount);
  List<InvoiceDto> listInvoices(Long userId);
  InvoiceDto markAsPaid(Long invoiceId);
}
