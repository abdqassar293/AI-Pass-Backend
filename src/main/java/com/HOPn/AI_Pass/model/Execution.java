package com.HOPn.AI_Pass.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "executions")
@Getter
@Setter
public class Execution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(nullable = false)
    private String decision;

    @Column(nullable = false)
    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private Long executionTimeMs;

    @Column(nullable = false)
    private Instant executedAt;
}