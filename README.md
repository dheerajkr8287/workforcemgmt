# Workforce Management API

A Spring Boot REST API for managing tasks, assignments, and workforce operations in a logistics environment.

## 🏗️ Project Overview

The Workforce Management API is a core component of a logistics super-app that helps managers create, assign, and track tasks for employees such as salespeople and operations staff. The system handles task lifecycle management, priority assignment, commenting, and activity tracking.

## 🚀 Features

### Core Functionality
- **Task Management**: Create, update, and retrieve tasks
- **Task Assignment**: Assign tasks to employees by reference
- **Date-based Filtering**: Fetch tasks within specific date ranges
- **Priority Management**: Set and update task priorities (HIGH, MEDIUM, LOW)
- **Task Comments**: Add and retrieve comments on tasks
- **Activity History**: Automatic logging of task events and changes

### Key Endpoints

#### Task Operations
- `GET /task-mgmt/{id}` - Get task by ID
- `POST /task-mgmt/create` - Create new tasks
- `POST /task-mgmt/update` - Update existing tasks
- `POST /task-mgmt/assign-by-ref` - Assign tasks by reference
- `POST /task-mgmt/fetch-by-date/v2` - Fetch tasks by date range

#### Priority Management
- `POST /task-mgmt/update-priority` - Update task priority
- `GET /task-mgmt/priority/{priority}` - Get tasks by priority level

#### Assignee Operations
- `GET /task-mgmt/fetch-by-assignees` - Fetch tasks by assignee IDs

#### Task History & Comments
- `POST /task-mgmt/{taskId}/comments` - Add comment to task
- `GET /task-mgmt/{taskId}/history` - Get complete task history

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.0.4**
- **Gradle** (Build tool)
- **Lombok** (Boilerplate reduction)
- **MapStruct** (Object mapping)
- **In-memory storage** (No external database required)

## 📁 Project Structure

```
src/main/java/com/railse/hiring/workforcemgmt/
├── WorkforcemgmtApplication.java          # Main application class
├── controller/
│   └── TaskManagementController.java      # REST endpoints
├── service/
│   ├── TaskManagementService.java         # Service interface
│   └── impl/
│       └── TaskManagementServiceImpl.java # Service implementation
├── repository/
│   ├── TaskRepository.java                # Repository interface
│   └── InMemoryTaskRepository.java        # In-memory implementation
├── model/
│   ├── TaskManagement.java                # Task entity
│   └── enums/
│       ├── Priority.java                  # Priority levels
│       ├── Task.java                      # Task types
│       └── TaskStatus.java                # Task statuses
├── dto/
│   ├── TaskManagementDto.java             # Task DTO
│   ├── TaskCreateRequest.java             # Create request
│   ├── UpdateTaskRequest.java             # Update request
│   ├── TaskFetchByDateRequest.java        # Date filter request
│   ├── UpdatePriorityRequest.java         # Priority update request
│   ├── AddCommentRequest.java             # Comment request
│   ├── TaskHistoryDto.java                # Task history response
│   ├── ActivityLogDto.java                # Activity log entry
│   └── CommentDto.java                    # Comment DTO
├── mapper/
│   └── ITaskManagementMapper.java         # MapStruct mapper
└── common/
    ├── exception/
    │   ├── CustomExceptionHandler.java    # Global exception handler
    │   ├── ResourceNotFoundException.java # Custom exception
    │   └── StatusCode.java                # Status codes
    └── model/
        ├── response/
        │   ├── Response.java              # API response wrapper
        │   └── ResponseStatus.java        # Response status
        └── enums/
            └── ReferenceType.java         # Reference types
```

## 🚦 Getting Started

### Prerequisites
- Java 17 or higher
- Gradle 7.0 or higher

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <your-repository-url>
   cd workforce-management-api
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the API**
   - Base URL: `http://localhost:8080`
   - All endpoints are prefixed with `/task-mgmt`

### Sample Data

The application comes pre-loaded with seed data for testing:
- Tasks with different priorities (HIGH, MEDIUM, LOW)
- Various task types (CREATE_INVOICE, ARRANGE_PICKUP, etc.)
- Multiple assignees and reference types

## 📝 API Usage Examples

### Create Tasks
```bash
POST /task-mgmt/create
Content-Type: application/json

{
  "requests": [
    {
      "reference_id": 104,
      "reference_type": "ORDER",
      "task": "CREATE_INVOICE",
      "assignee_id": 1,
      "priority": "HIGH",
      "task_deadline_time": 1691234567890
    }
  ]
}
```

### Fetch Tasks by Date Range
```bash
POST /task-mgmt/fetch-by-date/v2
Content-Type: application/json

{
  "start_date": 1691200000000,
  "end_date": 1691286400000,
  "assignee_ids": [1, 2]
}
```

### Update Task Priority
```bash
POST /task-mgmt/update-priority
Content-Type: application/json

{
  "task_id": 1,
  "priority": "HIGH"
}
```

### Add Comment to Task
```bash
POST /task-mgmt/1/comments
Content-Type: application/json

{
  "comment": "Task is progressing well",
  "user_id": 1
}
```

## 🐛 Bug Fixes Implemented

### Bug #1: Task Re-assignment Creates Duplicates
**Problem**: When reassigning tasks using assign-by-reference, old tasks weren't being cancelled, creating duplicates.

**Solution**: Modified the assignment logic to automatically cancel existing tasks for the same reference before creating new assignments.

### Bug #2: Cancelled Tasks Clutter the View
**Problem**: Date-based task fetching included cancelled tasks, cluttering the view.

**Solution**: Updated the fetch logic to filter out CANCELLED tasks by default.

## ✨ New Features Implemented

### Feature #1: Smart Daily Task View
Enhanced date-based filtering to show:
- All active tasks that started within the date range
- All active tasks that started before the range but are still open

### Feature #2: Task Priority Management
- Added priority field to task model (HIGH, MEDIUM, LOW)
- Endpoint to update task priority: `POST /task-mgmt/update-priority`
- Endpoint to fetch tasks by priority: `GET /task-mgmt/priority/{priority}`

### Feature #3: Task Comments & Activity History
- Automatic activity logging for task events
- User comments system
- Complete history view: `GET /task-mgmt/{taskId}/history`

## 🧪 Testing

The application includes comprehensive seed data for testing all functionality. Use tools like Postman, Insomnia, or curl to test the endpoints.

### Key Test Scenarios
1. Create tasks and verify assignment logic
2. Test priority updates and filtering
3. Add comments and verify history tracking
4. Test date-based filtering with various ranges
5. Verify cancelled task filtering

## 🔧 Configuration

The application uses default Spring Boot configuration. Key settings:
- Server port: 8080
- In-memory data storage (no external database)
- JSON snake_case naming strategy for API responses

## 📊 Response Format

All API responses follow a consistent format:
```json
{
  "data": { /* response data */ },
  "pagination": null,
  "status": {
    "code": 200,
    "message": "Success"
  }
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is part of a coding challenge and is for evaluation purposes.

## 📞 Support

For questions or issues, please refer to the submission guidelines or contact the development team.
