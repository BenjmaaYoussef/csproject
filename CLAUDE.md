# CLAUDE.md – Workout Tracker (CS202 Project)

## Working directory constraint
Only work inside `C:\Users\garou\csproject`. Do not create, edit, or delete files outside this directory.

## Before implementing any phase
1. Read `docs/PLAN.md` to confirm which phase comes next and what it requires.
2. Read the corresponding chapter slide PDF in `slides/` for that phase.
3. Match the professor's coding style exactly (see Style Rules below).
4. Only then write code.

## Phase → Slide mapping
| Phase | Chapter | Slide file |
|-------|---------|------------|
| 1 | Chapter 2 – Exceptions | `CS202 - Chapter 2 Exceptions.pdf` |
| 2 | Chapter 4 – Collections & Generics | `CS202 - Chapter 4 - Java Collections and Generics.pdf` |
| 3 | Chapter 3 – File Handling | `CS202 - Chapter 3 - File Handling.pdf` |
| 4 | Chapter 5 – Serialization | `CS202 - Chapter 5 - Serialization.pdf` |
| 5 | Chapter 6 – Database | `CS202 - Chapter 6 - Database Connection.pdf` |
| 6 | Chapter 7 – Client-Server | `CS202 - Chapter 7 - CS Communication.pdf` |

## Professor's coding style rules
These are derived directly from the slide examples and must be followed:

- **Imports**: Use specific imports matching what the slides show (e.g. `import java.io.FileWriter;`, not `import java.io.*;` unless the slides use the wildcard).
- **Error handling**: Use `try { } catch (IOException e) { System.err.println("An error occurred."); e.printStackTrace(); }` — exactly this pattern.
- **Resource closing**: Call `.close()` inside the `try` block after use (professor's style from Examples 3 & 4). Use `finally` with null checks only when the slides explicitly show it.
- **No try-with-resources**: The professor's examples do not use try-with-resources (`try (FileWriter w = ...)`), so don't use it.
- **No lambdas / streams API**: Unless the chapter slides introduce them.
- **No modern Java features** beyond what appears in the slides (no records, no var, no pattern matching).
- **Console output**: Use `System.out.println` for normal output and `System.err.println` for errors, matching the professor's examples.

## Project structure
```
src/
  Main.java                   – entry point, console menu
  model/
    Exercise.java
    ExerciseType.java         – enum
    WorkoutSession.java
    WorkoutManager.java       – Phase 2 full manager (used by Main)
    User.java
  manager/
    WorkoutManager.java       – simpler manager (not used by Main)
  exception/
    WorkoutAppException.java
    InvalidExerciseException.java
    WorkoutNotFoundException.java
    DuplicateExerciseException.java
  util/
    Pair.java
    OrderedPair.java
    FileManager.java          – Phase 3: FileWriter + Scanner(File)
  db/
    DatabaseUtility.java      – Phase 5: static getConnection() via Class.forName
    WorkoutSessionDAO.java    – Phase 5: CRUD for workout_sessions + session_exercises
```

## Current progress
- Phase 1 (Exceptions + Models): DONE
- Phase 2 (Collections & Generics): DONE
- Phase 3 (File Handling): DONE — `FileManager.java` handles save/load/export
- Phase 4 (Serialization): DONE — binary via ObjectOutputStream/ObjectInputStream (workouts.bin), XML via XMLEncoder (workouts.xml)
- Phase 5 (Database): DONE — MySQL via JDBC; `DatabaseUtility` + `WorkoutSessionDAO` in `src/db/`; run `workout_tracker.sql` first; driver: `mysql-connector-j-9.6.0/mysql-connector-j-9.6.0.jar`
- Phase 6 (Client-Server): TODO

## Data files (Phase 3)
- `workouts.txt` — pipe-delimited session data, written by `FileManager.saveSessions()`, read by `FileManager.loadSessions()`
- `report.txt` — human-readable export, written by `FileManager.exportReport()`
