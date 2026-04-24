# Workout Tracker — Improvements & Fixes

This document describes all agreed changes to be implemented in the next session.
Read it fully before writing any code.

---

## Already Done
- **Load duplication bug**: `loadText()`, `loadBinary()`, `loadFromDB()` in `DataPanel.java` now call
  `manager.getAllSessions().clear()` before adding loaded sessions. Fixed.

---

## 1. Persistence Mode Toggle (Login Screen)

At login (both GUI `ProfileSetupDialog` and CLI startup), the user picks a connection mode:

- **Direct (DB):** app reads/writes MySQL via `WorkoutSessionDAO`
- **Via Server:** app communicates via TCP socket to `WorkoutServer`, which handles DB on its end

The chosen mode is stored and used for all auto-save and auto-load operations throughout the session.
After login the user never sees or thinks about this choice again — both modes behave identically.

---

## 2. Auto-Load on Startup

Replace all manual "Load from DB" / "Load from Binary" buttons and CLI menu options with a single
automatic load at startup, after login:

```
1. Try to connect to DB or Server (depending on mode)
2. If connection succeeds:
   a. Load sessions from DB/Server
   b. Write .bin immediately (keeps local cache fresh)
   c. Continue normally
3. If connection fails:
   a. Load sessions from .bin (local cache)
   b. Show warning to user (see Section 6)
   c. Continue in offline mode for this session
4. If .bin also doesn't exist:
   a. Start with empty session list
   b. Inform user
```

**Remove:**
- GUI "Load from DB" button (`DataPanel.java`)
- GUI "Load from Binary" button (`DataPanel.java`)
- CLI option 11 (Load from DB) (`Main.java`)

---

## 3. Auto-Save on Every Change

Replace all manual "Save to DB" buttons and CLI options with automatic saving triggered by every
mutation (add session, delete session):

```
If connected:
   Save to DB/Server AND update .bin
If offline (connection failed at startup):
   Save to .bin only
```

**Remove:**
- GUI "Save to DB" button (`DataPanel.java`)
- CLI option 10 (Save to DB) (`Main.java`)

**Keep (export only, no load-back):**
- GUI "Save to TXT" button → renamed to "Export TXT" for clarity
- GUI "Export XML" button
- CLI option 8 (Export report)
- CLI option 9 (Export XML)

---

## 4. Sync on Next Startup (Offline Recovery)

If the previous session ran in offline mode (saved to `.bin` only), the next startup should detect
this and sync when connection is available:

```
Startup:
1. Try to connect to DB/Server
2. If connection succeeds AND .bin exists AND .bin is newer than last DB sync:
   a. Load sessions from .bin
   b. Push all sessions to DB/Server
   c. Confirm to user: "Connection restored. Local changes synced."
3. Otherwise proceed normally (Section 2)
```

No background threads, no mid-session retries. Sync only happens at startup.

---

## 5. .bin as Local Cache (Always Written)

`.bin` is no longer just a manual backup — it is always kept up to date:
- Written after every successful auto-save (connected mode)
- Written after every save in offline mode
- Written on clean app exit regardless of mode

This ensures `.bin` always reflects the latest known state.

File naming follows existing convention: `workouts_<username>.bin`

---

## 6. User-Facing Messages for Connection Failures

**GUI:** show a non-blocking banner or dialog at startup. Do not prevent the user from continuing.

**CLI:** print to `System.out` before the main menu appears.

| Situation | Message |
|-----------|---------|
| DB/Server unreachable at startup, `.bin` loaded | `"Could not connect. Using latest saved version from disk."` |
| DB/Server unreachable at startup, no `.bin` | `"Could not connect and no local data found. Starting fresh."` |
| Offline session synced successfully on next startup | `"Connection restored. Local changes synced."` |

No mid-session connection warnings. If offline at startup, the app stays offline for that session.

---

## 7. Pre-Login Report Reader

Before showing the login form, allow the user to read their last exported report.

**Flow:**
```
1. Ask: "View your last report before logging in? (yes/no)"
2. If yes:
   a. Ask for username
   b. Read report_<username>.txt using Scanner(File)
   c. Display contents
   d. Then proceed to login
3. If no or file doesn't exist:
   a. Proceed to login directly
   b. If file not found: inform user ("No report found for this user.")
```

**Implementation:**
- Use `Scanner(File)` from `FileManager` — this is the Phase 3 concept being exercised
- Throw/catch a custom exception (e.g. `WorkoutAppException`) if file is not found
- Apply to both GUI (scrollable dialog before `ProfileSetupDialog`) and CLI (before `setupUser()`)

---

## 8. Server: Fix Duplicated Parsing Logic

`WorkoutServer.parseSession()` duplicates the pipe-delimited format parsing that already exists in
`FileManager`. Fix: remove `WorkoutServer.parseSession()` and call `FileManager`'s equivalent method
instead. Both must use the same format.

---

## 9. Remove Manual Load Buttons — Full List

| Location | Item to Remove |
|----------|---------------|
| `DataPanel.java` (GUI) | "Load from TXT" button |
| `DataPanel.java` (GUI) | "Load from Binary" button |
| `DataPanel.java` (GUI) | "Load from DB" button |
| `DataPanel.java` (GUI) | "Save to DB" button |
| `Main.java` (CLI) | Option 10 — Save all sessions to database |
| `Main.java` (CLI) | Option 11 — Load sessions from database |

---

## 10. CLI and GUI Parity

After all changes, CLI and GUI must behave identically:
- Same toggle at login (Direct vs Via Server)
- Same auto-load on startup
- Same auto-save on every change
- Same fallback behavior
- Same pre-login report reader

No feature should exist in one and not the other.

---

## Summary of What Each Phase Contributes After Changes

| Phase | Concept | How it's used |
|-------|---------|---------------|
| 1 | Exceptions | `WorkoutAppException` thrown when report file not found |
| 2 | Collections & Generics | `WorkoutManager` session list throughout |
| 3 | File Handling | Pre-login report reader via `Scanner(File)`; TXT/XML export |
| 4 | Serialization | `.bin` as always-updated local cache and offline fallback |
| 5 | Database | Primary store in Direct mode, auto-save/load |
| 6 | Client-Server | Alternative mode via socket toggle at login |
| 7 | GUI | All of the above surfaced through Swing interface |
