import React, { useState } from "react";

function FinancialForm({ onSubmit }) {

  const [formData, setFormData] = useState({
    age: "",
    income: "",
    expenses: "",
    savings: "",
    investments: "",
    debt: ""
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit}>

      <input
        type="number"
        name="age"
        placeholder="Age"
        onChange={handleChange}
      /><br/><br/>

      <input
        type="number"
        name="income"
        placeholder="Monthly Income"
        onChange={handleChange}
      /><br/><br/>

      <input
        type="number"
        name="expenses"
        placeholder="Monthly Expenses"
        onChange={handleChange}
      /><br/><br/>

      <input
        type="number"
        name="savings"
        placeholder="Savings"
        onChange={handleChange}
      /><br/><br/>

      <input
        type="number"
        name="investments"
        placeholder="Investments"
        onChange={handleChange}
      /><br/><br/>

      <input
        type="number"
        name="debt"
        placeholder="Debt"
        onChange={handleChange}
      /><br/><br/>

      <button type="submit">
        Analyze Financial Health
      </button>

    </form>
  );
}

export default FinancialForm;