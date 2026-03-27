package com.ai.backend.model;

public class FinanceRequest {

    private int age;
    private double income;
    private double expenses;
    private double monthlySavings;
    private double totalSavings;
    private double investments;
    private double debt;

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }

    public double getExpenses() { return expenses; }
    public void setExpenses(double expenses) { this.expenses = expenses; }

    public double getMonthlySavings() { return monthlySavings; }
    public void setMonthlySavings(double monthlySavings) { this.monthlySavings = monthlySavings; }

    public double getTotalSavings() { return totalSavings; }
    public void setTotalSavings(double totalSavings) { this.totalSavings = totalSavings; }

    public double getInvestments() { return investments; }
    public void setInvestments(double investments) { this.investments = investments; }

    public double getDebt() { return debt; }
    public void setDebt(double debt) { this.debt = debt; }
}