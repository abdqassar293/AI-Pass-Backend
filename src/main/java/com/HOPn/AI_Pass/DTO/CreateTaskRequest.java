package com.HOPn.AI_Pass.DTO;

import com.HOPn.AI_Pass.model.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
        @NotBlank String title,
        @NotNull TaskType taskType,
        @NotBlank String inputText
) {}