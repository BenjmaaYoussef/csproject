# Phase 7 – GUI (Swing) Testing Guide

## Overview

Phase 7 replaces the console menu with a Swing GUI. The entry point is `gui.WorkoutTrackerGUI`. The original console `Main` still works unchanged. The GUI covers all concepts from Phases 1–6.

---

## Project Structure (Phase 7 additions)

```
src/gui/
  AppColors.java           – shared color palette and fonts (constants only)
  ProfileSetupDialog.java  – modal JDialog for first-run profile entry
  WorkoutTrackerGUI.java   – main JFrame, 5-tab JTabbedPane, entry point
  DashboardPanel.java      – stat cards + bar chart (exercises per session)
  AddWorkoutPanel.java     – form to log a new WorkoutSession with exercises
  HistoryPanel.java        – JTable of all sessions + detail pane
  ProfilePanel.java        – user info display + BMI semicircle gauge
  DataPanel.java           – all file/DB/network operations (Phases 3–6)
```

---

## How to Run

### From IntelliJ IDEA
- Set Main class to `gui.WorkoutTrackerGUI`
- Run (no extra classpath needed beyond the MySQL connector JAR already configured)

### From VS Code
- Open `src/gui/WorkoutTrackerGUI.java`
- Click the Run ▶ button above the `main` method
- Requires `.vscode/settings.json` (already present): `{ "java.project.sourcePaths": ["src"] }`

### From terminal (project root)
```bash
javac -cp "out/production/csproject" -sourcepath src -d out/production/csproject $(find src -name "*.java")
java -cp out/production/csproject gui.WorkoutTrackerGUI
```

> **macOS note:** Look & Feel is set to Metal (cross-platform) so button colors render correctly. Do NOT switch to the system L&F or button text becomes invisible.

---

## Startup – Profile Setup Dialog

On launch, a modal `ProfileSetupDialog` appears before the main window.

**Fields:** Name, Age, Weight (kg), Height (cm)

**Test cases:**
| Scenario | Expected |
|---|---|
| Leave Name blank, click Start | Name field border turns red, dialog stays open |
| Enter non-numeric Age/Weight/Height | Age field border turns red, dialog stays open |
| Fill all fields correctly, click Start | Dialog closes, main window opens with "Welcome, \<name\> \| BMI \<value\>" in header |

---

## Tab 1 – Dashboard

**What it shows:**
- 3 stat cards: Total Sessions, Total Exercises, Personal Records
- Bar chart: one bar per session, height = number of exercises, color = majority exercise type (blue=STRENGTH, green=CARDIO, yellow=FLEXIBILITY)

**Test cases:**
| Scenario | Expected |
|---|---|
| Open app with no saved data | All cards show 0, chart shows "No sessions logged yet." |
| Add sessions via Add Workout tab, switch back to Dashboard | Cards and chart update to reflect new data |
| Sessions with mixed exercise types | Bar color reflects the dominant type |

**Phase coverage:** Phase 2 (Collections – `getAllSessions()`, `getAllPersonalRecords()`)

---

## Tab 2 – Add Workout

**Left card:** Session date (yyyy-MM-dd) + notes textarea + "Save Session" button

**Right card:** Exercise form (Name, Type dropdown, conditional fields) + staged exercise table + "Remove Selected" button

**Type dropdown logic:**
- STRENGTH / FLEXIBILITY → Sets, Reps, Weight fields enabled; Duration disabled
- CARDIO → Duration field enabled; Sets, Reps, Weight disabled

**Test cases:**
| Scenario | Expected |
|---|---|
| Click "+ Add Exercise" with blank name | `InvalidExerciseException` shown in JOptionPane |
| Click "+ Add Exercise" with negative sets | `InvalidExerciseException` shown in JOptionPane |
| Add same exercise name twice to staged list | `DuplicateExerciseException` shown in JOptionPane on Save |
| Click "Save Session" with no date | Warning dialog: "Date is required." |
| Fill date + add 2 exercises + Save | JOptionPane confirms "Session for \<date\> saved with 2 exercise(s)." · session appears in History tab · `workouts.bin` updated |
| Select a row in staged table + click "Remove Selected" | Row removed from staged list |

**Phase coverage:** Phase 1 (Exceptions), Phase 2 (Collections), Phase 4 (binary auto-save via `FileManager.saveBinary()`)

---

## Tab 3 – History

**What it shows:**
- `JTable` with columns: Date, Exercises (count), Notes — sorted chronologically
- Detail pane below: click a row to see full session with all exercises

**Test cases:**
| Scenario | Expected |
|---|---|
| No sessions logged | Table is empty, detail pane shows "Select a session to see details." |
| Sessions exist | Rows sorted by date ascending |
| Click a row | Detail pane shows date, notes, numbered exercise list |
| Add a new session in Add Workout, come back to History | New row appears |

