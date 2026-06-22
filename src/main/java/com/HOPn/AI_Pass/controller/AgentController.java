package com.HOPn.AI_Pass.controller;

import com.HOPn.AI_Pass.mockAgent.Agent;
import com.HOPn.AI_Pass.mockAgent.AgentRegistry;
import com.HOPn.AI_Pass.mockAgent.AgentResult;
import com.HOPn.AI_Pass.DTO.AgentRunRequest;
import com.HOPn.AI_Pass.DTO.AgentRunResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/agents")
@Tag(name = "Agents", description = "Invoke named AI agents on input data")
public class AgentController {

    private final AgentRegistry registry;

    public AgentController(AgentRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/run")
    @Operation(
            summary = "Run an agent",
            description = "Invokes a named agent (e.g. DocumentAnalyst, InvoiceAuditor) on the provided input " +
                    "and returns its result with a confidence score."
    )
    public ResponseEntity<AgentRunResponse> run(@Valid @RequestBody AgentRunRequest request) {
        Agent agent = registry.find(request.agentName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown agent: " + request.agentName() +
                                ". Available: " + registry.listNames()
                ));

        AgentResult result = agent.run(request.input());

        return ResponseEntity.ok(new AgentRunResponse(
                agent.getName(),
                result.result(),
                result.confidence()
        ));
    }

    @GetMapping
    @Operation(
            summary = "List available agents",
            description = "Returns the names of all agents currently registered in the system."
    )
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(registry.listNames());
    }
}