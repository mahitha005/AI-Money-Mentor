import React from "react";
import FinancialForm from "./components/FinancialForm";

function App() {

  const handleSubmit = (data) => {
    console.log("User financial data:", data);
  };

  return (
    <div style={{textAlign:"center", marginTop:"50px"}}>

      <h1>💰 AI Money Mentor</h1>
      <p>Analyze your financial health</p>

      <FinancialForm onSubmit={handleSubmit}/>

    </div>
  );
}

export default App;