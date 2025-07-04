package com.example.myapp.service;

import com.example.myapp.dto.InvoiceDto;
import com.example.myapp.entity.Invoice;

public class InvoiceMapper {
    public static InvoiceDto toDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setUserId(invoice.getUserId());
        dto.setAmount(invoice.getAmount());
        dto.setIssuedAt(invoice.getIssuedAt());
        dto.setPaid(invoice.isPaid());
        return dto;
    }
}
