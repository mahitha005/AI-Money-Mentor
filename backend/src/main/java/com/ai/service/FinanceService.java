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

        double savingsRate = (savings / income) * 100;
        double debtRatio = (debt / income) * 100;
        double expenseRatio = (expenses / income) * 100;
        double investmentRatio = (investments / income) * 100;

        String health;

        if(savingsRate > 25 && debtRatio < 20){
            health = "High";
        }
        else if(savingsRate > 12){
            health = "Medium";
        }
        else{
            health = "Low";
        }

        double emergencyFundRequired = expenses * 6;
        double emergencyFundGap = emergencyFundRequired - savings;

        String portfolio;

        if(health.equals("High")){
            portfolio = "70% Equity Index Funds, 20% Debt Funds, 10% Gold";
        }
        else if(health.equals("Medium")){
            portfolio = "60% Equity Index Funds, 30% Debt Funds, 10% Gold";
        }
        else{
            portfolio = "40% Equity Index Funds, 40% Debt Funds, 20% Gold";
        }

        Map<String,Object> result = new HashMap<>();

        result.put("savingsRate", String.format("%.1f%%", savingsRate));
        result.put("debtRatio", String.format("%.1f%%", debtRatio));
        result.put("expenseRatio", String.format("%.1f%%", expenseRatio));
        result.put("investmentRatio", String.format("%.1f%%", investmentRatio));

        result.put("health", health);
        result.put("emergencyFundRequired", emergencyFundRequired);
        result.put("emergencyFundGap", emergencyFundGap);
        result.put("recommendedPortfolio", portfolio);

        return result;
    }
}