**Phase coverage:** Phase 2 (Collections – `getSessionsSortedByDate()`)

---

## Tab 4 – Profile

**What it shows:**
- Left card: Name, Age, Weight, Height, BMI value + category label
- Right card: Semicircle BMI gauge drawn with `Graphics2D` arcs and a needle

**BMI categories:**
| BMI | Label | Color |
|---|---|---|
| < 18.5 | Underweight | Yellow |
| 18.5 – 25 | Normal | Green |
| 25 – 30 | Overweight | Yellow |
| ≥ 30 | Obese | Red |

**Test cases:**
| Scenario | Expected |
|---|---|
| Profile entered on startup | All fields populated, needle points to correct BMI zone |
| BMI = 22 (normal) | Category label shows "Normal" in green |
| BMI = 27 | Category label shows "Overweight" in yellow |

**Phase coverage:** Phase 1 (User model)

---

## Tab 5 – Data & Sync

This tab exposes all I/O operations from Phases 3–6. Results are printed to the **Output Log** panel at the bottom.

### Phase 3 – File (Text)

| Button | Action | Test |
|---|---|---|
| Save to TXT | Calls `FileManager.saveSessions()` → writes `workouts.txt` | Check `workouts.txt` exists and contains SESSION/EXERCISE lines |
| Load from TXT | Calls `FileManager.loadSessions()` → adds sessions to manager | Add sessions, save, restart app (binary loads), load TXT → no duplicates if same data |
| Export Report | Calls `FileManager.exportReport()` → writes `report.txt` | Check `report.txt` contains user name, summary, and session details |

### Phase 4 – Serialization

| Button | Action | Test |
|---|---|---|
| Save Binary | Calls `FileManager.saveBinary()` → writes `workouts.bin` | File appears/updates; re-launching GUI auto-loads from this file |
| Load Binary | Calls `FileManager.loadBinary()` → adds sessions to manager | Sessions appear in History and Dashboard after load |
| Export XML | Calls `FileManager.exportXML()` → writes `workouts.xml` | `workouts.xml` contains XMLEncoder-format session data |

### Phase 5 – Database

> **Prerequisite:** MySQL running, `workout_tracker.sql` executed, credentials in `WorkoutSessionDAO` match your setup (`root` / password).

| Button | Action | Test |
|---|---|---|
| Save to DB | Calls `WorkoutSessionDAO.saveSession()` for all sessions | Output log shows count; verify in MySQL: `SELECT * FROM workout_sessions` |
| Load from DB | Calls `WorkoutSessionDAO.getAllSessions()` | Sessions added to manager, log shows count |
| Delete from DB | Shows input dialog for date → calls `WorkoutSessionDAO.deleteSession()` | Enter a valid date → log shows "Deleted"; enter invalid date → log shows "No session found" |

### Phase 6 – Client-Server

> **Prerequisite:** `WorkoutServer` must be running: `java -cp out/production/csproject network.WorkoutServer`

| Button | Action | Test |
|---|---|---|
| Fetch from Server | Sends `GET_SESSIONS` via `WorkoutClient.sendAndReceive()` | Server response appears in output log |
| Send to Server | Asks for a date, finds that session in manager, sends `ADD_SESSION:...` | Server response appears in output log; if server is not running, IOException is caught and printed |

---

## Cross-Phase Integration

| What to verify | How |
|---|---|
| Save session in GUI → appears in console `Main` | Run `Main`, choose "Load sessions from binary" equivalent |
| Binary file persists across GUI restarts | Close and reopen `WorkoutTrackerGUI` — sessions reload automatically |
| Exception handling visible in GUI | Try invalid exercise data in Add Workout tab |
| All 6 phase concepts reachable from one window | Navigate through all 5 tabs |

---

## Files Created / Modified in Phase 7

| File | Status |
|---|---|
| `src/gui/AppColors.java` | NEW |
| `src/gui/ProfileSetupDialog.java` | NEW |
| `src/gui/WorkoutTrackerGUI.java` | NEW |
| `src/gui/DashboardPanel.java` | NEW |
| `src/gui/AddWorkoutPanel.java` | NEW |
| `src/gui/HistoryPanel.java` | NEW |
| `src/gui/ProfilePanel.java` | NEW |
| `src/gui/DataPanel.java` | NEW |
| `.vscode/settings.json` | NEW – fixes VS Code source root resolution |
| `docs/PLAN.md` | unchanged |
| `src/Main.java` | unchanged |
| All Phase 1–6 source files | unchanged |

---

## Known Limitations

- **DB operations are synchronous on the EDT** — if MySQL is slow or unreachable, the GUI will briefly freeze. This is intentional to match the professor's style (no SwingWorker/threads).
- **Server operations** print to `System.out` internally; the GUI captures stdout temporarily to redirect output to the log panel.
- **Duplicate sessions** are not prevented when loading from file/DB into an already-populated manager — same behavior as the console `Main`.
