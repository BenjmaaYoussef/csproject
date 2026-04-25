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
- Box 3: **FileManager** — .txt / .bin / .xml / report
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

## Slide 2 — The Startup Flow

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

## Slide 3 — Chapter 2: Exceptions

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
- The entire app is protected by a hierarchy of checked exceptions rooted in `WorkoutAppException extends Exception`
- Three specialized exceptions: `InvalidExerciseException` (bad data), `WorkoutNotFoundException` (date not found), `DuplicateExerciseException` (same exercise added twice)
- Checked — not `RuntimeException` — means the compiler forces every caller to either catch them or declare `throws`. You cannot ignore them.
- `getSessionByDate()` declares `throws WorkoutNotFoundException` — if the date is not in the list, we throw. The caller has no choice but to handle it.
- This pattern runs through every layer: the CLI catches and prints, the GUI catches and shows a dialog, the server catches and returns an error string

### Demo Steps
1. In the running CLI → choose "View session by date" → enter a date that has no session → show the caught error message
2. Choose "Add session" → enter an exercise with 0 reps → show `InvalidExerciseException` caught and printed

---

## Slide 4 — Chapter 4: Collections & Generics

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
- Each user gets their own `WorkoutManager` instance — three typed `ArrayList` collections, no shared state between users
- `ArrayList<WorkoutSession>` holds sessions in insertion order
- `ArrayList<Exercise>` is the exercise library used for lookup
- `ArrayList<Pair<String, Double>>` holds personal records — the best weight ever lifted per exercise name
- `Pair<K, V>` is a generic interface we wrote: two type parameters, no casting, no raw types. `OrderedPair<K, V>` is the concrete implementation.
- Personal records auto-update inside `addSession()` — every time a session is added, it scans the exercises and calls `updatePersonalRecord()` if the new weight beats the stored one

### Demo Steps
1. In the running CLI → choose "View personal records" → show the `(Bench Press, 80.0)` style output
2. Choose "View all sessions" → show the ordered list printed from the `ArrayList`

---

## Slide 5 — Chapter 3 + Chapter 5: Persistence

**Duration**: ~2 min
**Type**: Split — code screenshots left, file explorer + open file right

### Visual
Left half: two VS Code screenshots stacked (one for each format)

Right half: a file explorer window showing three files for the same user:
- `workouts_Youssef.txt` — open in a text editor showing the pipe-delimited rows
- `workouts_Youssef.bin` — shown as a file icon (unreadable bytes)
- `report_Youssef.txt` — shown as a file icon

### Code Screenshots
**Screenshot 1 — Chapter 3, FileWriter**:
- `src/util/FileManager.java` — lines 54–69 (`saveSessions` method)
- Make sure `writer.close()` at line 68 is visible — it is inside the `try` block, not in a `finally`

**Screenshot 2 — Chapter 5, ObjectOutputStream**:
- `src/util/FileManager.java` — lines 141–154 (`saveUserSessions` method)

### Slide Text
- Three file formats, each with a distinct purpose — none of them duplicate each other
- `.txt` — written with `FileWriter`, read back with `Scanner(File)`. Pipe-delimited rows: one `SESSION|` line per session, one `EXERCISE|` line per exercise. Human-readable and fully re-importable into the app.
- `.bin` — `ObjectOutputStream.writeObject(sessions)` serializes the entire `ArrayList` in one call. Written automatically on clean exit. On startup, if the DB or server is unreachable, `autoLoad()` reads this file so the user never starts with an empty list.
- `report_name.txt` — formatted human summary exported on demand. This is what you read at the pre-login prompt.
- `sync_name.txt` — a single millisecond timestamp. If `workouts_name.bin` was modified more recently than this timestamp, the app knows there are unsynced offline sessions and pushes them before loading.
- Notice: `writer.close()` is called inside the `try` block — no try-with-resources, no `finally`. That is the professor's exact coding style from the Chapter 3 slides.

### Demo Steps
1. In the running CLI → choose "Export TXT" → `workouts_Youssef.txt` is written
2. Choose "Export Report" → `report_Youssef.txt` is written
3. Open the file explorer on screen — show all three files present
4. Open `workouts_Youssef.txt` in a text editor — show the readable pipe-delimited rows live

---

## Slide 6 — Chapter 6: Database

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
- `Class.forName("com.mysql.cj.jdbc.Driver")` — registers the JDBC driver before `DriverManager` can use it. This is the exact pattern from the Chapter 6 slides. Without it, the connection fails.
- `PreparedStatement` with bound parameters (`setString`, `setInt`, `setDouble`) — prevents SQL injection and handles type conversion automatically. Never raw string concatenation.
- `setAutoCommit(false)` starts a manual transaction — MySQL holds all changes until we call `commit()`
- The session row is inserted first. `getGeneratedKeys()` retrieves the auto-generated `session_id`. That ID is then used as the foreign key when inserting each exercise row.
- If any `SQLException` occurs between the session insert and the last exercise insert, `rollback()` in the `catch` block undoes everything. The session and its exercises are always consistent.

### Demo Steps
1. In the running CLI → log a new workout session with two exercises
2. Switch to MySQL Workbench → refresh `workout_sessions` → new row visible
3. Switch to `session_exercises` → both exercise rows visible, linked by the same `session_id`

---

