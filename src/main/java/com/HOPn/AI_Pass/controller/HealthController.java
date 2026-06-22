package com.HOPn.AI_Pass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@Tag(name = "System", description = "Health and infrastructure endpoints")
public class HealthController {

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Returns service status. Used by load balancers and uptime monitors."
    )
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "ai-pass",
                "timestamp", Instant.now().toString()
        ));
    }
}