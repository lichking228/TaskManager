package com.example.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class TaskControllerValidationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        taskRepository.deleteAll();
    }

    @Test
    void createTaskReturnsValidationErrors() throws Exception {
        var invalidJson = """
                {
                  "creatorId": 0,
                  "assignedUserId": -1,
                  "createDateTime": "%s",
                  "deadlineDate": "%s",
                  "priority": null
                }
                """.formatted(
                LocalDateTime.now().plusDays(1),
                LocalDate.now().minusDays(1)
        );

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.creatorId", is("creatorId must be positive")))
                .andExpect(jsonPath("$.errors.assignedUserId", is("assignedUserId must be positive")))
                .andExpect(jsonPath("$.errors.createDateTime", is("createDateTime must not be in the future")))
                .andExpect(jsonPath("$.errors.deadlineDate", is("deadlineDate must not be in the past")))
                .andExpect(jsonPath("$.errors.priority", is("priority is required")));
    }

    @Test
    void createTaskRejectsClientControlledStatus() throws Exception {
        var json = """
                {
                  "creatorId": 1,
                  "assignedUserId": 2,
                  "status": "DONE",
                  "priority": "High"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTaskRejectsDifferentBodyId() throws Exception {
        var task = taskRepository.save(new TaskEntity(
                null,
                1L,
                2L,
                TaskStatus.CREATED,
                LocalDateTime.now(),
                null,
                TaskPriority.Low
        ));

        var json = """
                {
                  "id": %d,
                  "creatorId": 1,
                  "assignedUserId": 2,
                  "status": "IN_PROGRESS",
                  "priority": "Medium"
                }
                """.formatted(task.getId() + 1);

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
