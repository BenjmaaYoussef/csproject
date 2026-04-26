package util;

import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import exception.WorkoutAppException;
import model.Exercise;
import model.ExerciseType;
import model.WorkoutManager;
import model.WorkoutSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import model.User;

/**
 * Handles all file I/O for the Workout Tracker app.
 * Phase 3: File Handling using FileWriter and Scanner.
 *
 * File format (workouts.txt):
 *   SESSION|yyyy-MM-dd|notes
 *   EXERCISE|name|type|sets|reps|weightKg|durationMin
 */
public class FileManager {

    private static final String USERS_FILE = "users.bin";

    // ------------------------------------------------------------------ Per-user binary save/load

    /** Returns the binary filename for a given user's sessions. */
    private static String userSessionFile(String userName) {
        return "workouts_" + userName.replaceAll("\\s+", "_") + ".bin";
    }

    public static void saveUserSessions(String userName, ArrayList<WorkoutSession> sessions) {
        String path = userSessionFile(userName);
        ObjectOutputStream os = null;
        try {
            FileOutputStream fo = new FileOutputStream(path);
            os = new ObjectOutputStream(fo);
            os.writeObject(sessions);
            os.close();
            System.out.println("Cache updated: " + path + ".");
        } catch (Exception e) {
            System.err.println("An error occurred while saving sessions for " + userName + ".");
            e.printStackTrace();
        } finally {
            if (os != null) {
                try { os.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public static ArrayList<WorkoutSession> loadUserSessions(String userName) {
        ArrayList<WorkoutSession> sessions = new ArrayList<>();
        String path = userSessionFile(userName);
        File file = new File(path);
        if (!file.exists()) {
            return sessions;
        }
        ObjectInputStream ois = null;
        try {
            FileInputStream fi = new FileInputStream(path);
            ois = new ObjectInputStream(fi);
            sessions = (ArrayList<WorkoutSession>) ois.readObject();
            ois.close();
            System.out.println("Local cache: " + sessions.size() + " session(s) for " + userName + ".");
        } catch (Exception e) {
            System.err.println("An error occurred while loading sessions for " + userName + ".");
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try { ois.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
        return sessions;
    }

    // ------------------------------------------------------------------ User list save/load

    public static void saveUsers(ArrayList<User> users) {
        ObjectOutputStream os = null;
        try {
            FileOutputStream fo = new FileOutputStream(USERS_FILE);
            os = new ObjectOutputStream(fo);
            os.writeObject(users);
            os.close();
            System.out.println("User list saved to " + USERS_FILE + ".");
        } catch (Exception e) {
            System.err.println("An error occurred while saving user list.");
            e.printStackTrace();
        } finally {
            if (os != null) {
                try { os.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public static ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return users;
        }
        ObjectInputStream ois = null;
        try {
            FileInputStream fi = new FileInputStream(USERS_FILE);
            ois = new ObjectInputStream(fi);
            users = (ArrayList<User>) ois.readObject();
            ois.close();
            System.out.println("Loaded " + users.size() + " user(s) from " + USERS_FILE + ".");
        } catch (Exception e) {
            System.err.println("An error occurred while loading user list.");
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try { ois.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
        return users;
    }

    // ------------------------------------------------------------------ Parse session from text

    /**
     * Parses a pipe-delimited session block into a WorkoutSession.
     * Format: SESSION|date|notes\nEXERCISE|name|type|sets|reps|weightKg|durationMin\n...
     */
    public static WorkoutSession parseSessionFromText(String data) {
        String[] lines = data.split("\n");
        WorkoutSession session = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("SESSION|")) {
                String[] parts = line.split("\\|", 3);
                String date  = parts[1];
                String notes = parts.length > 2 ? parts[2] : "";
                session = new WorkoutSession(date, notes);
            } else if (line.startsWith("EXERCISE|") && session != null) {
                String[] parts = line.split("\\|");
                try {
                    String name       = parts[1];
                    ExerciseType type = ExerciseType.valueOf(parts[2]);
                    int sets          = Integer.parseInt(parts[3]);
                    int reps          = Integer.parseInt(parts[4]);
                    double weightKg   = Double.parseDouble(parts[5]);
                    int durationMin   = Integer.parseInt(parts[6]);
                    Exercise e = new Exercise(name, sets, reps, weightKg, durationMin, type);
                    session.addExercise(e);
                } catch (InvalidExerciseException e) {
                    System.err.println("An error occurred.");
                    e.printStackTrace();
                } catch (DuplicateExerciseException e) {
                    System.err.println("An error occurred.");
                    e.printStackTrace();
                }
            }
        }
        return session;
    }

    // ------------------------------------------------------------------ Sync timestamp

    /** Returns the sync-timestamp filename for a given user. */
    private static String userSyncFile(String userName) {
        return "sync_" + userName.replaceAll("\\s+", "_") + ".txt";
    }

    /** Returns the binary filename for external callers (e.g. to check lastModified). */
    public static String getUserBinPath(String userName) {
        return "workouts_" + userName.replaceAll("\\s+", "_") + ".bin";
    }

    /** Writes the current time (ms) to sync_<userName>.txt. */
    public static void saveSyncTimestamp(String userName) {
        String path = userSyncFile(userName);
        FileWriter writer = null;
        try {
            writer = new FileWriter(path);
            writer.write(String.valueOf(System.currentTimeMillis()));
            writer.close();
        } catch (IOException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    /** Reads the last sync timestamp from sync_<userName>.txt; returns 0 if absent. */
    public static long readSyncTimestamp(String userName) {
        String path = userSyncFile(userName);
        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }
        try {
            Scanner scanner = new Scanner(file);
            if (scanner.hasNextLong()) {
                long ts = scanner.nextLong();
                scanner.close();
                return ts;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        }
        return 0;
    }

    // ------------------------------------------------------------------ Report reading

    /** Returns the report filename for a given user. */
    private static String userReportFile(String userName) {
        return "report_" + userName.replaceAll("\\s+", "_") + ".txt";
    }

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

    // ------------------------------------------------------------------ Export report

    /**
     * Exports a readable workout history report to report_<name>.txt.
     */
    public static void exportReport(WorkoutManager manager) {
        String path = userReportFile(manager.getUser().getName());
        FileWriter writer = null;
        try {
            writer = new FileWriter(path);
            writer.write("========================================\n");
            writer.write("  WORKOUT REPORT – " + manager.getUser().getName() + "\n");
            writer.write("========================================\n\n");
            writer.write(manager.getSummary() + "\n");
            writer.write("---- Session Details ----\n\n");
            for (WorkoutSession s : manager.getSessionsSortedByDate()) {
                writer.write(s.toString() + "\n");
            }
            writer.close();
            System.out.println("Report exported to " + path + ".");
        } catch (IOException e) {
            System.err.println("An error occurred while exporting the report.");
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }
}
