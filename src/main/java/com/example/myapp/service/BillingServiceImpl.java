package com.example.myapp.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.myapp.dto.InvoiceDto;
import com.example.myapp.entity.Invoice;
import com.example.myapp.repository.InvoiceRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {
  private final InvoiceRepository invoiceRepo;
  public BillingServiceImpl(InvoiceRepository invoiceRepo) {
    this.invoiceRepo = invoiceRepo;
  }

  @Override
  public InvoiceDto issueInvoice(Long userId, BigDecimal amount) {
    Invoice invoice = new Invoice();
    return InvoiceMapper.toDto(invoiceRepo.save(invoice));
  }

  @Override
  public List<InvoiceDto> listInvoices(Long userId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'listInvoices'");
  }

  @Override
  public InvoiceDto markAsPaid(Long invoiceId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'markAsPaid'");
  }
}
