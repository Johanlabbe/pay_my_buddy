package com.example.myapp.service;

import com.example.myapp.dto.InvoiceDto;
import com.example.myapp.entity.Invoice;

public class InvoiceMapper {
    public static InvoiceDto toDto(Invoice inv) {
        return new InvoiceDto(
            inv.getId(),
            inv.getUserId(),
            inv.getAmount(),
            inv.getIssuedAt(),
            inv.isPaid()
        );
    }
}
