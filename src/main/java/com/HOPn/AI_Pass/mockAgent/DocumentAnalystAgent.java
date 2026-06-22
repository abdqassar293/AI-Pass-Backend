package com.HOPn.AI_Pass.mockAgent;


import org.springframework.stereotype.Component;

@Component
public class DocumentAnalystAgent implements Agent {

    @Override
    public String getName() {
        return "DocumentAnalyst";
    }

    @Override
    public AgentResult run(String input) {
        if (input == null || input.isBlank()) {
            return new AgentResult("No content to analyze", 0.0);
        }

        int words = input.trim().split("\\s+").length;
        int sentences = input.split("(?<=[.!?])\\s+").length;
        int paragraphs = input.split("\\n\\s*\\n").length;

        String firstSentence = input.split("(?<=[.!?])\\s+")[0];
        double readability = Math.min(0.95, 0.5 + (sentences / 200.0));

        String result = String.format(
                "Document contains %d words, %d sentences, %d paragraph(s). " +
                        "Opening line: \"%s\". Estimated readability score: %.2f.",
                words, sentences, paragraphs, firstSentence, readability
        );

        double confidence = Math.min(0.95, 0.6 + (words / 2000.0));
        return new AgentResult(result, confidence);
    }
}