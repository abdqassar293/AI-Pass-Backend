package com.HOPn.AI_Pass.mockAIEngine;


import com.HOPn.AI_Pass.model.TaskType;

public interface AiTaskHandler {
    boolean supports(TaskType type);
    ExecutionResult execute(String input);
}