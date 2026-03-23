package com.ai.backend.controller;

import com.ai.backend.model.FinanceRequest;
import com.ai.backend.service.FinanceService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @PostMapping("/chat")
    public Map<String,String> chat(@RequestBody Map<String,String> body){

        String question = body.get("question").toLowerCase();

        String answer;

        if(question.contains("invest")){
            answer = "Consider starting SIP in diversified index funds and maintain long-term investment discipline.";
        }
        else if(question.contains("emergency fund")){
            answer = "An emergency fund should cover at least 6 months of your expenses.";
        }
        else if(question.contains("save")){
            answer = "Try to save at least 20% of your monthly income for financial stability.";
        }
        else{
            answer = "Focus on budgeting, saving consistently, and investing for long-term growth.";
        }

        Map<String,String> response = new HashMap<>();
        response.put("answer", answer);

        return response;
    }
}