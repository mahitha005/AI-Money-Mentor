package com.ai.backend.controller;

import com.ai.backend.model.FinanceRequest;
import com.ai.backend.service.FinanceService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/analyze")
    public Map<String,Object> analyze(@RequestBody FinanceRequest request){

        return financeService.calculateFinancialRatios(request);
    }
}