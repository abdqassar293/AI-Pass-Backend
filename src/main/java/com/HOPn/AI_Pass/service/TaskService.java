package com.HOPn.AI_Pass.service;

import com.HOPn.AI_Pass.DTO.CreateTaskRequest;
import com.HOPn.AI_Pass.DTO.TaskResponse;
import com.HOPn.AI_Pass.DTO.TaskResultDto;
import com.HOPn.AI_Pass.mockAIEngine.ExecutionEngine;
import com.HOPn.AI_Pass.mockAIEngine.ExecutionResult;
import com.HOPn.AI_Pass.model.Execution;
import com.HOPn.AI_Pass.model.Task;
import com.HOPn.AI_Pass.model.TaskStatus;
import com.HOPn.AI_Pass.model.UserEntity;
import com.HOPn.AI_Pass.repository.ExecutionRepository;
import com.HOPn.AI_Pass.repository.TaskRepository;
import com.HOPn.AI_Pass.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ExecutionRepository executionRepository;
    private final UserRepository userRepository;
    private final ExecutionEngine executionEngine;
    private final ObjectMapper objectMapper;

    public TaskService(TaskRepository taskRepository,
                       ExecutionRepository executionRepository,
                       UserRepository userRepository,
                       ExecutionEngine executionEngine,
                       ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.executionRepository = executionRepository;
        this.userRepository = userRepository;
        this.executionEngine = executionEngine;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TaskResponse createAndExecute(CreateTaskRequest request, String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Task task = new Task();
        task.setTitle(request.title());
        task.setType(request.taskType());
        task.setInputText(request.inputText());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(Instant.now());
        task.setUser(user);

        long start = System.currentTimeMillis();
        ExecutionResult result;
        try {
            result = executionEngine.execute(request.taskType(), request.inputText());
            task.setStatus(TaskStatus.COMPLETED);
        } catch (Exception ex) {
            result = new ExecutionResult("ERROR", 0.0, "Execution failed: " + ex.getMessage());
            task.setStatus(TaskStatus.FAILED);
        }
        long elapsed = System.currentTimeMillis() - start;

        TaskResultDto output = new TaskResultDto(
                task.getStatus().name().toLowerCase(),
                result.decision(),
                result.confidence(),
                result.explanation(),
                Instant.now()
        );

        try {
            task.setOutputJson(objectMapper.writeValueAsString(output));
        } catch (Exception e) {
            task.setOutputJson(null);
        }

        Task saved = taskRepository.save(task);

        Execution execution = new Execution();
        execution.setTask(saved);
        execution.setDecision(result.decision());
        execution.setConfidence(result.confidence());
        execution.setExplanation(result.explanation());
        execution.setExecutionTimeMs(elapsed);
        execution.setExecutedAt(Instant.now());
        executionRepository.save(execution);

        return toResponse(saved, output);
    }

    public List<TaskResponse> getHistory(String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return taskRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponseFromEntity)
                .toList();
    }

    public TaskResponse getById(Long id, String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return toResponseFromEntity(task);
    }

    private TaskResponse toResponse(Task task, TaskResultDto result) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getType(),
                task.getInputText(),
                task.getStatus().name(),
                result,
                task.getCreatedAt()
        );
    }

    private TaskResponse toResponseFromEntity(Task task) {
        TaskResultDto result = null;
        if (task.getOutputJson() != null) {
            try {
                result = objectMapper.readValue(task.getOutputJson(), TaskResultDto.class);
            } catch (Exception ignored) {}
        }
        return toResponse(task, result);
    }
}