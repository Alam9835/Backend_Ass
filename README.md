

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
