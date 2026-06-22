package com.HOPn.AI_Pass.DTO;

import com.HOPn.AI_Pass.model.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
        @Schema(description = "Short task title", example = "Invoice #4521")
        @NotBlank String title,
        @Schema(description = "Task type", example = "INVOICE_REVIEW")
        @NotNull TaskType taskType,
        @Schema(description = "Raw text to be processed", example = "Invoice No: 4521\nTotal: $1,250.00")
        @NotBlank String inputText
) {}