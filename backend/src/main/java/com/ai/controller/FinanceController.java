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
@CrossOrigin(origins = "http://localhost:3000")
public class FinanceController {

    private final FinanceService financeService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // In-memory session history: sessionId -> [{role, text}, ...]
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
            String question        = body.get("question").toString();
            String sessionId       = body.getOrDefault("sessionId", "default").toString();

            double income          = Double.parseDouble(body.get("income").toString());
            double expenses        = Double.parseDouble(body.get("expenses").toString());
            double savings         = Double.parseDouble(body.get("savings").toString());
            double investments     = Double.parseDouble(body.get("investments").toString());
            double debt            = Double.parseDouble(body.get("debt").toString());
            double monthlySavings  = Double.parseDouble(body.getOrDefault("monthlySavings", "0").toString());
            double presentSaved    = Double.parseDouble(body.getOrDefault("presentSavedAmount", "0").toString());
            double emergencyReq    = Double.parseDouble(body.get("emergencyFundRequired").toString());
            double emergencyGap    = emergencyReq - presentSaved;
            String health          = body.get("health").toString();

            List<Map<String, String>> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

            String systemContext = String.format(
                "You are a concise, structured financial advisor AI.\n\n" +
                "STRICT RULES:\n" +
                "- Answer in MAX 5 bullet points. No long paragraphs.\n" +
                "- Use **bold** for key terms.\n" +
                "- Base ALL advice strictly on the user's financial data below.\n" +
                "- After your answer, add a section '💡 AI Suggestions:' with 1-2 proactive tips the user hasn't asked.\n" +
                "- If investment/stock related, add '🔗 Useful Links:' with 1-2 real trustworthy links " +
                  "(Groww: https://groww.in, Zerodha: https://zerodha.com, NSE: https://nseindia.com, " +
                  "BSE: https://bseindia.com, SEBI: https://sebi.gov.in, Moneycontrol: https://moneycontrol.com) " +
                  "and a one-line reason why.\n" +
                "- Never suggest anything not supported by the user's data.\n\n" +
                "User Financial Profile:\n" +
                "- Monthly Income: ₹%.2f\n" +
                "- Monthly Expenses: ₹%.2f\n" +
                "- Current Savings: ₹%.2f\n" +
                "- Investments: ₹%.2f\n" +
                "- Debt: ₹%.2f\n" +
                "- Monthly Savings Contribution: ₹%.2f\n" +
                "- Present Total Saved Amount: ₹%.2f\n" +
                "- Emergency Fund Required (6x expenses): ₹%.2f\n" +
                "- Emergency Fund Gap: ₹%.2f\n" +
                "- Financial Health: %s\n",
                income, expenses, savings, investments, debt,
                monthlySavings, presentSaved, emergencyReq, emergencyGap, health
            );

            List<Map<String, Object>> contents = new ArrayList<>();

            // Inject system context only once at start of session
            if (history.isEmpty()) {
                contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", systemContext))));
                contents.add(Map.of("role", "model", "parts", List.of(Map.of("text",
                    "Understood. I will give short, structured, data-driven advice with proactive suggestions."))));
            }

            // Replay full session history for memory
            for (Map<String, String> turn : history) {
                contents.add(Map.of(
                    "role", turn.get("role"),
                    "parts", List.of(Map.of("text", turn.get("text")))
                ));
            }

            // Add current user question
            contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", question))));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
            ResponseEntity<Map> response = new RestTemplate()
                .postForEntity(url, new HttpEntity<>(requestBody, headers), Map.class);

            String answer = "AI response unavailable";
            try {
                List candidates = (List) response.getBody().get("candidates");
                Map contentMap  = (Map) ((Map) candidates.get(0)).get("content");
                answer = ((Map) ((List) contentMap.get("parts")).get(0)).get("text").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Save turn to session memory
            history.add(Map.of("role", "user", "text", question));
            history.add(Map.of("role", "model", "text", answer));

            Map<String, Object> result = new HashMap<>();
            result.put("answer", answer);
            result.put("emergencyFundGap", emergencyGap);
            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
            return Map.of("answer", "AI service currently unavailable.");
        }
    }

    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        sessionHistory.remove(sessionId);
        return Map.of("status", "cleared");
    }
}
