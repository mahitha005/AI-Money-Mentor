import React, { useState, useRef, useEffect } from "react";
import FinancialForm from "./components/FinancialForm";

const SESSION_ID = "session_" + Date.now();

// Simple markdown renderer: bold, bullets, links
function renderMarkdown(text) {
  return text
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/^[•\-]\s(.+)/gm, "<li>$1</li>")
    .replace(/(<li>[\s\S]*?<\/li>)/g, "<ul style='text-align:left;padding-left:18px;margin:6px 0'>$1</ul>")
    .replace(/\[([^\]]+)\]\((https?:\/\/[^\)]+)\)/g, '<a href="$2" target="_blank" rel="noreferrer" style="color:#0070f3">$1</a>')
    .replace(/\n{2,}/g, "<br/>");
}

export default function App() {
  const [result, setResult]                     = useState(null);
  const [question, setQuestion]                 = useState("");
  const [chatHistory, setChatHistory]           = useState([]);
  const [loading, setLoading]                   = useState(false);
  const [monthlySavings, setMonthlySavings]     = useState("");
  const [presentSaved, setPresentSaved]         = useState("");
  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatHistory, loading]);

  const handleSubmit = async (formData) => {
    try {
      const res  = await fetch("http://localhost:8080/api/analyze", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });
      const data = await res.json();
      const ctx  = { ...data, ...formData };
      setResult(ctx);
      // Proactive AI suggestion fires automatically after analysis
      await callAI("Give me a quick overview of my financial health and your top 2 suggestions.", ctx);
    } catch (e) {
      console.error(e);
    }
  };

  const callAI = async (q, ctx) => {
    const activeCtx = ctx || result;
    if (!activeCtx) return;
    setLoading(true);
    const gap = (activeCtx.emergencyFundRequired || 0) - parseFloat(presentSaved || 0);
    try {
      const res  = await fetch("http://localhost:8080/api/ai-advice", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          sessionId:            SESSION_ID,
          question:             q,
          income:               activeCtx.income        || 0,
          expenses:             activeCtx.expenses      || 0,
          savings:              activeCtx.savings       || 0,
          investments:          activeCtx.investments   || 0,
          debt:                 activeCtx.debt          || 0,
          monthlySavings:       parseFloat(monthlySavings || 0),
          presentSavedAmount:   parseFloat(presentSaved   || 0),
          emergencyFundRequired:activeCtx.emergencyFundRequired || 0,
          emergencyFundGap:     gap,
          health:               activeCtx.health        || "Unknown",
        }),
      });
      const data = await res.json();
      setChatHistory(prev => [
        ...prev,
        { role: "user", text: q },
        { role: "ai",   text: data.answer },
      ]);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const askAI = async () => {
    if (!question.trim()) return;
    const q = question;
    setQuestion("");
    await callAI(q);
  };

  const emergencyGap = result
    ? (result.emergencyFundRequired || 0) - parseFloat(presentSaved || 0)
    : null;

  const cards = result ? [
    { label: "Financial Health",         value: result.health },
    { label: "Savings Rate",             value: result.savingsRate },
    { label: "Debt Ratio",               value: result.debtRatio },
    { label: "Expense Ratio",            value: result.expenseRatio },
    { label: "Emergency Fund Required",  value: `₹${result.emergencyFundRequired}` },
    { label: "Emergency Fund Gap",       value: `₹${emergencyGap?.toFixed(2)}`, highlight: emergencyGap > 0 },
    { label: "Recommended Portfolio",    value: result.recommendedPortfolio },
  ] : [];

  return (
    <div style={{ maxWidth: 820, margin: "40px auto", fontFamily: "Arial", padding: "0 20px" }}>
      <h1 style={{ textAlign: "center" }}>💰 AI Money Mentor</h1>

      {/* Monthly Savings + Present Saved Amount */}
      <div style={{ display: "flex", gap: 12, justifyContent: "center", marginBottom: 20 }}>
        <input
          type="number" placeholder="Monthly Savings (₹)"
          value={monthlySavings} onChange={e => setMonthlySavings(e.target.value)}
          style={inputStyle}
        />
        <input
          type="number" placeholder="Present Total Saved Amount (₹)"
          value={presentSaved} onChange={e => setPresentSaved(e.target.value)}
          style={inputStyle}
        />
      </div>

      <FinancialForm onSubmit={handleSubmit} />

      {/* Result Cards */}
      {result && (
        <div style={{ display: "flex", flexWrap: "wrap", gap: 14, marginTop: 32, justifyContent: "center" }}>
          {cards.map(({ label, value, highlight }) => (
            <div key={label} style={{ ...cardStyle, borderColor: highlight ? "#e53e3e" : "#ddd" }}>
              <div style={{ fontSize: 12, color: "#666" }}>{label}</div>
              <div style={{ fontSize: 16, fontWeight: "bold", marginTop: 6, color: highlight ? "#e53e3e" : "#222" }}>
                {value}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* AI Chat */}
      <div style={{ marginTop: 48 }}>
        <h2 style={{ textAlign: "center" }}>🤖 AI Financial Advisor</h2>

        <div style={{
          border: "1px solid #ddd", borderRadius: 12, padding: 16,
          minHeight: 200, maxHeight: 440, overflowY: "auto", background: "#f9f9f9"
        }}>
          {chatHistory.length === 0 && (
            <p style={{ color: "#aaa", textAlign: "center" }}>
              Submit your financial data above to get started...
            </p>
          )}
          {chatHistory.map((msg, i) => (
            <div key={i} style={{ marginBottom: 14, textAlign: msg.role === "user" ? "right" : "left" }}>
              <span style={{
                display: "inline-block",
                background: msg.role === "user" ? "#0070f3" : "#fff",
                color: msg.role === "user" ? "#fff" : "#222",
                border: msg.role === "ai" ? "1px solid #e2e8f0" : "none",
                borderRadius: 10, padding: "10px 14px", maxWidth: "80%", textAlign: "left",
              }}>
                {msg.role === "ai"
                  ? <span dangerouslySetInnerHTML={{ __html: renderMarkdown(msg.text) }} />
                  : msg.text}
              </span>
            </div>
          ))}
          {loading && <p style={{ color: "#888", textAlign: "center" }}>⏳ Thinking...</p>}
          <div ref={chatEndRef} />
        </div>

        <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
          <input
            type="text" placeholder="Ask a follow-up question..."
            value={question} onChange={e => setQuestion(e.target.value)}
            onKeyDown={e => e.key === "Enter" && askAI()}
            style={{ ...inputStyle, flex: 1 }}
            disabled={!result}
          />
          <button onClick={askAI} disabled={loading || !result} style={btnStyle}>
            Ask
          </button>
        </div>
      </div>
    </div>
  );
}

const cardStyle = {
  border: "1px solid #ddd", borderRadius: 10, padding: "14px 18px",
  minWidth: 160, boxShadow: "0 2px 8px rgba(0,0,0,0.07)",
  background: "#fff", textAlign: "center",
};
const inputStyle = {
  padding: "10px 12px", borderRadius: 8,
  border: "1px solid #ccc", fontSize: 14,
};
const btnStyle = {
  padding: "10px 22px", borderRadius: 8,
  background: "#0070f3", color: "#fff",
  border: "none", cursor: "pointer", fontSize: 14,
};
