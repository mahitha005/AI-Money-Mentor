import React, { useState, useRef, useEffect } from "react";
import "./App.css";

const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080";
const SESSION_ID = "session_" + Date.now();

function renderMarkdown(text) {
  return text
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/^[•\-]\s(.+)/gm, "<li>$1</li>")
    .replace(/(<li>[\s\S]*?<\/li>)/g, "<ul>$1</ul>")
    .replace(/\n/g, "<br/>");
}

function healthClass(h) {
  if (!h) return "";
  if (h === "High")   return "good";
  if (h === "Medium") return "warn";
  return "danger";
}

export default function App() {

  const [formData, setFormData] = useState({
    age: "", income: "", expenses: "",
    monthlySavings: "", totalSavings: "", debt: "", investments: ""
  });
  const [language, setLanguage] = useState("English");
  const [result, setResult]     = useState(null);
  const [chatHistory, setChatHistory] = useState([]);
  const [question, setQuestion] = useState("");
  const [loading, setLoading]   = useState(false);
  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatHistory, loading]);

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  // ── ANALYZE ──
  const handleSubmit = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/analyze`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...formData,
          income:         parseFloat(formData.income || 0),
          expenses:       parseFloat(formData.expenses || 0),
          monthlySavings: parseFloat(formData.monthlySavings || 0),
          totalSavings:   parseFloat(formData.totalSavings || 0),
          debt:           parseFloat(formData.debt || 0),
          investments:    parseFloat(formData.investments || 0)
        })
      });
      const data = await res.json();
      const ctx = { ...data, age: parseInt(formData.age || 0), language };
      setResult(ctx);
      setChatHistory([]);
      await callAI("Give my financial summary and top 2 improvements", ctx);
    } catch (e) { console.error(e); }
  };

  // ── AI ──
  const callAI = async (q, ctx) => {
    const active = ctx || result;
    if (!active) return;
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/ai-advice`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          sessionId: SESSION_ID, question: q,
          age:              active.age || 0,
          income:           active.income || 0,
          expenses:         active.expenses || 0,
          monthlySavings:   active.monthlySavings || 0,
          totalSavings:     active.totalSavings || 0,
          savingsRate:      active.savingsRate || "0%",
          debtRatio:        active.debtRatio || "0%",
          emergencyFundGap: active.emergencyFundGap || 0,
          monthsCovered:    active.monthsCovered || "0",
          retirementCorpus: active.retirementCorpus || 0,
          health:           active.health || "Unknown",
          language:         active.language || language
        })
      });
      const data = await res.json();
      setChatHistory(prev => [
        ...prev,
        { role: "user", text: q },
        { role: "ai",   text: data.answer }
      ]);
    } catch (e) { console.error(e); }
    setLoading(false);
  };

  const askAI = async () => {
    if (!question.trim()) return;
    const q = question;
    setQuestion("");
    await callAI(q);
  };

  const handleKey = (e) => { if (e.key === "Enter") askAI(); };

  // ── UI ──
  return (
    <>
      <div className="header">
        <h1>💰 AI Money Mentor</h1>
        <p>Smart financial advice powered by AI — personalized for you</p>
      </div>

      <div className="main">

        {/* BASIC INFO */}
        <div className="card">
          <div className="card-title">👤 Basic Info</div>
          <div className="grid-2">
            <Field label="Age"><input name="age" placeholder="e.g. 25" onChange={handleChange} /></Field>
            <Field label="Monthly Income (₹)"><input name="income" placeholder="e.g. 200000" onChange={handleChange} /></Field>
          </div>
        </div>

        {/* MONTHLY CASH FLOW */}
        <div className="card">
          <div className="card-title">💸 Monthly Cash Flow</div>
          <div className="grid-3">
            <Field label="Expenses (₹)"><input name="expenses" placeholder="e.g. 30000" onChange={handleChange} /></Field>
            <Field label="Savings (₹)"><input name="monthlySavings" placeholder="e.g. 50000" onChange={handleChange} /></Field>
            <Field label="Debt EMI (₹)"><input name="debt" placeholder="e.g. 10000" onChange={handleChange} /></Field>
          </div>
        </div>

        {/* FINANCIAL POSITION */}
        <div className="card">
          <div className="card-title">🏦 Current Financial Position</div>
          <div className="grid-2">
            <Field label="Total Savings (₹)"><input name="totalSavings" placeholder="e.g. 100000" onChange={handleChange} /></Field>
            <Field label="Investments (₹)"><input name="investments" placeholder="e.g. 50000" onChange={handleChange} /></Field>
          </div>
        </div>

        {/* LANGUAGE */}
        <div className="card">
          <div className="card-title">🌐 Preferred Language</div>
          <div style={{ maxWidth: 260 }}>
            <Field label="AI Response Language">
              <select value={language} onChange={(e) => setLanguage(e.target.value)}>
                <option>English</option>
                <option>Telugu</option>
                <option>Hindi</option>
                <option>Hinglish</option>
              </select>
            </Field>
          </div>
        </div>

        <button className="btn-analyze" onClick={handleSubmit}>
          ⚡ Analyze Financial Health
        </button>

        {/* RESULTS */}
        {result && (
          <>
            <div className="results-grid">
              <ResultCard
                icon="🏥" label="Health" value={result.health}
                variant={healthClass(result.health)}
              />
              <ResultCard icon="💰" label="Savings Rate" value={result.savingsRate} />
              <ResultCard icon="💳" label="Debt Ratio"   value={result.debtRatio}
                variant={parseFloat(result.debtRatio) > 40 ? "danger" : parseFloat(result.debtRatio) > 20 ? "warn" : ""}
              />
              <ResultCard icon="🛡️" label="Emergency Required" value={`₹${result.emergencyFundRequired?.toLocaleString()}`} />
              <ResultCard
                icon="⚠️" label="Emergency Gap"
                value={`₹${result.emergencyFundGap?.toLocaleString()}`}
                variant={result.emergencyFundGap > 0 ? "danger" : "good"}
              />
              <ResultCard icon="📅" label="Months Covered" value={`${result.monthsCovered} mo`}
                variant={parseFloat(result.monthsCovered) >= 6 ? "good" : parseFloat(result.monthsCovered) >= 3 ? "warn" : "danger"}
              />
            </div>

            {/* CHAT */}
            <div className="chat-wrapper">
              <div className="chat-header">
                🤖 AI Financial Advisor
                <span style={{ marginLeft: "auto", color: "#4a5568", fontWeight: 400 }}>
                  {language}
                </span>
              </div>
              <div className="chat-messages">
                {chatHistory.map((msg, i) => (
                  <div key={i} className={`msg-row ${msg.role}`}>
                    <div
                      className={`msg-bubble ${msg.role}`}
                      dangerouslySetInnerHTML={{ __html: renderMarkdown(msg.text) }}
                    />
                  </div>
                ))}
                {loading && (
                  <div className="msg-row ai">
                    <div className="typing">
                      <span /><span /><span />
                    </div>
                  </div>
                )}
                <div ref={chatEndRef} />
              </div>
            </div>

            <div className="chat-input-row">
              <input
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                onKeyDown={handleKey}
                placeholder="Ask anything about your finances..."
              />
              <button className="btn-ask" onClick={askAI}>Send ➤</button>
            </div>
          </>
        )}

      </div>
    </>
  );
}

// ── COMPONENTS ──
function Field({ label, children }) {
  return (
    <div className="field">
      <label>{label}</label>
      {children}
    </div>
  );
}

function ResultCard({ icon, label, value, variant = "" }) {
  return (
    <div className={`result-card ${variant}`}>
      <div className="rc-icon">{icon}</div>
      <div className="rc-label">{label}</div>
      <div className="rc-value">{value}</div>
    </div>
  );
}
