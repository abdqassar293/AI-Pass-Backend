package com.HOPn.AI_Pass.mockAIEngine;

import com.HOPn.AI_Pass.model.TaskType;
import org.springframework.stereotype.Component;

@Component
public class DocumentSummaryHandler implements AiTaskHandler {

    @Override
    public boolean supports(TaskType type) {
        return type == TaskType.DOCUMENT_SUMMARY;
    }

    @Override
    public ExecutionResult execute(String input) {
        if (input == null || input.isBlank()) {
            return new ExecutionResult("FAIL", 0.0, "Empty input text");
        }

        String[] sentences = input.split("(?<=[.!?])\\s+");
        int wordCount = input.trim().split("\\s+").length;
        int sentenceCount = sentences.length;

        String summary = sentenceCount >= 2
                ? sentences[0] + " " + sentences[1]
                : sentences[0];

        // Confidence scales with input size, capped at 0.95
        double confidence = Math.min(0.95, 0.55 + (wordCount / 1000.0));

        String explanation = String.format(
                "Summarized %d words across %d sentences. Summary: %s",
                wordCount, sentenceCount, summary
        );

        return new ExecutionResult("SUMMARIZED", confidence, explanation);
    }
}