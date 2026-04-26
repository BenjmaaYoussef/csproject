# CS202 – Workout Tracker Presentation

> **Layout rule for technical slides**: code screenshot on the left half, live CLI or GUI on the right half.
> **Demo**: run live on the same slide — you show the code, then immediately demonstrate it.
> **Total time**: ~12 min + 3 min Q&A.
> **Slide Text** = what is written on the slide and read aloud by the presenter.
> **Demo Steps** = live actions the presenter performs during that slide.
> **Visual** = description of the graphic or layout to build in the presentation tool.

---

## Slide 1 — What We Built

**Duration**: ~1 min
**Type**: Overview — no code, no demo

### Visual
A vertical stack of five labeled boxes connected by downward arrows. Title above the stack: "The Workout Tracker Stack".

- Box 1 (top): **CLI — Main.java** (left) and **GUI — WorkoutTrackerGUI.java — bonus** (right), sharing the same box with a dividing line
- Box 2: **WorkoutManager + Collections** — per-user in-memory state
- Box 3: **FileManager** — .txt / .bin
- Box 4: **WorkoutSessionDAO** — MySQL via JDBC
- Box 5 (bottom): **WorkoutServer ↔ WorkoutClient** — TCP port 9876

To the right of boxes 4 and 5, two labeled branches:
- "Direct DB" pointing to box 4
- "Via Server" pointing to box 5

A double-headed arrow between boxes 3 and 4, labeled "offline fallback".

### Slide Text
- A multi-user workout tracker built across 6 CS202 chapters — one chapter per layer
- Two interfaces: the CLI is the main project, the Swing GUI is a bonus built on top of it
- Two connection modes at startup: Direct DB (JDBC directly to MySQL) or Via Server (everything goes through a TCP socket relay)
- Every layer has a fallback — if the database or server is unreachable, the app falls back to a local binary file and syncs automatically when the connection comes back
- We will go through each layer one by one with code and a live demo

---

## Slide 2 — Every Session Starts Here
**Subtitle**: The Startup Flow

**Duration**: ~1.5 min
**Type**: Demo only — no code

### Visual
The slide is structured top to bottom in four sections:

**[Diagram 2a — top]** (from DIAGRAMS.md)
Three-step flow: Pre-login report → Select profile → Choose connection mode

**[Text block — middle]**
First three Slide Text bullets (see below)

**[Diagram 2b — bottom]** (from DIAGRAMS.md)
Two-step flow: Auto-load sessions → Main menu

**[Text block — below]**
Last three Slide Text bullets (see below)

### Slide Text

*Goes between Diagram 2a and Diagram 2b:*
- The app offers to show your last report before you even log in
- Returning users pick their profile from a saved list — no re-entry needed
- You choose once: Direct DB goes straight to MySQL, Via Server routes through the socket server

*Goes below Diagram 2b:*
- Sessions load automatically from whichever source is active
- The menu adapts — some options are hidden depending on the chosen mode

### Demo Steps
1. Run `Main.java` live
2. At the pre-login prompt → type `yes` → it reads and prints `report_Youssef.txt`
3. At profile selection → pick an existing profile from the list
4. At connection mode → pick `1. Direct DB`
5. Watch the auto-load print the session count
6. Show the main menu — point out the numbered options

---

## Slide 3 — Built to Fail Safely
**Subtitle**: Chapter 2 · Exceptions

**Duration**: ~1.5 min
**Type**: Split — code screenshot left, CLI demo right

### Visual
Left half: two items stacked
- Top: exception hierarchy diagram (Diagram 3 from DIAGRAMS.md) — use the rendered Mermaid class diagram
- Bottom: VS Code screenshot of `getSessionByDate()` from WorkoutManager (lines 50–57)

Right half: CLI terminal showing an error being triggered

### Code Screenshots
**Screenshot** (bottom-left, below the diagram):
- `src/model/WorkoutManager.java` — lines 50–57 (`getSessionByDate` method)

### Slide Text
- The app uses a hierarchy of custom checked exceptions rooted in `WorkoutAppException` — three specialized subclasses cover invalid data, missing sessions, and duplicate exercises.
- Because they are checked and not `RuntimeException`, the compiler forces every caller to either catch them or declare `throws` — you cannot ignore them.
- `getSessionByDate()` is a good example: it declares `throws WorkoutNotFoundException`, so if the date is not found, we throw and the caller is forced to handle it.

