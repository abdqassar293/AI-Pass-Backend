package com.HOPn.AI_Pass.DTO;

public record AgentRunResponse(
        String agent,
        String result,
        double confidence
) {}