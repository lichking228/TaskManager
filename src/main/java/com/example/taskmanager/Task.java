package com.example.taskmanager;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Task(
        Long id,

        @NotNull(message = "creatorId is required")
        @Positive(message = "creatorId must be positive")
        Long creatorId,

        @NotNull(message = "assignedUserId is required")
        @Positive(message = "assignedUserId must be positive")
        Long assignedUserId,

        TaskStatus status,

        @PastOrPresent(message = "createDateTime must not be in the future")
        LocalDateTime createDateTime,

        @FutureOrPresent(message = "deadlineDate must not be in the past")
        LocalDate deadlineDate,

        @NotNull(message = "priority is required")
        TaskPriority priority
) {
}
