package com.example.myapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.dto.InvoiceDto;
import com.example.myapp.entity.Invoice;
import com.example.myapp.repository.InvoiceRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepo;

    public BillingServiceImpl(InvoiceRepository invoiceRepo) {
        this.invoiceRepo = invoiceRepo;
    }

    @Override
    public InvoiceDto issueInvoice(Long userId, java.math.BigDecimal amount) {
        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setAmount(amount);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setPaid(false);
        Invoice saved = invoiceRepo.save(invoice);
        return InvoiceMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDto> listInvoices(Long userId) {
        return invoiceRepo.findByUserId(userId).stream()
                          .map(InvoiceMapper::toDto)
                          .collect(Collectors.toList());
    }

    @Override
    public InvoiceDto markAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + invoiceId));
        invoice.setPaid(true);
        Invoice saved = invoiceRepo.save(invoice);
        return InvoiceMapper.toDto(saved);
    }
}
