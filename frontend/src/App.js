import React from "react";
import FinancialForm from "./components/FinancialForm";

function App() {

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

      console.log("Backend response:", data);

      alert(data.message);

    } catch (error) {
      console.error("Error connecting backend:", error);
    }
  };

  return (
    <div style={{ textAlign: "center", marginTop: "50px" }}>

      <h1>💰 AI Money Mentor</h1>
      <p>Analyze your financial health</p>

      <FinancialForm onSubmit={handleSubmit} />

    </div>
  );
}

export default App;