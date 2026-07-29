package com.example.taskmanager;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(this::toTask)
                .orElseThrow(() -> new NoSuchElementException("Not found task id = " + id));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::toTask)
                .toList();
    }

    public Task createTask(Task taskToCreate) {
        validateTask(taskToCreate);
        if (taskToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }
        if (taskToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        var createDateTime = taskToCreate.createDateTime() != null
                ? taskToCreate.createDateTime()
                : LocalDateTime.now();

        var newTask = new TaskEntity(
                null,
                taskToCreate.creatorId(),
                taskToCreate.assignedUserId(),
                TaskStatus.CREATED,
                createDateTime,
                taskToCreate.deadlineDate(),
                taskToCreate.priority()
        );
        return toTask(taskRepository.save(newTask));
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new NoSuchElementException("Not found task id = " + id);
        }
        taskRepository.deleteById(id);
    }

    public Task updateTask(Long id, Task taskToUpdate) {
        validateTask(taskToUpdate);
        if (taskToUpdate.id() != null && !taskToUpdate.id().equals(id)) {
            throw new IllegalArgumentException("Task id in body must match path id");
        }

        var task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Not found task id = " + id));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Illegal status");
        }

        task.setCreatorId(taskToUpdate.creatorId());
        task.setAssignedUserId(taskToUpdate.assignedUserId());
        task.setStatus(taskToUpdate.status() != null ? taskToUpdate.status() : task.getStatus());
        task.setCreateDateTime(taskToUpdate.createDateTime() != null ? taskToUpdate.createDateTime() : task.getCreateDateTime());
        task.setDeadlineDate(taskToUpdate.deadlineDate());
        task.setPriority(taskToUpdate.priority());

        return toTask(taskRepository.save(task));
    }

    public Task updateTaskStatus(Long id, TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status should not be empty");
        }

        var task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Not found task id = " + id));

        if (task.getStatus() == TaskStatus.DONE && status != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Done task can only be returned to IN_PROGRESS");
        }

        task.setStatus(status);
        return toTask(taskRepository.save(task));
    }

    private void validateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task should not be empty");
        }
        if (task.creatorId() == null) {
            throw new IllegalArgumentException("Creator id should not be empty");
        }
        if (task.creatorId() <= 0) {
            throw new IllegalArgumentException("Creator id should be positive");
        }
        if (task.assignedUserId() == null) {
            throw new IllegalArgumentException("Assigned user id should not be empty");
        }
        if (task.assignedUserId() <= 0) {
            throw new IllegalArgumentException("Assigned user id should be positive");
        }
        if (task.createDateTime() != null && task.createDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Create date time should not be in the future");
        }
        if (task.deadlineDate() != null && task.deadlineDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline date should not be in the past");
        }
        if (task.priority() == null) {
            throw new IllegalArgumentException("Priority should not be empty");
        }
    }

    private Task toTask(TaskEntity taskEntity) {
        return new Task(
                taskEntity.getId(),
                taskEntity.getCreatorId(),
                taskEntity.getAssignedUserId(),
                taskEntity.getStatus(),
                taskEntity.getCreateDateTime(),
                taskEntity.getDeadlineDate(),
                taskEntity.getPriority()
        );
    }

}