### Demo Steps
1. In the running CLI → choose "View session by date" → enter a date that has no session → show the caught error message
2. Choose "Add session" → enter an exercise with 0 reps → show `InvalidExerciseException` caught and printed

---

## Slide 4 — One User, One Manager
**Subtitle**: Chapter 4 · Collections & Generics

**Duration**: ~1.5 min
**Type**: Split — code screenshot left, CLI demo right

### Visual
Left half: two VS Code screenshots stacked
- Top: the three field declarations in WorkoutManager
- Bottom: Pair.java and OrderedPair.java side by side

Right half: CLI terminal showing personal records printed

### Code Screenshots
**Screenshot 1**:
- `src/model/WorkoutManager.java` — lines 22–25 (the three `ArrayList` field declarations)

**Screenshot 2** — take both together in VS Code:
- `src/util/Pair.java` — full file (lines 1–12)
- `src/util/OrderedPair.java` — lines 9–26

### Slide Text
- Each user gets their own `WorkoutManager` instance with three typed `ArrayList` collections — sessions, exercises, and personal records.
- Personal records auto-update inside `addSession()` — if the new weight beats the stored one, the record updates as a `Pair` of the exercise name and its best weight.
- `Pair<K, V>` is a generic interface we wrote ourselves: two type parameters, no casting, no raw types. `OrderedPair<K, V>` is the concrete implementation that holds each of those records.

### Demo Steps
1. In the running CLI → choose "View personal records" → show the `(Bench Press, 80.0)` style output
2. Choose "View all sessions" → show the ordered list printed from the `ArrayList`

---

## Slide 5 — Nothing Gets Lost
**Subtitle**: Chapter 3 · File Handling

**Duration**: ~1 min
**Type**: Split — code screenshot left, CLI screenshot right

### Visual
Left half: VS Code screenshot of `exportReport` method

Right half: CLI screenshot showing "Export Report" selected and the confirmation line printed

### Code Screenshots
- `src/util/FileManager.java` — lines 274–292 (`exportReport` method)

### CLI Screenshots
- CLI session: user selects "Export Report" → terminal prints `Report exported to report_Youssef.txt.`

### Slide Text
- The app produces two plain-text files using `FileWriter` and `Scanner(File)`: a human-readable workout report and a sync timestamp — each with a distinct role in the app's flow.
- `exportReport()` writes `report_Youssef.txt` on demand — the CLI screenshot on the right shows it being triggered and the confirmation that the file was written.
- `sync_Youssef.txt` holds a millisecond timestamp written after every successful push — `Scanner(File)` reads it back on startup to check whether the `.bin` file has sessions that were never synced.

---

## Slide 5.1 — What Gets Written
**Subtitle**: Chapter 3 · File Handling (continued)

**Duration**: ~30 sec
**Type**: Split — report file left, CLI pre-login screenshot right

### Visual
Left half: `report_Youssef.txt` open in a text editor — show the full formatted content (header, summary block, session details)

Right half: CLI screenshot of the pre-login prompt — user types `yes` and the report prints line by line in the terminal

### Slide Text
- This is the output of `exportReport()` — a formatted header, the user's overall summary, and each session in date order — and the same file is what the pre-login prompt reads back with `Scanner(File)` before the profile selector even appears.

---

## Slide 6 — Nothing Gets Lost
**Subtitle**: Chapter 5 · Serialization

**Duration**: ~1 min
**Type**: Split — code screenshot left, file explorer right

### Visual
Left half: VS Code screenshot of `saveUserSessions` method

Right half: file explorer showing `workouts_Youssef.bin` as an unreadable binary file alongside the `.txt`

### Code Screenshots
- `src/util/FileManager.java` — lines 141–154 (`saveUserSessions` method)

