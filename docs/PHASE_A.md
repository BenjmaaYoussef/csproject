# Phase A — Cleanup & Refactor
**Goal:** Remove dead UI/CLI code and fix the duplicated parsing logic.
No new features. After this phase the codebase is clean and ready for Phases B and C.

---

## Tasks

### A1. Remove manual DB/load buttons from DataPanel.java (GUI)
File: `src/gui/DataPanel.java`

Remove these buttons and their ActionListener logic entirely:
- "Load from TXT" button
- "Load from Binary" button
- "Load from DB" button
- "Save to DB" button

Rename the remaining button:
- "Save to TXT" → "Export TXT"

Do not remove:
- "Export XML" button

After removal, re-lay out the remaining buttons so the panel still looks clean.

---

### A2. Remove manual DB/load options from Main.java (CLI)
File: `src/Main.java`

Remove from the menu and switch/if block:
- Option 10 — Save all sessions to database
- Option 11 — Load sessions from database

Renumber subsequent options if needed so the menu stays contiguous.

---

### A3. Fix duplicated parsing logic in WorkoutServer
Files: `src/network/WorkoutServer.java`, `src/util/FileManager.java`

`WorkoutServer.parseSession()` duplicates the pipe-delimited parsing that already lives in
`FileManager`. Fix:

1. Add (or expose) a **static** method in `FileManager`:
   ```java
   public static WorkoutSession parseSessionFromText(String data)
   ```
   It should accept the same pipe-delimited block that `parseSession()` currently handles
   (SESSION|date|notes\nEXERCISE|...).  
   Reuse the existing line-parsing code already in `loadSessions()` / similar methods — do not
   duplicate it again.

2. In `WorkoutServer.handleCommand()`, replace the call to the local `parseSession()` with a call
   to `FileManager.parseSessionFromText()`.

3. Delete the private `WorkoutServer.parseSession()` method entirely.

Both server and any future callers now share a single source of truth for the format.

---

## Acceptance criteria
- `DataPanel.java` compiles with no references to the removed buttons.
- `Main.java` menu no longer lists options 10 or 11.
- `WorkoutServer` has no `parseSession` method; it calls `FileManager.parseSessionFromText`.
- All other existing functionality unchanged.
