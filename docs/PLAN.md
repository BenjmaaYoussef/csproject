# Workout App – CS202 Project Plan

## Overview
A console-based Workout Tracker app built progressively using each CS202 chapter concept.
Users can manage exercises, log workout sessions, and track their fitness progress.

---

## Phases

### Phase 1 – Core Model + Exceptions (Chapter 2)
**Goal:** Define all data classes and custom exceptions. Build a working console menu in Main.java.

Classes:
- `ExerciseType` (enum) — STRENGTH, CARDIO, FLEXIBILITY
- `Exercise` — name, sets, reps, weightKg, durationMin, type
- `WorkoutSession` — date, list of exercises, notes
- `User` — name, age, weightKg, heightCm

Exceptions:
- `InvalidExerciseException` — thrown when exercise data is invalid (negative values, blank name)
- `WorkoutNotFoundException` — thrown when a session by date is not found
- `DuplicateExerciseException` — thrown when adding a duplicate exercise to a session

Entry point:
- `Main.java` — console menu: manage users, add exercises, log sessions, view history

---

### Phase 2 – Collections & Generics (Chapter 4)
**Goal:** Replace raw arrays with proper Java Collections. Add generic utility classes.

- `WorkoutManager` uses `ArrayList<WorkoutSession>` and `HashMap<String, Exercise>`
- Generic `Pair<A,B>` utility for returning stats
- Sort sessions by date, filter exercises by type
- Personal records tracking with `TreeMap<String, Double>` (exercise → best weight)

---

### Phase 3 – File Handling (Chapter 3)
**Goal:** Persist data to plain text files using BufferedReader/Writer.

- Save workout sessions to `workouts.txt` (CSV-style)
- Load sessions on startup
- Export workout history to a readable report file

---

### Phase 4 – Serialization (Chapter 5)
**Goal:** Replace text files with binary serialization and add XML export.

- `WorkoutSession` implements `Serializable`
- Save/load sessions using `ObjectOutputStream` / `ObjectInputStream`
- Export to XML using DOM (DocumentBuilder)

---

### Phase 5 – Database (Chapter 6)
**Goal:** Store all data in a MySQL database using JDBC (MySQL Connector/J 9.x).

Tables: `workout_sessions`, `session_exercises`
- `DatabaseUtility` class: static `getConnection()` loads `com.mysql.cj.jdbc.Driver` via `Class.forName`
- `WorkoutSessionDAO` class: static URL/USER_NAME/PASSWORD, full CRUD via `PreparedStatement`
- Transactions (`setAutoCommit(false)` / `commit()` / `rollback()`) for session saves
- Resources closed explicitly in `finally` blocks (ResultSet → Statement → Connection)
- SQL script: `workout_tracker.sql` creates the database and tables

---

### Phase 6 – Client-Server (Chapter 7)
**Goal:** Allow a client to connect to a server and sync workout data.

- `WorkoutServer` — listens on a port, accepts client connections, serves session data
- `WorkoutClient` — connects, sends commands (GET_SESSIONS, ADD_SESSION), receives responses
- Protocol: simple text-based command/response over TCP sockets

---

### Phase 7 – GUI / Bonus (Chapter 8 / Swing)
**Goal:** Replace the console menu with a Swing GUI for full bonus marks.

- Main window with tabbed pane: Dashboard, Add Workout, History, Profile
- JTable to display workout sessions
- JChart or custom drawing for progress visualization