### Slide Text
- Where the `.txt` files are written on demand, `workouts_Youssef.bin` is automatic — `saveUserSessions()` uses `ObjectOutputStream.writeObject()` to serialize the entire session list in one call, triggered on every add and on exit.
- On startup, if the database or server is unreachable, `loadUserSessions()` reads it back with `ObjectInputStream` — so the user never starts with an empty list regardless of connectivity.
- `sync_Youssef.txt` connects both layers: if the `.bin` file is newer than the last sync timestamp, the app knows there are offline sessions to push before loading from the remote source.

### Demo Steps
1. Exit the app cleanly → show `workouts_Youssef.bin` created in the file explorer
2. Restart the app → point out the auto-load message printing the session count from the binary file

---

## Slide 7 — The Source of Truth
**Subtitle**: Chapter 6 · Database

**Duration**: ~1.5 min
**Type**: Split — code screenshots left, MySQL Workbench right

### Visual
Left half: two VS Code screenshots stacked

Right half: MySQL Workbench with two tabs open:
- `workout_sessions` table showing a row with `session_id`, `user_name`, `session_date`, `notes`
- `session_exercises` table showing rows linked by `session_id`

### Code Screenshots
**Screenshot 1**:
- `src/db/DatabaseUtility.java` — lines 7–20 (full `getConnection` method)
- Both the `Class.forName()` and `DriverManager.getConnection()` lines must be visible

**Screenshot 2**:
- `src/db/WorkoutSessionDAO.java` — lines 26–57
- From `connection.setAutoCommit(false)` to `connection.commit()`
- Must show: `prepareStatement`, `RETURN_GENERATED_KEYS`, `setString`, `executeUpdate`, `getGeneratedKeys`, `commit`

### Slide Text
- Every session is persisted to MySQL through `WorkoutSessionDAO` — two tables, `workout_sessions` for the session header and `session_exercises` for each exercise, linked by a generated `session_id` visible in MySQL Workbench on the right.
- `Class.forName()` registers the JDBC driver and `DriverManager.getConnection()` opens the connection — both are in the top code screenshot, and without `Class.forName()` the connection never opens.
- The insert block opens with `setAutoCommit(false)`, prepares the session statement with `RETURN_GENERATED_KEYS`, binds the fields, and calls `executeUpdate()` — that is where the session row visible in MySQL Workbench on the right gets written.

### Demo Steps
1. In the running CLI → log a new workout session with two exercises
2. Switch to MySQL Workbench → refresh `workout_sessions` → new row visible
3. Switch to `session_exercises` → both exercise rows visible, linked by the same `session_id`

---

## Slide 8 — Going Remote
**Subtitle**: Chapter 7 · Client-Server

**Duration**: ~1.5 min
**Type**: Split — code screenshots left, two terminals side by side right

### Visual
Left half: two VS Code screenshots stacked (server code top, client code bottom)

Right half: two terminal windows side by side
- Left terminal: WorkoutServer running, printing received commands
- Right terminal: CLI client sending commands and printing responses

### Code Screenshots
**Screenshot 1 — Server**:
- `src/network/WorkoutServer.java` — lines 22–44
- From `serverSocket = new ServerSocket(port)` to `socket.close()`
- Must show: `accept()`, `DataInputStream`, `DataOutputStream`, `readUTF()`, `writeUTF()`

**Screenshot 2 — Client**:
- `src/network/WorkoutClient.java` — lines 53–73 (`sendAndReceive` method)
- Must show: `new Socket(HOST, PORT)`, `DataOutputStream`, `DataInputStream`, `writeUTF(command)`, `readUTF()`

### Slide Text
- The server opens a `ServerSocket` on port 9876 and blocks at `accept()`, waiting for a client — both sides then wrap the socket streams in `DataInputStream` and `DataOutputStream`, using `readUTF()` and `writeUTF()`, the exact pair from the Chapter 7 slides.
- The protocol is plain text: `GET_SESSIONS`, `ADD_SESSION`, or `EXIT` — simple enough to read directly in the terminal during the demo.
- In Via Server mode, the client never opens a JDBC connection — all database operations happen on the server side through the same `WorkoutSessionDAO` from the previous slide, so the client stays completely decoupled from the database.

### Demo Steps
1. Open two terminals side by side
2. Left terminal → start `WorkoutServer` → it prints "WorkoutServer started on port 9876" and waits
3. Right terminal → run the CLI, pick Via Server mode → fetch sessions → server prints "Client connected" and "Command received: GET_SESSIONS:Youssef", client prints the session list
4. Add a session through the client → server prints the ADD_SESSION command → switch to MySQL Workbench and refresh — the row appears

