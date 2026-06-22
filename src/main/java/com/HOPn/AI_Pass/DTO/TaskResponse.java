package com.HOPn.AI_Pass.DTO;

import com.HOPn.AI_Pass.model.TaskType;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        TaskType taskType,
        String inputText,
        String status,
        TaskResultDto result,
        Instant createdAt
) {}