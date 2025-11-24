package com.shubham.stockmonitoring.masterdata.service;

import com.shubham.stockmonitoring.commons.dto.BaseResponse;

import com.shubham.stockmonitoring.masterdata.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {
    
    private final InstrumentRepository instrumentRepository;

    public BaseResponse getAllStocks(Pageable pageable) {
        return BaseResponse.success(instrumentRepository.findAll(pageable).getContent());
    }

}