## Slide 7 — Chapter 7: Client-Server

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
- The server opens a `ServerSocket` on port 9876 and blocks at `accept()` — it waits until a client connects
- Both sides wrap the socket's streams in `DataInputStream` and `DataOutputStream`. `readUTF()` and `writeUTF()` are the exact method pair from the Chapter 7 slides.
- The protocol is plain text: `GET_SESSIONS:<name>`, `ADD_SESSION:<userName>\n<session data>`, or `EXIT` — simple enough to read in the terminal
- In Via Server mode, the client never opens a JDBC connection. All database operations happen on the server side through the same `WorkoutSessionDAO` from the previous slide.
- The offline sync works identically in both modes — in Via Server mode, the unsynced sessions from the `.bin` file are pushed as `ADD_SESSION` socket commands instead of direct DAO calls

### Demo Steps
1. Open two terminals side by side
2. Left terminal → start `WorkoutServer` → it prints "WorkoutServer started on port 9876" and waits
3. Right terminal → run the CLI, pick Via Server mode → fetch sessions → server prints "Client connected" and "Command received: GET_SESSIONS:Youssef", client prints the session list
4. Add a session through the client → server prints the ADD_SESSION command → switch to MySQL Workbench and refresh — the row appears

---

## Slide 8 — GUI (Bonus)

**Duration**: ~1.5 min
**Type**: Demo only — no code

### Visual
A screenshot grid showing the five tabs:
- Dashboard tab (bar chart visible)
- Add Workout tab (form visible)
- History tab (JTable with rows, detail pane below)
- Profile tab (BMI gauge visible)
- Exports tab (four cards visible, with the mode badge in the top area)

### Slide Text
- The GUI is a bonus — `WorkoutTrackerGUI` shares the exact same `WorkoutManager`, `FileManager`, `WorkoutSessionDAO`, and `WorkoutClient` as the CLI. No logic was duplicated.
- Same startup flow as the CLI: pre-login report prompt → profile selection → connection mode
- Dashboard: custom bar chart (exercises per session) drawn entirely with `Graphics2D` — no external library
- History: a `JTable` of all sessions — click a row, the detail pane below updates immediately
- Profile: user info and a BMI semicircle gauge drawn with `Graphics2D`
- Exports tab shows the connection mode most visibly: in Direct DB mode, all 4 cards are active. Switch to Via Server — the Database card grays out, the mode badge changes, and the subtitle updates. The same mode-awareness exists in the CLI menu.

### Demo Steps
1. Run `WorkoutTrackerGUI.java` — show the pre-login prompt and profile selector (same as CLI)
2. Connection mode → pick Direct DB
3. Dashboard tab — point to the bar chart
4. Add Workout tab → add a session with two exercises → save
5. History tab → new session appears in the table → click the row → detail pane updates
6. Profile tab → show the BMI gauge
7. Exports tab → show all 4 cards active → restart or simulate Via Server mode → Database card grays out, badge changes to "Via Server"

---

## Slide 9 — Summary

**Duration**: ~30 sec
**Type**: Table

### Slide Text

| Chapter | Concept | Where it shows in the app |
|---------|---------|---------------------------|
| Ch.2 | Custom checked exceptions | `exception/` hierarchy — thrown in `WorkoutManager`, caught in CLI, GUI, and server |
| Ch.4 | Collections + Generics | `ArrayList<WorkoutSession>`, `ArrayList<Pair<String, Double>>`, `OrderedPair<K,V>` |
| Ch.3 | FileWriter / Scanner | `.txt` export and re-import, report export and pre-login read |
| Ch.5 | ObjectOutputStream | `.bin` auto-save on exit, auto-load fallback on startup, offline cache |
| Ch.6 | JDBC + PreparedStatement | `DatabaseUtility.getConnection()`, `WorkoutSessionDAO` atomic transactions |
| Ch.7 | ServerSocket / Socket | TCP text protocol, Via Server mode, offline sync relay via socket commands |

- The exceptions protect the model. The collections hold the data. The files keep it safe locally. The database persists it properly. The server makes it accessible remotely.

---

## Q&A Prep

| Question | Answer |
|----------|--------|
| Why two persistence formats — `.txt` and `.bin`? | `.txt` is human-readable and re-importable — you can open it in any text editor or load it back into the app. `.bin` is the app's own snapshot — faster to read, no line-by-line parsing. They serve different purposes and never replace each other. |
| Why `Class.forName()` before `DriverManager.getConnection()`? | It manually registers the MySQL JDBC driver. Without it, `DriverManager` does not know the driver exists and the connection fails. This is the direct pattern from the Chapter 6 slides. |
| Why `setAutoCommit(false)` in the DAO? | So the session row and all its exercise rows are saved in one atomic transaction. If only the session saved and one exercise insert failed, the data would be inconsistent. With `setAutoCommit(false)`, either everything commits or everything rolls back. |
| Why no try-with-resources anywhere? | That is the professor's coding style from the slide examples. Resources are closed explicitly with `.close()` inside the `try` block, not through try-with-resources syntax. We followed it exactly. |
| What happens if the server is down when the client tries to connect? | `new Socket(HOST, PORT)` throws `IOException`. It is caught and printed. In `autoLoad()`, a failed connection sets `connected = false` and the app falls back to the `.bin` file instead. |
| How does the offline sync know what to push? | `sync_Youssef.txt` holds the timestamp of the last successful push. On startup, `autoLoad()` reads `binFile.lastModified()` and compares it to that timestamp. If the `.bin` is newer, there are offline sessions to push — and they are sent before loading from the remote source. |
| Why is the GUI a separate entry point from the CLI? | `WorkoutTrackerGUI` has its own `main()` method so it can be launched independently. Both the CLI and GUI share the same model, utility, database, and network classes. Running `Main.java` gives the CLI; running `WorkoutTrackerGUI.java` gives the GUI. |
