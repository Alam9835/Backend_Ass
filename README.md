

# Backend Assignment

## Overview
This project is a backend service that tracks every change made to a Project entity over time.
Instead of overwriting data on updates, each change is stored as an audit event.  
This allows the system to answer questions like:
- What changed?
- When did it change?
- What did the project look like at a specific time in the past?

The implementation focuses on correctness, simplicity, and clarity rather than over-engineering.

---

## Tech Stack
- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Jackson (for JSON handling)

---

## Data Model and Design Choice

The system uses **two tables**, each with a clear responsibility.

### Project (Current State)
Stores only the latest version of a project.

**Fields:**
- `id` – unique project identifier
- `data` – dynamic project data stored as JSON (TEXT)
- `deleted` – soft delete flag

**Why this design was chosen:**
- Fast access to the current state
- Keeps the table small and easy to manage
- Historical data is intentionally excluded from this table

---

### AuditEvent (Change History)
Stores every change made to a project.

**Fields:**
- `entityId` – project identifier
- `action` – CREATE / UPDATE / DELETE
- `timestamp` – time when the change occurred
- `diff` – JSON containing only changed fields

**Why this design was chosen:**
- Append-only table ensures no data loss
- Each change is preserved clearly
- Enables accurate reconstruction of past states

---

## How Diffs Are Computed

On every update request, the incoming project data is compared against the currently stored project state.

- Only fields whose values have changed are recorded.
- Unchanged fields are ignored to keep audit logs minimal and meaningful.
- Each change is stored as a field-level diff containing both the old and new values.

### Example

**Existing project state:**
```json
{
  "price": 100,
  "status": "draft"
}
```

**Incoming update:**
```json
{
  "price": 120
}
```

**Stored diff event:**
```json
{
  "price": {
    "old": 100,
    "new": 120
  }
}
```

---

## How State Reconstruction Works

To reconstruct a project’s state at a specific point in time, the system replays audit events in chronological order.

### Reconstruction Steps

1. Fetch all audit events for the given project, ordered by timestamp.
2. Initialize an empty project state.
3. Sequentially apply each event’s diff:
   - For each field in the diff, apply the `"new"` value to the state.
4. Stop processing once an event timestamp exceeds the requested point in time.
5. Return the reconstructed state.

### Guarantees

This process correctly handles:
- Field updates
- Newly added fields
- Historical accuracy of project state

The final reconstructed state represents exactly how the project looked at that moment.

---

## System Behavior at Scale

### Write Operations
- Fast due to append-only audit logs.
- No updates or deletes on historical data.

### Current State Reads
- Fast, as only the `projects` table is queried.
- Audit logs are not involved for normal reads.

### State Reconstruction
- Linear with respect to the number of audit events.
- Performance may degrade for projects with very large histories.

### Scalability Considerations

In a production system, reconstruction performance could be improved using:
- Periodic snapshots
- Caching reconstructed states

These optimizations were intentionally omitted to keep the solution simple and aligned with assignment constraints.


## Design Decision I Am Not Fully Satisfied With

Project state reconstruction is currently performed by replaying all audit events from the beginning of the project’s history.

While this approach is:
- Simple to implement
- Easy to reason about
- Correct and deterministic

it does **not scale optimally** for projects with very long change histories, as reconstruction time grows linearly with the number of audit events.

### Possible Improvement

A potential optimization would be to introduce **periodic snapshots** of the project state.  
State reconstruction could then start from the latest snapshot before the requested timestamp instead of replaying all events from the beginning.

This optimization was intentionally **not implemented** to keep the design straightforward and fully aligned with the assignment constraints.

---

## API Endpoints

### Create Project

Creates a new project with the initial state.

**Endpoint**
```
POST /projects/{id}
```

**Request Body**
```json
{
  "price": 100,
  "status": "draft"
}
```

---

### Update Project (Partial Update / Add New Fields)

Updates one or more fields of an existing project.  
Only changed or newly added fields are recorded as diffs.

