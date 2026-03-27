package com.ai.backend.service;

import com.ai.backend.model.FinanceRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class FinanceService {

    public Map<String, Object> calculateFinancialRatios(FinanceRequest request) {

        double income         = request.getIncome();
        double expenses       = request.getExpenses();
        double monthlySavings = request.getMonthlySavings();
        double totalSavings   = request.getTotalSavings();
        double investments    = request.getInvestments();
        double debt           = request.getDebt();

        double savingsRate     = income > 0 ? monthlySavings / income : 0;
        double debtRatio       = income > 0 ? debt / income : 0;
        double expenseRatio    = income > 0 ? expenses / income : 0;
        double investmentRatio = income > 0 ? investments / income : 0;

        String health = predictHealthML(savingsRate, expenseRatio, debtRatio, investmentRatio);

        double emergencyFundRequired = expenses * 6;
        double emergencyFundGap      = Math.max(0, emergencyFundRequired - totalSavings);
        double monthsCovered         = expenses > 0 ? totalSavings / expenses : 0;
        double retirementCorpus      = expenses * 12 * 25;

        Map<String, Object> result = new HashMap<>();
        result.put("savingsRate",           String.format("%.2f%%", savingsRate * 100));
        result.put("debtRatio",             String.format("%.2f%%", debtRatio * 100));
        result.put("expenseRatio",          String.format("%.2f%%", expenseRatio * 100));
        result.put("health",                health);
        result.put("emergencyFundRequired", emergencyFundRequired);
        result.put("emergencyFundGap",      emergencyFundGap);
        result.put("monthsCovered",         String.format("%.1f", monthsCovered));
        result.put("retirementCorpus",       retirementCorpus);
        result.put("income",                income);
        result.put("expenses",              expenses);
        result.put("monthlySavings",        monthlySavings);
        result.put("totalSavings",          totalSavings);
        result.put("investments",           investments);
        result.put("debt",                  debt);
        return result;
    }

    public String predictHealthML(double savingsRate, double expenseRatio,
                                   double debtRatio, double investmentRatio) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "python", "ml-model/predict.py",
                String.valueOf(savingsRate),
                String.valueOf(expenseRatio),
                String.valueOf(debtRatio),
                String.valueOf(investmentRatio)
            );
            process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String prediction = reader.readLine();
                return (prediction == null || prediction.isBlank()) ? "Medium" : prediction.trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Medium";
        } finally {
            if (process != null) process.destroy();
        }
    }
}
