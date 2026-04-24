# Phase B — Connection Mode Toggle + Auto Load/Save
**Goal:** Add the persistence mode toggle at login, replace manual load/save with automatic
load-on-startup and save-on-change, and keep `.bin` always up to date.

Depends on Phase A being complete (dead buttons already removed).

---

## Background / design decisions

### ConnectionMode enum
Add `src/util/ConnectionMode.java`:
```java
package util;
public enum ConnectionMode { DIRECT_DB, VIA_SERVER }
```

### AppState singleton (or pass-through)
The chosen mode needs to be accessible wherever saves/loads happen (GUI panels, CLI Main).
Use a simple static field — a minimal `AppState` class or a field on `WorkoutTrackerGUI` /
`Main`:
```java
public static ConnectionMode connectionMode;   // set once at login, never changed mid-session
public static boolean offlineMode = false;     // true if connection failed at startup
```

---

## Tasks

### B1. Add mode toggle to ProfileSetupDialog (GUI login)
File: `src/gui/ProfileSetupDialog.java`

After the existing username field, add a `JComboBox<String>` (or two `JRadioButton`s) with options:
- `"Direct (Database)"`
- `"Via Server"`

On dialog confirm, store the selection:
```java
WorkoutTrackerGUI.connectionMode = <selected>;
```

---

### B2. Add mode toggle to CLI startup
File: `src/Main.java`

After `setupUser()` (username entry), print:
```
Select connection mode:
1. Direct (Database)
2. Via Server
```
Read choice, store in `Main.connectionMode`.

---

### B3. Auto-load on startup (GUI)
File: `src/gui/WorkoutTrackerGUI.java` (or a new `StartupLoader` helper called from there)

After login dialog closes:
```
1. Try to connect (DB or Server depending on mode)
2. If success:
   a. Load sessions into WorkoutManager
   b. Write workouts_<username>.bin immediately
   c. Set offlineMode = false
3. If failure:
   a. Load from workouts_<username>.bin if it exists
   b. Set offlineMode = true
   c. Show warning message (see Section 6 of IMPROVEMENTS.md):
      GUI: non-blocking JOptionPane.showMessageDialog (not modal blocking — use after pack/setVisible)
4. If .bin also missing:
   a. Start with empty list
   b. Show "Could not connect and no local data found. Starting fresh."
```

Connecting to DB: use `DatabaseUtility.getConnection(...)` wrapped in try/catch — failure = catch.
Connecting to Server: open a `Socket(host, port)` in try/catch — failure = catch.

Loading from DB: call `WorkoutSessionDAO.getAllSessions(userName)`.
Loading from Server: send `GET_SESSIONS:<userName>` via `WorkoutClient.sendAndReceive` (or equivalent).

---

### B4. Auto-load on startup (CLI)
File: `src/Main.java`

Immediately after mode selection (B2), run the same logic as B3 but print to console instead of dialogs.

---

### B5. Auto-save on every mutation (GUI)
Files: `src/gui/AddWorkoutPanel.java`, and any panel that supports delete.

Wherever a session is added or deleted, call a shared helper:
```java
private void autoSave(String userName, WorkoutManager manager) {
    if (!offlineMode) {
        // save to DB or Server depending on mode
    }
    // always write .bin
    FileManager.saveBinary(userName, manager.getAllSessions());
}
```

The "Export TXT" and "Export XML" buttons remain — they are export-only and not affected.

---

### B6. Auto-save on every mutation (CLI)
File: `src/Main.java`

After every add/delete operation, call the same save logic (save to DB/Server if online, always write `.bin`).

Remove the now-unreachable save-to-DB and load-from-DB code paths (already cleaned in Phase A).

---

### B7. .bin always written on clean exit
GUI: override `WorkoutTrackerGUI`'s window-closing listener to write `.bin` before disposing.  
CLI: add a final `FileManager.saveBinary(...)` call before `System.exit` or after the loop ends.

---

## Acceptance criteria
- Login (GUI and CLI) always asks for mode before proceeding.
- On startup, sessions load automatically — no manual "Load" button needed.
- Adding or deleting a session triggers an immediate save (DB/Server + .bin).
- `.bin` file exists and is current after any session that touched data.
- `offlineMode` correctly reflects whether the connection succeeded.
- No regressions in export (TXT, XML, report) functionality.
