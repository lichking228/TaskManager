package com.example.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TaskServiceTests {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void createTaskPersistsTaskWithDefaultStatusAndCreateDateTime() {
        var created = taskService.createTask(new Task(
                null,
                1L,
                2L,
                null,
                null,
                LocalDate.now().plusDays(1),
                TaskPriority.High
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(TaskStatus.CREATED);
        assertThat(created.createDateTime()).isNotNull();

        var loaded = taskService.getTaskById(created.id());
        assertThat(loaded.id()).isEqualTo(created.id());
        assertThat(loaded.creatorId()).isEqualTo(1L);
        assertThat(loaded.assignedUserId()).isEqualTo(2L);
        assertThat(loaded.status()).isEqualTo(TaskStatus.CREATED);
        assertThat(loaded.createDateTime()).isNotNull();
        assertThat(loaded.deadlineDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(loaded.priority()).isEqualTo(TaskPriority.High);
    }

    @Test
    void updateTaskStatusPersistsStatusChange() {
        var created = taskService.createTask(new Task(
                null,
                1L,
                2L,
                null,
                null,
                null,
                TaskPriority.Medium
        ));

        var updated = taskService.updateTaskStatus(created.id(), TaskStatus.IN_PROGRESS);

        assertThat(updated.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(taskService.getTaskById(created.id()).status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void deleteTaskRemovesTask() {
        var created = taskService.createTask(new Task(
                null,
                1L,
                2L,
                null,
                null,
                null,
                TaskPriority.Low
        ));

        taskService.deleteTask(created.id());

        assertThatThrownBy(() -> taskService.getTaskById(created.id()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void createTaskRejectsInvalidData() {
        var task = new Task(
                null,
                0L,
                -2L,
                null,
                LocalDateTime.now().plusDays(1),
                LocalDate.now().minusDays(1),
                null
        );

        assertThatThrownBy(() -> taskService.createTask(task))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateTaskRejectsDifferentBodyId() {
        var created = taskService.createTask(new Task(
                null,
                1L,
                2L,
                null,
                null,
                null,
                TaskPriority.Low
        ));

        var update = new Task(
                created.id() + 1,
                1L,
                2L,
                TaskStatus.IN_PROGRESS,
                null,
                null,
                TaskPriority.Medium
        );

        assertThatThrownBy(() -> taskService.updateTask(created.id(), update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task id in body must match path id");
    }
}
