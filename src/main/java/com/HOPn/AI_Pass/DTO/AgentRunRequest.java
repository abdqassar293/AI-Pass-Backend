package com.HOPn.AI_Pass.DTO;


import jakarta.validation.constraints.NotBlank;

public record AgentRunRequest(
        @NotBlank String agentName,
        @NotBlank String input
) {}