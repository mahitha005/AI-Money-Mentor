package com.ai.backend.service;

import com.ai.backend.model.FinanceRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class FinanceService {

    public Map<String,Object> calculateFinancialRatios(FinanceRequest request){

        double income = request.getIncome();
        double expenses = request.getExpenses();
        double savings = request.getSavings();
        double investments = request.getInvestments();
        double debt = request.getDebt();

        // Financial ratios
        double savingsRate = savings / income;
        double debtRatio = debt / income;
        double expenseRatio = expenses / income;
        double investmentRatio = investments / income;

        // ML Prediction
        String health = predictHealthML(
                savingsRate,
                expenseRatio,
                debtRatio,
                investmentRatio
        );

        // Emergency fund
        double emergencyFundRequired = expenses * 6;
        double emergencyFundGap = emergencyFundRequired - savings;

        // Portfolio recommendation
        String portfolio;

        if("High".equalsIgnoreCase(health)){
            portfolio = "70% Equity Index Funds, 20% Debt Funds, 10% Gold";
        }
        else if("Medium".equalsIgnoreCase(health)){
            portfolio = "60% Equity Index Funds, 30% Debt Funds, 10% Gold";
        }
        else{
            portfolio = "40% Equity Index Funds, 40% Debt Funds, 20% Gold";
        }

        Map<String,Object> result = new HashMap<>();

        // Convert ratios to percentage
        result.put("savingsRate", String.format("%.2f%%", savingsRate * 100));
        result.put("debtRatio", String.format("%.2f%%", debtRatio * 100));
        result.put("expenseRatio", String.format("%.2f%%", expenseRatio * 100));
        result.put("investmentRatio", String.format("%.2f%%", investmentRatio * 100));

        result.put("health", health);
        result.put("emergencyFundRequired", emergencyFundRequired);
        result.put("emergencyFundGap", emergencyFundGap);
        result.put("recommendedPortfolio", portfolio);

        return result;
    }


    public String predictHealthML(double savingsRate,
                                  double expenseRatio,
                                  double debtRatio,
                                  double investmentRatio){

        try{

            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "ml-model/predict.py",
                    String.valueOf(savingsRate),
                    String.valueOf(expenseRatio),
                    String.valueOf(debtRatio),
                    String.valueOf(investmentRatio)
            );

            Process process = pb.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String prediction = reader.readLine();

            if(prediction == null || prediction.isEmpty()){
                return "Medium";
            }

            return prediction;

        }
        catch(Exception e){
            e.printStackTrace();
            return "Medium";
        }
    }
}