package com.example.myapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.myapp.entity.Invoice;
import com.example.myapp.repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock private InvoiceRepository invoiceRepo;
    @InjectMocks private BillingServiceImpl billingService;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setUserId(42L);
        invoice.setAmount(new BigDecimal("150.00"));
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setPaid(false);
    }

    @Test
    void issueInvoice_shouldPersist_andReturnDto() {
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(invoice);

        var dto = billingService.issueInvoice(42L, new BigDecimal("150.00"));

        assertEquals(1L, dto.getId());
        assertEquals(42L, dto.getUserId());
        assertEquals(new BigDecimal("150.00"), dto.getAmount());
        assertFalse(dto.isPaid());
        verify(invoiceRepo).save(any(Invoice.class));
    }

    @Test
    void listInvoices_shouldReturnAll() {
        when(invoiceRepo.findByUserId(42L)).thenReturn(List.of(invoice));

        var list = billingService.listInvoices(42L);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
    }

    @Test
    void markAsPaid_shouldUpdatePaidFlag() {
        invoice.setPaid(false);
        when(invoiceRepo.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepo.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = billingService.markAsPaid(1L);
        assertTrue(dto.isPaid());
        verify(invoiceRepo).save(argThat(i -> i.isPaid()));
    }
}