---

## Slide 9 — The Same App, With a Face
**Subtitle**: Bonus · Swing GUI

**Duration**: ~1.5 min
**Type**: Demo only — no code

### Visual
A screenshot grid showing the five tabs:
- Dashboard tab (bar chart visible)
- Add Workout tab (form visible)
- History tab (JTable with rows, detail pane below)
- Profile tab (BMI gauge visible)
- Exports tab (two cards visible, with the mode badge in the top area)

### Slide Text
- The GUI is a bonus — `WorkoutTrackerGUI` shares the exact same `WorkoutManager`, `FileManager`, `WorkoutSessionDAO`, and `WorkoutClient` as the CLI. No logic was duplicated.
- Same startup flow as the CLI: pre-login report prompt → profile selection → connection mode
- Dashboard: custom bar chart (exercises per session) drawn entirely with `Graphics2D` — no external library
- History: a `JTable` of all sessions — click a row, the detail pane below updates immediately
- Profile: user info and a BMI semicircle gauge drawn with `Graphics2D`
- Exports tab shows the connection mode most visibly: in Direct DB mode, both cards are active. Switch to Via Server — the Delete Session card grays out, the mode badge changes, and the subtitle updates. The same mode-awareness exists in the CLI menu.

### Demo Steps
1. Run `WorkoutTrackerGUI.java` — show the pre-login prompt and profile selector (same as CLI)
2. Connection mode → pick Direct DB
3. Dashboard tab — point to the bar chart
4. Add Workout tab → add a session with two exercises → save
5. History tab → new session appears in the table → click the row → detail pane updates
6. Profile tab → show the BMI gauge
7. Exports tab → show both cards active → restart or simulate Via Server mode → Delete Session card grays out, badge changes to "Via Server"

---

## Slide 10 — Summary

**Duration**: ~30 sec
**Type**: Bullet list

### Slide Text
- Starting from a single exception class and ending with a networked, multi-user application — six concepts, each one adding a capability and a fallback for everything below it.
- The result is an app that works offline, syncs automatically, and runs identically through a CLI or a GUI.

---

## Q&A Prep

| Question | Answer |
|----------|--------|
| Why keep `.txt` files if sessions are stored in `.bin`? | The two `.txt` files serve different purposes from the session cache. `report_name.txt` is a formatted human summary you can open in any text editor — it is never loaded back into the app. `sync_name.txt` holds a single timestamp that tells the app whether the `.bin` file has unsynchronised sessions. Neither one replaces the binary cache. |
| Why `Class.forName()` before `DriverManager.getConnection()`? | It manually registers the MySQL JDBC driver. Without it, `DriverManager` does not know the driver exists and the connection fails. This is the direct pattern from the Chapter 6 slides. |
| Why `setAutoCommit(false)` in the DAO? | So the session row and all its exercise rows are saved in one atomic transaction. If only the session saved and one exercise insert failed, the data would be inconsistent. With `setAutoCommit(false)`, either everything commits or everything rolls back. |
| Why no try-with-resources anywhere? | That is the professor's coding style from the slide examples. Resources are closed explicitly with `.close()` inside the `try` block, not through try-with-resources syntax. We followed it exactly. |
| What happens if the server is down when the client tries to connect? | `new Socket(HOST, PORT)` throws `IOException`. It is caught and printed. In `autoLoad()`, a failed connection sets `connected = false` and the app falls back to the `.bin` file instead. |
| How does the offline sync know what to push? | `sync_Youssef.txt` holds the timestamp of the last successful push. On startup, `autoLoad()` reads `binFile.lastModified()` and compares it to that timestamp. If the `.bin` is newer, there are offline sessions to push — and they are sent before loading from the remote source. |
| Why is the GUI a separate entry point from the CLI? | `WorkoutTrackerGUI` has its own `main()` method so it can be launched independently. Both the CLI and GUI share the same model, utility, database, and network classes. Running `Main.java` gives the CLI; running `WorkoutTrackerGUI.java` gives the GUI. |
