import React, { useState } from "react";

const fields = [
  { name: "age",         placeholder: "Age" },
  { name: "income",      placeholder: "Monthly Income (₹)" },
  { name: "expenses",    placeholder: "Monthly Expenses (₹)" },
  { name: "savings",     placeholder: "Current Savings (₹)" },
  { name: "investments", placeholder: "Investments (₹)" },
  { name: "debt",        placeholder: "Debt (₹)" },
];

export default function FinancialForm({ onSubmit }) {
  const [formData, setFormData] = useState(
    Object.fromEntries(fields.map(f => [f.name, ""]))
  );

  const handleChange = e =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = e => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit}
      style={{ display: "flex", flexWrap: "wrap", gap: 12, justifyContent: "center" }}>
      {fields.map(({ name, placeholder }) => (
        <input
          key={name} type="number" name={name}
          placeholder={placeholder} value={formData[name]}
          onChange={handleChange} required={name !== "age"}
          style={{
            padding: "10px 12px", borderRadius: 8,
            border: "1px solid #ccc", fontSize: 14, width: 200,
          }}
        />
      ))}
      <button type="submit" style={{
        width: "100%", padding: 12, borderRadius: 8,
        background: "#0070f3", color: "#fff",
        border: "none", fontSize: 15, cursor: "pointer", marginTop: 4,
      }}>
        Analyze Financial Health
      </button>
    </form>
  );
}
