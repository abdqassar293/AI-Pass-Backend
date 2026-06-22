package com.HOPn.AI_Pass.controller;

import com.HOPn.AI_Pass.DTO.CreateTaskRequest;
import com.HOPn.AI_Pass.DTO.TaskResponse;
import com.HOPn.AI_Pass.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Create AI tasks and retrieve execution history")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(
            summary = "Create and execute a task",
            description = "Submits a task to the execution engine. Supported types: DOCUMENT_SUMMARY, INVOICE_REVIEW. " +
                    "Returns the structured result with decision, confidence, and explanation."
    )
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        TaskResponse response = taskService.createAndExecute(request, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Get task history",
            description = "Returns all tasks created by the authenticated user, newest first."
    )
    public ResponseEntity<List<TaskResponse>> history(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskService.getHistory(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a single task",
            description = "Retrieves one task by ID. Users can only access their own tasks."
    )
    public ResponseEntity<TaskResponse> getOne(@PathVariable Long id,
                                               @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskService.getById(id, jwt.getSubject()));
    }
}