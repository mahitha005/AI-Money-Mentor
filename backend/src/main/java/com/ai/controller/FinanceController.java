package com.ai.backend.controller;

import com.ai.backend.model.FinanceRequest;
import com.ai.backend.service.FinanceService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class FinanceController {

    private final FinanceService financeService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public FinanceController(FinanceService financeService){
        this.financeService = financeService;
    }

    // Financial analysis endpoint
    @PostMapping("/analyze")
    public Map<String,Object> analyze(@RequestBody FinanceRequest request){
        return financeService.calculateFinancialRatios(request);
    }

    // Gemini AI advisor
    @PostMapping("/ai-advice")
    public Map<String,String> aiAdvice(@RequestBody Map<String,String> body){

        String question = body.get("question");

        RestTemplate restTemplate = new RestTemplate();

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key="
                        + geminiApiKey;

        Map<String,Object> part = new HashMap<>();
        part.put("text", question);

        List<Map<String,Object>> parts = new ArrayList<>();
        parts.add(part);

        Map<String,Object> content = new HashMap<>();
        content.put("parts", parts);

        List<Map<String,Object>> contents = new ArrayList<>();
        contents.add(content);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String,Object>> request =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        Map responseBody = response.getBody();

        String answer = "AI response unavailable";

        try {

            List candidates = (List) responseBody.get("candidates");

            Map candidate = (Map) candidates.get(0);

            Map contentMap = (Map) candidate.get("content");

            List partsList = (List) contentMap.get("parts");

            Map textPart = (Map) partsList.get(0);

            answer = textPart.get("text").toString();

        } catch(Exception e){
            e.printStackTrace();
        }

        Map<String,String> result = new HashMap<>();
        result.put("answer", answer);

        return result;
    }
}