**Endpoint**
```
PATCH /projects/{id}
```

**Request Body**
```json
{
  "price": 120,
  "category": "electronics"
}
```

---

### Fetch Full Change History

Returns the complete audit history for a project in chronological order.

**Endpoint**
```
GET /projects/{id}/history
```

---

### Reconstruct Project State at a Given Time

Reconstructs and returns the project state as it existed at a specific point in time.

**Endpoint**
```
GET /projects/{id}/state?at=2025-12-30T16:10:00Z
```

**Description**
- The `at` query parameter is an ISO-8601 timestamp.
- All audit events up to (and including) this timestamp are applied.
- The response represents the exact historical state of the project at that moment.


## Instructions to Run the Project Locally

Follow the steps below to run the project on your local machine.

---

### 1. Prerequisites

Make sure the following software is installed on your system:

- **Java 17 or higher**
  ```bash (To check Version)
  java -version
  ```

- **Maven**
  ```bash (To check Version)
  mvn -version
  ```

- **PostgreSQL**
  - PostgreSQL server should be running
  - Default port: `5432`

---

### 2. Clone the Repository

Clone the project from GitHub:

```bash
git clone <your-github-repo-url>
cd backendassignment
```

---

### 3. Create PostgreSQL Database

Login to PostgreSQL using `psql` or pgAdmin and create a database:

```sql
CREATE DATABASE audit_db;
```

> You can use any database name, but ensure it matches the configuration in the next step.

---

### 4. Configure Application Properties

Open the following file:

```
src/main/resources/application.properties
```

Update it with your PostgreSQL credentials:

```properties
spring.application.name=backendassignment

spring.datasource.url=jdbc:postgresql://localhost:5432/audit_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

> **Note:** Replace the username and password if your PostgreSQL credentials are different.

---

### 5. Build the Project

From the project root directory, run:

```bash
mvn clean install
```

This command downloads dependencies and compiles the project.

---

### 6. Run the Application

Start the Spring Boot application using:

```bash
mvn spring-boot:run
```

Alternatively, you can run the main class directly:

```
BackendAssignmentApplication.java
```

---

### 7. Verify Application Startup

If the application starts successfully, you should see logs similar to:

```
Tomcat started on port 8080
Started BackendAssignmentApplication
```

This confirms the backend is running successfully.

---

### 8. Test APIs Using Postman

You can use **Postman** or any REST client to test the APIs.

#### Create a Project
```http
POST http://localhost:8080/projects/P1
```

**Request Body**
```json
{
  "price": 100,
  "status": "draft"
}
```

---

#### Update Project (Partial Update / Add New Fields)
```http
PATCH http://localhost:8080/projects/P1
```

**Request Body**
```json
{
  "price": 120,
  "category": "electronics"
}
```

---

#### Fetch Full Change History
```http
GET http://localhost:8080/projects/P1/history
```

---

#### Reconstruct Project State at a Given Time
```http
GET http://localhost:8080/projects/P1/state?at=2025-12-30T16:10:00Z
```

---

### 9. Verify Data in Database (Optional)

You can verify stored data directly in PostgreSQL:

```sql
SELECT * FROM projects;
SELECT * FROM audit_events;
```

This helps confirm that both the current state and the audit history are stored correctly.

---

### 10. Stop the Application

To stop the application, press:

```
Ctrl + C
```
## Sample API Outputs (Postman)

Below are sample responses captured from Postman to demonstrate the working of the APIs.
![11](https://github.com/user-attachments/assets/6278d7cd-03eb-4cd5-a74e-011dc71f2796)
![22](https://github.com/user-attachments/assets/c4c979d5-5141-41f6-9148-3e8d3efec831)
![33](https://github.com/user-attachments/assets/baf99828-6867-4228-bcde-c40c3552f40f)
![44](https://github.com/user-attachments/assets/72ad3b47-1392-4c1f-ad27-5986016d258d)
![55](https://github.com/user-attachments/assets/fb057889-ce4d-4081-98ec-fe995053f0dd)



