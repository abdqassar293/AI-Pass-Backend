package com.HOPn.AI_Pass.DTO;

import java.time.Instant;

public record TaskResultDto(
        String status,
        String decision,
        double confidence,
        String explanation,
        Instant timestamp
) {}