package com.HOPn.AI_Pass.mockAIEngine;

import com.HOPn.AI_Pass.model.TaskType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class InvoiceReviewHandler implements AiTaskHandler {

    private record Field(String name, Pattern pattern) {}

    private static final List<Field> REQUIRED_FIELDS = List.of(
            new Field("invoice number", Pattern.compile("(?i)invoice\\s*(no\\.?|number|#)\\s*[:#]?\\s*\\S+")),
            new Field("amount/total",   Pattern.compile("(?i)(total|amount|sum|due)\\s*[:=]?\\s*\\$?\\s*\\d")),
            new Field("date",           Pattern.compile("(?i)(date|dated|issued)\\s*[:=]?\\s*\\S+"))
    );

    @Override
    public boolean supports(TaskType type) {
        return type == TaskType.INVOICE_REVIEW;
    }

    @Override
    public ExecutionResult execute(String input) {
        if (input == null || input.isBlank()) {
            return new ExecutionResult("FAIL", 0.0, "Empty input text");
        }

        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (Field f : REQUIRED_FIELDS) {
            if (f.pattern().matcher(input).find()) found.add(f.name());
            else missing.add(f.name());
        }

        double confidence = (double) found.size() / REQUIRED_FIELDS.size();
        String decision = missing.isEmpty() ? "PASS" : "FAIL";
        String explanation = missing.isEmpty()
                ? "All required fields detected: " + String.join(", ", found)
                : "Missing fields: " + String.join(", ", missing)
                + (found.isEmpty() ? "" : ". Detected: " + String.join(", ", found));

        return new ExecutionResult(decision, confidence, explanation);
    }
}