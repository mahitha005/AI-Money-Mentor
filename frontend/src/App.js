import React, { useState } from "react";
import FinancialForm from "./components/FinancialForm";

function App() {

  const [result, setResult] = useState(null);

  const handleSubmit = async (formData) => {

    try {

      const response = await fetch("http://localhost:8080/api/analyze", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      setResult(data);

    } catch (error) {
      console.error("Error connecting backend:", error);
    }
  };

  return (
    <div style={{ textAlign: "center", marginTop: "40px", fontFamily: "Arial" }}>

      <h1>💰 AI Money Mentor</h1>
      <p>Analyze your financial health</p>

      <FinancialForm onSubmit={handleSubmit} />

      {result && (

        <div style={{
          marginTop: "40px",
          display: "flex",
          justifyContent: "center",
          gap: "20px",
          flexWrap: "wrap"
        }}>

          <div style={cardStyle}>
            <h3>Financial Health</h3>
            <p>{result.health}</p>
          </div>

          <div style={cardStyle}>
            <h3>Savings Rate</h3>
            <p>{result.savingsRate}</p>
          </div>

          <div style={cardStyle}>
            <h3>Debt Ratio</h3>
            <p>{result.debtRatio}</p>
          </div>

          <div style={cardStyle}>
            <h3>Expense Ratio</h3>
            <p>{result.expenseRatio}</p>
          </div>

          <div style={cardStyle}>
            <h3>Emergency Fund Required</h3>
            <p>₹{result.emergencyFundRequired}</p>
          </div>

          <div style={cardStyle}>
            <h3>Emergency Fund Gap</h3>
            <p>₹{result.emergencyFundGap}</p>
          </div>

          <div style={cardStyle}>
            <h3>Recommended Portfolio</h3>
            <p>{result.recommendedPortfolio}</p>
          </div>

        </div>

      )}

    </div>
  );
}

const cardStyle = {
  border: "1px solid #ddd",
  borderRadius: "10px",
  padding: "20px",
  width: "200px",
  boxShadow: "0 4px 10px rgba(0,0,0,0.1)",
  background: "#fafafa"
};

export default App;