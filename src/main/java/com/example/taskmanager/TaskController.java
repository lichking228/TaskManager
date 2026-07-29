package com.example.taskmanager;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskbyId(@PathVariable("id") Long id){
        log.info("Called getTaskbyId");
        try {
            return ResponseEntity.ok(taskService.getTaskById(id));
        } catch (NoSuchElementException e) {
            log.error("Task not found: id={}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(){
        log.info("Called getAllTasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task taskToCreate){
        log.info("Called createTask");
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(taskService.createTask(taskToCreate));
        } catch (IllegalArgumentException e) {
            log.error("Could not create task: taskToCreate={}", taskToCreate, e);
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable("id") Long id, @Valid @RequestBody Task taskToUpdate){
        log.info("Called updateTask id ={}, taskToUpdate={}", id, taskToUpdate);
        try {
            var updated = taskService.updateTask(id, taskToUpdate);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.error("Task not found: id={}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Could not update task: id={}, taskToUpdate={}", id, taskToUpdate, e);
            return ResponseEntity.badRequest().build();
        }
    }
    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id){
        log.info("Called deleteTask: id={}",id);
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e){
            log.error("Task not found: id={}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable("id") Long id, @RequestBody TaskStatus status) {
        log.info("Called updateTaskStatus id={}, status={}", id, status);
        try {
            var updated = taskService.updateTaskStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.error("Task not found: id={}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Could not update task status: id={}, status={}", id, status, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
