# Phase C — Offline Sync + Connection Messages + Pre-Login Report Reader
**Goal:** Add offline-to-online sync on the next startup, user-facing connection messages, and the
pre-login report reader. Verify CLI/GUI parity across all improvements.

Depends on Phases A and B being complete.

---

## Tasks

### C1. Offline sync on next startup
Files: `src/gui/WorkoutTrackerGUI.java`, `src/Main.java`

At the very beginning of the startup load sequence (before normal auto-load from B3/B4):

```
1. Try to connect to DB/Server
2. If connection succeeds AND workouts_<username>.bin exists AND .bin is newer than last DB sync:
   a. Load sessions from .bin
   b. Push all sessions to DB/Server  (save each session via DAO or ADD_SESSION command)
   c. Show message: "Connection restored. Local changes synced."
   d. Then proceed with normal auto-load (which will now read the freshly-synced DB)
3. Otherwise proceed with normal auto-load (Phase B logic)
```

**Tracking "last DB sync":** write a small metadata file `sync_<username>.txt` containing a
timestamp (milliseconds, `System.currentTimeMillis()`) whenever a successful DB/Server save
completes. Compare this with `workouts_<username>.bin`'s last-modified time using `File.lastModified()`.
Do NOT use background threads. Sync happens only at startup, synchronously.

---

### C2. User-facing connection failure messages
Already partially handled in B3/B4. Ensure the exact message strings from IMPROVEMENTS.md are used:

| Situation | Message |
|-----------|---------|
| DB/Server unreachable, `.bin` loaded | `"Could not connect. Using latest saved version from disk."` |
| DB/Server unreachable, no `.bin` | `"Could not connect and no local data found. Starting fresh."` |
| Offline session synced on next startup | `"Connection restored. Local changes synced."` |

**GUI:** use `JOptionPane.showMessageDialog(frame, message)` — called after `setVisible(true)` so
the main window is already showing.  
**CLI:** `System.out.println(message)` before the main menu loop.

No mid-session messages. Once the app is running, connection status is not reported again.

---

### C3. Pre-login report reader (CLI)
File: `src/Main.java`

Insert this block **before** `setupUser()` / the login prompt:

```
System.out.println("View your last report before logging in? (yes/no)");
String answer = scanner.nextLine().trim();
if (answer.equalsIgnoreCase("yes")) {
    System.out.println("Enter your username:");
    String name = scanner.nextLine().trim();
    try {
        FileManager.readAndPrintReport(name);   // see C5
    } catch (WorkoutAppException e) {
        System.out.println("No report found for this user.");
    }
}
// then proceed to setupUser() as before
```

---

### C4. Pre-login report reader (GUI)
File: `src/gui/WorkoutTrackerGUI.java` (called before `ProfileSetupDialog` is shown)

Show a `JOptionPane.showConfirmDialog` asking "View your last report before logging in?"  
If YES:
1. Prompt for username via `JOptionPane.showInputDialog`.
2. Try `FileManager.readReport(name)` → returns the file contents as a `String`.
3. Show in a scrollable `JTextArea` inside a `JScrollPane` inside a `JOptionPane.showMessageDialog`.
4. If `WorkoutAppException` is thrown (file not found), show `"No report found for this user."`.

Then proceed to `ProfileSetupDialog` as before.

---

### C5. Add report-reading methods to FileManager
File: `src/util/FileManager.java`

Add two methods:

```java
/** CLI use: reads report_<userName>.txt and prints each line to System.out. */
public static void readAndPrintReport(String userName) throws WorkoutAppException {
    File file = new File(userReportFile(userName));
    if (!file.exists()) {
        throw new WorkoutAppException("Report file not found for user: " + userName);
    }
    try {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            System.out.println(scanner.nextLine());
        }
        scanner.close();
    } catch (IOException e) {
        System.err.println("An error occurred.");
        e.printStackTrace();
    }
}

/** GUI use: reads report_<userName>.txt and returns contents as a String. */
public static String readReport(String userName) throws WorkoutAppException {
    File file = new File(userReportFile(userName));
    if (!file.exists()) {
        throw new WorkoutAppException("Report file not found for user: " + userName);
    }
    StringBuilder sb = new StringBuilder();
    try {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine()).append("\n");
        }
        scanner.close();
    } catch (IOException e) {
        System.err.println("An error occurred.");
        e.printStackTrace();
    }
    return sb.toString();
}
```

Use `Scanner(File)` — this is the Phase 3 concept being exercised.  
Helper `userReportFile(userName)` should follow the existing convention in `FileManager`
(e.g. `"report_" + userName + ".txt"`).

---

### C6. CLI/GUI parity audit
After all tasks above are done, verify that CLI (`Main.java`) and GUI (`WorkoutTrackerGUI.java` +
panels) have identical behavior for:

| Feature | CLI location | GUI location |
|---------|-------------|--------------|
| Pre-login report reader | Before `setupUser()` | Before `ProfileSetupDialog` |
| Mode toggle (Direct / Via Server) | After `setupUser()` | Inside `ProfileSetupDialog` |
| Auto-load on startup | After mode selection | After `ProfileSetupDialog` closes |
| Offline sync check | Start of auto-load | Start of auto-load |
| Connection failure messages | `System.out.println` | `JOptionPane` |
| Auto-save on add/delete | After each mutation | After each mutation in panels |
| `.bin` written on exit | Before loop ends | Window-closing listener |
| Export TXT / XML / Report | Options 8, 9 in menu | "Export TXT" / "Export XML" buttons |

Fix any gap found during the audit.

---

## Acceptance criteria
- If app ran offline last time and connection is restored, `.bin` data is pushed to DB/Server before
  loading, and the user sees `"Connection restored. Local changes synced."`.
- Exact message strings from IMPROVEMENTS.md appear in the correct situations (no typos).
- Pre-login report reader works in both GUI and CLI; throws/catches `WorkoutAppException` for missing file.
- `FileManager.readAndPrintReport` and `FileManager.readReport` use `Scanner(File)`.
- Parity audit table above has no gaps.
