package com.example.taskmanager;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {
    private final Map<Long, Task> tasks;

    private final AtomicLong idCounter;

    public TaskService() {
        tasks = new HashMap<>();
        idCounter = new AtomicLong();
    }

    public Task getTaskById(Long id) {
        if (!tasks.containsKey(id)) throw new NoSuchElementException("Not found task id = " + id);
        return tasks.get(id);
    }

    public List<Task> getAllTasks() {
        return tasks.values().stream().toList();
    }

    public Task createTask(Task taskToCreate) {
        if (taskToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }
        if (taskToCreate.status() != null) {
            throw new IllegalArgumentException("Status shoud be empty");
        }
        var newTask = new Task(
                idCounter.incrementAndGet(),
                taskToCreate.creatorId(),
                taskToCreate.assignedUserId(),
                TaskStatus.CREATED,
                taskToCreate.createDateTime(),
                taskToCreate.deadlineDate(),
                taskToCreate.priority()
        );
        tasks.put(newTask.id(), newTask);
        return newTask;
    }

    public void deleteTask(Long id) {
        if (!tasks.containsKey(id)) {
            throw new NoSuchElementException("Not found task id = " + id);
        }
        tasks.remove(id);
    }

    public Task updateTask(Long id, Task taskToUpdate) {
        if (!tasks.containsKey(id)) {
            throw new NoSuchElementException("Not found task id = " + id);
        }
        var task = tasks.get(id);
        if (task.status() == TaskStatus.DONE) {
            throw new IllegalStateException("Illegal status");
        }
        var updatedTask = new Task(
                task.id(),
                taskToUpdate.creatorId(),
                taskToUpdate.assignedUserId(),
                taskToUpdate.status(),
                taskToUpdate.createDateTime(),
                taskToUpdate.deadlineDate(),
                taskToUpdate.priority()
        );
        tasks.put(task.id(), updatedTask);
        return updatedTask;
    }

    public Task updateTaskStatus(Long id, TaskStatus status) {
        if (!tasks.containsKey(id)) {
            throw new NoSuchElementException("Not found task id = " + id);
        }
        if (status == null) {
            throw new IllegalArgumentException("Status should not be empty");
        }

        var task = tasks.get(id);

        if (task.status() == TaskStatus.DONE && status != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Done task can only be returned to IN_PROGRESS");
        }

        var updatedTask = new Task(
                task.id(),
                task.creatorId(),
                task.assignedUserId(),
                status,
                task.createDateTime(),
                task.deadlineDate(),
                task.priority()
        );

        tasks.put(id, updatedTask);
        return updatedTask;
    }
}

