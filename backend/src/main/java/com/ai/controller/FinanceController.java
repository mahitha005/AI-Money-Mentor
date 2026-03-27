package com.ai.backend.controller;

import com.ai.backend.model.FinanceRequest;
import com.ai.backend.service.FinanceService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:3000", "https://ai-money-mentor-vt91.onrender.com" })
public class FinanceController {

    private final FinanceService financeService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final Map<String, List<Map<String, String>>> sessionHistory = new ConcurrentHashMap<>();

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/analyze")
    public Map<String, Object> analyze(@RequestBody FinanceRequest request) {
        return financeService.calculateFinancialRatios(request);
    }

    @PostMapping("/ai-advice")
    public Map<String, Object> aiAdvice(@RequestBody Map<String, Object> body) {

        try {
            String question = body.get("question").toString();
            String sessionId = body.getOrDefault("sessionId", "default").toString();

            double income           = Double.parseDouble(body.get("income").toString());
            double expenses         = Double.parseDouble(body.get("expenses").toString());
            double monthlySavings   = Double.parseDouble(body.getOrDefault("monthlySavings", "0").toString());
            double totalSavings     = Double.parseDouble(body.getOrDefault("totalSavings", "0").toString());
            double emergencyFundGap = Double.parseDouble(body.getOrDefault("emergencyFundGap", "0").toString());
            double retirementCorpus = Double.parseDouble(body.getOrDefault("retirementCorpus", "0").toString());
            int    age              = Integer.parseInt(body.getOrDefault("age", "0").toString());
            String monthsCovered    = body.getOrDefault("monthsCovered", "0").toString();
            String savingsRate      = body.getOrDefault("savingsRate", "0%").toString();
            String debtRatio        = body.getOrDefault("debtRatio", "0%").toString();
            String health           = body.getOrDefault("health", "Unknown").toString();
            String language         = body.getOrDefault("language", "English").toString();

            List<Map<String, String>> history =
                    sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

            String systemContext = String.format(
                    "You are a smart, practical, and safe financial advisor.\n\n" +

                    "=== BEHAVIOR RULES ===\n" +
                    "- Answer in MAX 5 bullet points\n" +
                    "- Use **bold** for important terms\n" +
                    "- No long paragraphs\n" +
                    "- Base advice ONLY on user data below\n" +
                    "- Do NOT assume extra income or savings\n" +
                    "- Do NOT recompute values already given\n" +
                    "- Be realistic and conservative\n" +
                    "- Ask 1 relevant follow-up question at the end\n" +
                    "- Default language: %s\n" +
                    "- If user explicitly asks to switch language (e.g. 'speak in English', 'Telugu lo cheppu', 'Hindi mein bolo'), IMMEDIATELY switch to that language\n" +
                    "- If user's latest message is in a different language, prefer that language for your reply\n" +
                    "- If user mixes languages (Hinglish, Telugu-English), respond in the same mixed style\n" +
                    "- Never say you can only speak one language - be flexible and adaptive\n" +
                    "- Keep tone natural and human-like when switching languages\n" +
                    "- If user says their data was already provided, TRUST the USER DATA section - never say data is missing\n\n" +

                    "=== RISK RULES ===\n" +
                    "- Health=High -> suggest 60-70%% equity allocation\n" +
                    "- Health=Medium -> balanced portfolio, 50-60%% equity, avoid aggressive assets\n" +
                    "- Health=Low -> focus on savings and debt reduction first, avoid stocks/crypto\n\n" +

                    "=== EMERGENCY FUND RULE ===\n" +
                    "- If Emergency Gap > 0 -> prioritize building emergency fund before investing\n" +
                    "- Only suggest investments after gap = 0\n\n" +

                    "=== INVESTMENT RULES ===\n" +
                    "- Prefer: Index funds, Mutual funds, SIP\n" +
                    "- Avoid: Individual stock picking for beginners, high-risk assets if not stable\n\n" +

                    "=== RETIREMENT RULE ===\n" +
                    "- Use 4%% rule: Corpus = monthlyExpense x 12 x 25\n" +
                    "- State clearly if goal is realistic, ambitious, or highly aggressive\n\n" +

                    "=== GOAL PLANNING ===\n" +
                    "- Short-term (0-3 yrs), Mid-term (3-7 yrs), Long-term (7+ yrs)\n" +
                    "- Suggest saving strategy per category\n\n" +

                    "=== OUTPUT FORMAT ===\n" +
                    "\u2022 Key financial insight\n" +
                    "\u2022 Current status (good / needs improvement)\n" +
                    "\u2022 2-3 actionable suggestions\n" +
                    "\ud83d\udca1 Suggestions: practical steps\n" +
                    "\ud83d\udc49 One follow-up question\n\n" +

                    "=== USER DATA (use ONLY these values) ===\n" +
                    "- Age: %d years\n" +
                    "- Income: \u20b9%.0f/month\n" +
                    "- Expenses: \u20b9%.0f/month\n" +
                    "- Monthly Savings: \u20b9%.0f/month\n" +
                    "- Total Savings: \u20b9%.0f (accumulated)\n" +
                    "- Savings Rate: %s\n" +
                    "- Debt Ratio: %s\n" +
                    "- Emergency Fund Gap: \u20b9%.0f\n" +
                    "- Emergency Coverage: %s months\n" +
                    "- Retirement Corpus Needed: \u20b9%.0f\n" +
                    "- Financial Health: %s\n\n" +
                    "Now answer the user's question using ONLY the data above.",
                    language,
                    age, income, expenses, monthlySavings, totalSavings,
                    savingsRate, debtRatio, emergencyFundGap, monthsCovered,
                    retirementCorpus, health
            );

            List<Map<String, Object>> contents = new ArrayList<>();

            // Always inject system context as first turn
            contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", systemContext))));
            contents.add(Map.of("role", "model", "parts", List.of(Map.of("text",
                    "Understood. I will give concise financial advice."))));

            for (Map<String, String> turn : history) {
                contents.add(Map.of(
                        "role", turn.get("role"),
                        "parts", List.of(Map.of("text", turn.get("text")))
                ));
            }

            contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", question))));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

            ResponseEntity<Map> response =
                    new RestTemplate().postForEntity(url, new HttpEntity<>(requestBody, headers), Map.class);

            String answer = "AI response unavailable";

            try {
                List candidates = (List) response.getBody().get("candidates");
                Map contentMap = (Map) ((Map) candidates.get(0)).get("content");
                answer = ((Map) ((List) contentMap.get("parts")).get(0)).get("text").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            history.add(Map.of("role", "user", "text", question));
            history.add(Map.of("role", "model", "text", answer));

            return Map.of("answer", answer);

        } catch (Exception ex) {
            ex.printStackTrace();

            double income         = Double.parseDouble(body.getOrDefault("income", "0").toString());
            double monthlySavings = Double.parseDouble(body.getOrDefault("monthlySavings", "0").toString());
            double emergencyFundGap = Double.parseDouble(body.getOrDefault("emergencyFundGap", "0").toString());
            String savingsRate    = body.getOrDefault("savingsRate", "0%").toString();
            String monthsCovered  = body.getOrDefault("monthsCovered", "0").toString();

            String fallback = String.format(
                    "• Your savings rate is %s, which is %s\n" +
                    "• You have %s months of emergency coverage\n" +
                    "• You still need ₹%.0f to be fully safe\n\n" +
                    "💡 Suggestions:\n" +
                    "• Build emergency fund to 6 months\n" +
                    "• Invest using balanced strategy\n" +
                    "• Avoid high-risk assets\n\n" +
                    "👉 Want a monthly investment plan?",
                    savingsRate,
                    (income > 0 && monthlySavings / income > 0.2) ? "good" : "low",
                    monthsCovered,
                    emergencyFundGap
            );

            return Map.of("answer", fallback);
        }
    }

    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        sessionHistory.remove(sessionId);
        return Map.of("status", "cleared");
    }
}