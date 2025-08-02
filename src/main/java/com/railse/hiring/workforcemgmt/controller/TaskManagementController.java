package com.railse.hiring.workforcemgmt.controller;

import com.railse.hiring.workforcemgmt.common.model.response.Response;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import com.railse.hiring.workforcemgmt.service.impl.TaskManagementServiceImpl;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/task-mgmt")
public class TaskManagementController {

    private static final Logger logger = LoggerFactory.getLogger(TaskManagementController.class);

    private final TaskManagementService taskManagementService;
    private final TaskManagementServiceImpl taskManagementServiceImpl; // For additional methods

    public TaskManagementController(TaskManagementService taskManagementService,
                                    TaskManagementServiceImpl taskManagementServiceImpl) {
        this.taskManagementService = taskManagementService;
        this.taskManagementServiceImpl = taskManagementServiceImpl;
    }

    // Original endpoints matching your existing controller
    @GetMapping("/{id}")
    public Response<TaskManagementDto> getTaskById(@PathVariable Long id) {
        logger.info("Fetching task by id: {}", id);
        return new Response<>(taskManagementService.findTaskById(id));
    }

    @PostMapping("/create")
    public Response<List<TaskManagementDto>> createTasks(@RequestBody TaskCreateRequest request) {
        logger.info("Creating tasks");
        return new Response<>(taskManagementService.createTasks(request));
    }

    @PostMapping("/update")
    public Response<List<TaskManagementDto>> updateTasks(@RequestBody UpdateTaskRequest request) {
        logger.info("Updating tasks");
        return new Response<>(taskManagementService.updateTasks(request));
    }

    @PostMapping("/assign-by-ref")
    public Response<String> assignByReference(@RequestBody AssignByReferenceRequest request) {
        logger.info("Assigning tasks by reference");
        return new Response<>(taskManagementService.assignByReference(request));
    }

    @PostMapping("/fetch-by-date/v2")
    public Response<List<TaskManagementDto>> fetchByDate(@RequestBody TaskFetchByDateRequest request) {
        logger.info("Fetching tasks by date");
        return new Response<>(taskManagementService.fetchTasksByDate(request));
    }

    // New endpoints from your service interface
    @PostMapping("/update-priority")
    public Response<TaskManagementDto> updateTaskPriority(@RequestBody UpdatePriorityRequest request) {
        logger.info("Updating priority for task ID: {} to {}", request.getTaskId(), request.getPriority());
        return new Response<>(taskManagementService.updateTaskPriority(request));
    }

    @GetMapping("/fetch-by-assignees")
    public Response<List<TaskManagementDto>> fetchTasksByAssigneeIds(@RequestParam List<Long> assigneeIds) {
        logger.info("Fetching tasks by assignee IDs: {}", assigneeIds);
        return new Response<>(taskManagementService.fetchTasksByAssigneeIds(assigneeIds));
    }

    @GetMapping("/priority/{priority}")
    public Response<List<TaskManagementDto>> getTasksByPriority(@PathVariable Priority priority) {
        logger.info("Fetching tasks by priority: {}", priority);
        return new Response<>(taskManagementService.getTasksByPriority(priority));
    }

    // Additional feature endpoints (using the impl directly for extra features not in interface)
    @PostMapping("/{taskId}/comments")
    public Response<String> addComment(@PathVariable Long taskId, @RequestBody AddCommentRequest request) {
        logger.info("Adding comment to task ID: {}", taskId);
        return new Response<>(taskManagementServiceImpl.addComment(taskId, request));
    }

    @GetMapping("/{taskId}/history")
    public Response<TaskHistoryDto> getTaskHistory(@PathVariable Long taskId) {
        logger.info("Fetching history for task ID: {}", taskId);
        return new Response<>(taskManagementServiceImpl.getTaskHistory(taskId));
    }
}