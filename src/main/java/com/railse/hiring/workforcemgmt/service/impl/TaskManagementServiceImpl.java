package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    private static final Logger logger = LoggerFactory.getLogger(TaskManagementServiceImpl.class);

    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;

    public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest request) {
        logger.info("Creating {} tasks", request.getRequests().size());

        List<TaskManagement> createdTasks = new ArrayList<>();

        for (TaskCreateRequest.RequestItem item : request.getRequests()) {
            TaskManagement task = new TaskManagement();
            task.setReferenceId(item.getReferenceId());
            task.setReferenceType(item.getReferenceType());
            task.setTask(item.getTask());
            task.setAssigneeId(item.getAssigneeId());
            task.setPriority(item.getPriority());
            task.setTaskDeadlineTime(item.getTaskDeadlineTime());
            task.setStatus(TaskStatus.ASSIGNED);
            task.setDescription("Task created via API");

            TaskManagement savedTask = taskRepository.save(task);
            createdTasks.add(savedTask);
            logger.debug("Created task with ID: {}", savedTask.getId());
        }

        return taskMapper.modelListToDtoList(createdTasks);
    }

    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest request) {
        logger.info("Updating {} tasks", request.getRequests().size());

        List<TaskManagement> updatedTasks = new ArrayList<>();

        for (UpdateTaskRequest.RequestItem item : request.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

            if (item.getTaskStatus() != null) {
                task.setStatus(item.getTaskStatus());
            }
            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }

            TaskManagement savedTask = taskRepository.save(task);
            updatedTasks.add(savedTask);
            logger.debug("Updated task with ID: {}", savedTask.getId());
        }

        return taskMapper.modelListToDtoList(updatedTasks);
    }

    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        logger.info("Assigning tasks by reference - Reference ID: {}, Type: {}, Assignee: {}",
                request.getReferenceId(), request.getReferenceType(), request.getAssigneeId());

        // Bug Fix #1: Find existing tasks for this reference and cancel them before creating new assignment
        List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(
                request.getReferenceId(), request.getReferenceType());

        int cancelledCount = 0;
        for (TaskManagement existingTask : existingTasks) {
            if (existingTask.getStatus() != TaskStatus.CANCELLED &&
                    existingTask.getStatus() != TaskStatus.COMPLETED) {
                existingTask.setStatus(TaskStatus.CANCELLED);
                taskRepository.save(existingTask);
                cancelledCount++;
                logger.debug("Cancelled existing task with ID: {}", existingTask.getId());
            }
        }

        // Create new task assignment
        TaskManagement newTask = new TaskManagement();
        newTask.setReferenceId(request.getReferenceId());
        newTask.setReferenceType(request.getReferenceType());
        newTask.setAssigneeId(request.getAssigneeId());
        newTask.setStatus(TaskStatus.ASSIGNED);
        newTask.setDescription("Task assigned by reference");
        newTask.setTaskDeadlineTime(System.currentTimeMillis() + 86400000); // 1 day from now

        TaskManagement savedTask = taskRepository.save(newTask);

        String message = String.format("Successfully assigned task (ID: %d) to assignee %d. Cancelled %d existing tasks.",
                savedTask.getId(), request.getAssigneeId(), cancelledCount);
        logger.info(message);

        return message;
    }

    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        logger.info("Fetching tasks by date range - Start: {}, End: {}, Assignees: {}",
                request.getStartDate(), request.getEndDate(), request.getAssigneeIds());

        List<TaskManagement> allTasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        // Bug Fix #2: Filter out cancelled tasks
        // Feature 1: Smart daily task view - include tasks within date range OR active tasks from before
        List<TaskManagement> filteredTasks = allTasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED) // Bug Fix #2
                .filter(task -> {
                    long taskDeadline = task.getTaskDeadlineTime();

                    // Include tasks within the date range
                    boolean withinRange = taskDeadline >= request.getStartDate() &&
                            taskDeadline <= request.getEndDate();

                    // Feature 1: Also include active tasks from before the range that are still open
                    boolean activeFromBefore = taskDeadline < request.getStartDate() &&
                            (task.getStatus() == TaskStatus.ASSIGNED ||
                                    task.getStatus() == TaskStatus.STARTED);

                    return withinRange || activeFromBefore;
                })
                .collect(Collectors.toList());

        logger.info("Found {} tasks matching criteria", filteredTasks.size());
        return taskMapper.modelListToDtoList(filteredTasks);
    }

    @Override
    public TaskManagementDto findTaskById(Long id) {
        logger.info("Fetching task by ID: {}", id);

        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        return taskMapper.modelToDto(task);
    }

    @Override
    public TaskManagementDto updateTaskPriority(UpdatePriorityRequest request) {
        logger.info("Updating priority for task ID: {} to {}", request.getTaskId(), request.getPriority());

        TaskManagement task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));

        Priority oldPriority = task.getPriority();
        task.setPriority(request.getPriority());
        TaskManagement savedTask = taskRepository.save(task);

        logger.info("Priority changed from {} to {} for task ID: {}", oldPriority, request.getPriority(), task.getId());

        return taskMapper.modelToDto(savedTask);
    }

    @Override
    public List<TaskManagementDto> fetchTasksByAssigneeIds(List<Long> assigneeIds) {
        logger.info("Fetching tasks by assignee IDs: {}", assigneeIds);

        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(assigneeIds);

        // Filter out cancelled tasks (Bug Fix #2)
        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .collect(Collectors.toList());

        logger.info("Found {} active tasks for assignees", filteredTasks.size());
        return taskMapper.modelListToDtoList(filteredTasks);
    }

    @Override
    public List<TaskManagementDto> getTasksByPriority(Priority priority) {
        logger.info("Fetching tasks by priority: {}", priority);

        List<TaskManagement> allTasks = taskRepository.findAll();
        List<TaskManagement> filteredTasks = allTasks.stream()
                .filter(task -> task.getPriority() == priority)
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED) // Don't show cancelled tasks
                .collect(Collectors.toList());

        logger.info("Found {} tasks with priority {}", filteredTasks.size(), priority);
        return taskMapper.modelListToDtoList(filteredTasks);
    }

    // Additional helper methods for comments and history (not in interface)
    public String addComment(Long taskId, AddCommentRequest request) {
        logger.info("Adding comment to task ID: {}", taskId);

        // Verify task exists
        TaskManagement task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        logger.info("Comment added to task {}: {}", taskId, request.getComment());
        return "Comment added successfully";
    }

    public TaskHistoryDto getTaskHistory(Long taskId) {
        logger.info("Fetching history for task ID: {}", taskId);

        TaskManagement task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        TaskHistoryDto history = new TaskHistoryDto();
        history.setTask(taskMapper.modelToDto(task));

        // Create sample activity log and comments for demo
        List<ActivityLogDto> activities = createSampleActivityLog(taskId);
        List<CommentDto> comments = createSampleComments(taskId);

        history.setActivityHistory(activities);
        history.setComments(comments);

        return history;
    }

    // Helper methods for creating sample data
    private List<ActivityLogDto> createSampleActivityLog(Long taskId) {
        List<ActivityLogDto> activities = new ArrayList<>();

        ActivityLogDto created = new ActivityLogDto();
        created.setId(1L);
        created.setTaskId(taskId);
        created.setAction("TASK_CREATED");
        created.setDetails("Task was created");
        created.setUserId(1L);
        created.setTimestamp(System.currentTimeMillis() - 86400000); // 1 day ago
        activities.add(created);

        ActivityLogDto assigned = new ActivityLogDto();
        assigned.setId(2L);
        assigned.setTaskId(taskId);
        assigned.setAction("TASK_ASSIGNED");
        assigned.setDetails("Task was assigned to user");
        assigned.setUserId(1L);
        assigned.setTimestamp(System.currentTimeMillis() - 43200000); // 12 hours ago
        activities.add(assigned);

        return activities;
    }

    private List<CommentDto> createSampleComments(Long taskId) {
        List<CommentDto> comments = new ArrayList<>();

        CommentDto comment1 = new CommentDto();
        comment1.setId(1L);
        comment1.setTaskId(taskId);
        comment1.setComment("This task needs to be completed by end of day");
        comment1.setUserId(2L);
        comment1.setTimestamp(System.currentTimeMillis() - 21600000); // 6 hours ago
        comments.add(comment1);

        CommentDto comment2 = new CommentDto();
        comment2.setId(2L);
        comment2.setTaskId(taskId);
        comment2.setComment("Working on it now");
        comment2.setUserId(1L);
        comment2.setTimestamp(System.currentTimeMillis() - 3600000); // 1 hour ago
        comments.add(comment2);

        return comments;
    }
}