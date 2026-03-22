package com.ai.backend.service;

import com.ai.backend.model.FinanceRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FinanceService {

    public Map<String,Object> calculateFinancialRatios(FinanceRequest request){

        double income = request.getIncome();
        double expenses = request.getExpenses();
        double savings = request.getSavings();
        double investments = request.getInvestments();
        double debt = request.getDebt();

        double savingsRate = savings / income;
        double debtRatio = debt / income;
        double expenseRatio = expenses / income;
        double investmentRatio = investments / income;

        String health;

        if(savingsRate > 0.25 && debtRatio < 0.2){
            health = "High";
        }
        else if(savingsRate > 0.12){
            health = "Medium";
        }
        else{
            health = "Low";
        }

        double emergencyFundRequired = expenses * 6;

        double emergencyFundGap = emergencyFundRequired - savings;

        Map<String,Object> result = new HashMap<>();

        result.put("savingsRate", savingsRate);
        result.put("debtRatio", debtRatio);
        result.put("expenseRatio", expenseRatio);
        result.put("investmentRatio", investmentRatio);
        result.put("health", health);
        result.put("emergencyFundRequired", emergencyFundRequired);
        result.put("emergencyFundGap", emergencyFundGap);

        return result;
    }
}