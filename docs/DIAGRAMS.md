# Presentation Diagrams — Mermaid Source

Each diagram is labelled with the slide it belongs to.
Paste the code block content into any Mermaid renderer (mermaid.live, Notion, Obsidian, etc.).

---

## Diagram 1 — Architecture Stack (Slide 1)

```mermaid
flowchart TD
    subgraph UI["User Interface"]
        direction LR
        CLI["CLI<br>Main.java<br>(main project)"]
        GUI["GUI<br>WorkoutTrackerGUI.java<br>(bonus)"]
    end

    WM["WorkoutManager + Collections<br>per-user in-memory state"]

    FM["FileManager<br>.txt  ·  .bin  ·  .xml  ·  report"]

    subgraph MODES["Persistence Layer"]
        direction LR
        DAO["WorkoutSessionDAO<br>Direct DB — JDBC"]
        NET["WorkoutServer / WorkoutClient<br>Via Server — TCP port 9876"]
    end

    DB[("MySQL")]

    UI --> WM
    WM --> FM
    WM -->|"Direct DB mode"| DAO
    WM -->|"Via Server mode"| NET
    DAO --> DB
    NET --> DB
    FM <-.->|"offline fallback"| DAO
    FM <-.->|"offline fallback"| NET
```

---

## Diagram 2a — Startup Flow: Identity & Connection (Slide 2, top)

```mermaid
flowchart LR
    A["Pre-login<br>report?"]
    B["Select<br>profile"]
    C["Choose<br>connection mode"]

    A -->|"yes: read report_name.txt<br>no: skip"| B
    B -->|"load users.bin<br>or create new profile"| C
```

---

## Diagram 2b — Startup Flow: Data & Menu (Slide 2, bottom)

```mermaid
flowchart LR
    D["Auto-load<br>sessions"]
    E["Main<br>menu"]

    D -->|"if .bin newer than sync timestamp<br>push offline changes first"| E
```

---

## Diagram 3 — Exception Hierarchy (Slide 3)

```mermaid
classDiagram
    class Exception {
        <<Java built-in>>
    }

    class WorkoutAppException {
        <<checked>>
        +WorkoutAppException(String message)
        +WorkoutAppException(String message, Throwable cause)
    }

    class InvalidExerciseException {
        +InvalidExerciseException(String message)
    }

    class WorkoutNotFoundException {
        +WorkoutNotFoundException(String date)
    }

    class DuplicateExerciseException {
        +DuplicateExerciseException(String exerciseName)
    }

    Exception <|-- WorkoutAppException
    WorkoutAppException <|-- InvalidExerciseException
    WorkoutAppException <|-- WorkoutNotFoundException
    WorkoutAppException <|-- DuplicateExerciseException
```
