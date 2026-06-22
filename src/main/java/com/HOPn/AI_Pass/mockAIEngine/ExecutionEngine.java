package com.HOPn.AI_Pass.mockAIEngine;

import com.HOPn.AI_Pass.model.TaskType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionEngine {

    private final List<AiTaskHandler> handlers;

    public ExecutionEngine(List<AiTaskHandler> handlers) {
        this.handlers = handlers;
    }

    public ExecutionResult execute(TaskType type, String input) {
        return handlers.stream()
                .filter(h -> h.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No handler for task type: " + type))
                .execute(input);
    }
}