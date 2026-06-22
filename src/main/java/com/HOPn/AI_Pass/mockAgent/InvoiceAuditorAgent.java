package com.HOPn.AI_Pass.mockAgent;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InvoiceAuditorAgent implements Agent {

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("\\$\\s*([0-9]+(?:[,.][0-9]+)*)");
    private static final Pattern INVOICE_NO_PATTERN =
            Pattern.compile("(?i)invoice\\s*(?:no\\.?|number|#)\\s*[:#]?\\s*(\\S+)");

    @Override
    public String getName() {
        return "InvoiceAuditor";
    }

    @Override
    public AgentResult run(String input) {
        if (input == null || input.isBlank()) {
            return new AgentResult("No invoice content provided", 0.0);
        }

        List<String> findings = new ArrayList<>();

        Matcher invMatch = INVOICE_NO_PATTERN.matcher(input);
        if (invMatch.find()) {
            findings.add("Invoice number: " + invMatch.group(1));
        } else {
            findings.add("WARNING: invoice number not found");
        }

        Matcher amtMatch = AMOUNT_PATTERN.matcher(input);
        List<String> amounts = new ArrayList<>();
        while (amtMatch.find()) amounts.add("$" + amtMatch.group(1));
        if (!amounts.isEmpty()) {
            findings.add("Amounts detected: " + String.join(", ", amounts));
        } else {
            findings.add("WARNING: no monetary amounts detected");
        }

        boolean hasDate = input.matches("(?is).*\\b(20\\d{2}|19\\d{2})[-/]\\d{1,2}[-/]\\d{1,2}.*")
                || input.toLowerCase().contains("date");
        findings.add(hasDate ? "Date reference: present" : "WARNING: no date reference");

        long warnings = findings.stream().filter(f -> f.startsWith("WARNING")).count();
        double confidence = Math.max(0.3, 1.0 - (warnings * 0.25));

        return new AgentResult(String.join(" | ", findings), confidence);
    }
}
