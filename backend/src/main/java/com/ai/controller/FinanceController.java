package com.ai.backend.controller;

import com.ai.backend.model.FinanceRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class FinanceController {

    @PostMapping("/analyze")
    public Map<String,String> analyze(@RequestBody FinanceRequest request){

        Map<String,String> response = new HashMap<>();

        response.put("status","API working");
        response.put("message","Financial analysis endpoint ready");

        return response;
    }
}