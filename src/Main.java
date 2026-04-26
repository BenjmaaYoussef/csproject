import db.DatabaseUtility;
import db.WorkoutSessionDAO;
import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import exception.WorkoutAppException;
import exception.WorkoutNotFoundException;
import model.*;
import network.WorkoutClient;
import util.ConnectionMode;
import util.FileManager;
import util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static WorkoutManager manager;
    private static ConnectionMode connectionMode;
    private static boolean offlineMode = false;

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/workout_tracker";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "youssef123";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════╗");
        System.out.println("║       Workout Tracker        ║");
        System.out.println("╚══════════════════════════════╝");

        // C3: Pre-login report reader
        System.out.println("View your last report before logging in? (yes/no)");
        String preAnswer = scanner.nextLine().trim();
        if (preAnswer.equalsIgnoreCase("yes")) {
            System.out.println("Enter your username:");
            String reportName = scanner.nextLine().trim();
            try {
                FileManager.readAndPrintReport(reportName);
            } catch (WorkoutAppException e) {
                System.out.println("No report found for this user.");
            }
        }

        manager = setupUser();

        // B2: Mode selection
        System.out.println("\nSelect connection mode:");
        System.out.println("  1. Direct (Database)");
        System.out.println("  2. Via Server");
        int modeChoice = readInt("Choice: ");
        connectionMode = (modeChoice == 2) ? ConnectionMode.VIA_SERVER : ConnectionMode.DIRECT_DB;

        // B4: Auto-load
        autoLoad();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choice: ");
            switch (choice) {
                case 1  -> logWorkoutSession();
                case 2  -> viewAllSessions();
                case 3  -> viewSessionByDate();
                case 4  -> filterExercisesByType();
                case 5  -> viewPersonalRecords();
                case 6  -> viewSummary();
                case 7  -> viewUserProfile();
                case 8  -> exportReport();
                case 9 -> {
                    if (connectionMode == ConnectionMode.DIRECT_DB) {
                        deleteSessionFromDB();
                    } else {
                        System.out.println("Not available in Via Server mode.");
                    }
                }
                case 0  -> running = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
        // B7: Write .bin on clean exit
        FileManager.saveUserSessions(manager.getUser().getName(), manager.getAllSessions());
        if (!offlineMode) {
            FileManager.saveSyncTimestamp(manager.getUser().getName());
        }
        System.out.println("Goodbye!");
    }

    // ------------------------------------------------------------------ Setup

    private static WorkoutManager setupUser() {
        ArrayList<User> savedUsers = FileManager.loadUsers();

        if (!savedUsers.isEmpty()) {
            System.out.println("\n--- Select Profile ---");
            for (int i = 0; i < savedUsers.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + savedUsers.get(i).getName());
            }
            System.out.println("  " + (savedUsers.size() + 1) + ". New profile");
            int choice = readInt("Choice: ");
            if (choice >= 1 && choice <= savedUsers.size()) {
                User user = savedUsers.get(choice - 1);
                System.out.println("Welcome back, " + user.getName() + "! BMI = " + String.format("%.1f", user.getBMI()));
                return new WorkoutManager(user);
            }
        }

        System.out.println("\n--- New Profile ---");
        User user = null;
        while (user == null) {
            String name = readString("Your name: ");
            int age = readInt("Age: ");
            double weight = readDouble("Weight (kg): ");
            double height = readDouble("Height (cm): ");
            try {
                user = new User(name, age, weight, height);
            } catch (WorkoutAppException e) {
                System.err.println(e.getMessage());
            }
        }

        boolean found = false;
        for (int i = 0; i < savedUsers.size(); i++) {
            if (savedUsers.get(i).getName().equalsIgnoreCase(user.getName())) {
                savedUsers.set(i, user);
                found = true;
                break;
            }
        }
        if (!found) {
            savedUsers.add(user);
        }
        FileManager.saveUsers(savedUsers);

        System.out.println("Welcome, " + user.getName() + "! BMI = " + String.format("%.1f", user.getBMI()));
        return new WorkoutManager(user);
    }

    // ------------------------------------------------------------------ Auto-load / Auto-save

    private static void autoLoad() {
        String userName = manager.getUser().getName();
        boolean connected = false;

        // Step 1: Test connectivity
        if (connectionMode == ConnectionMode.DIRECT_DB) {
            try {
                Connection conn = DatabaseUtility.getConnection(DB_URL, DB_USER, DB_PASS);
                conn.close();
                connected = true;
            } catch (SQLException e) {
                System.err.println("An error occurred.");
                e.printStackTrace();
            }
        } else { // VIA_SERVER
            try {
                Socket testSocket = new Socket("localhost", 9876);
                testSocket.close();
                connected = true;
            } catch (IOException e) {
                connected = false;
            }
        }

        File binFile = new File(FileManager.getUserBinPath(userName));
        long lastSync = FileManager.readSyncTimestamp(userName);

        // C1: Offline sync – if connected and .bin is newer than last sync, push local data first
        if (connected) {
            if (binFile.exists() && binFile.lastModified() > lastSync) {
                ArrayList<WorkoutSession> binSessions = FileManager.loadUserSessions(userName);
                if (!binSessions.isEmpty()) {
                    boolean allSynced = true;
                    int pushedCount = 0;
                    if (connectionMode == ConnectionMode.DIRECT_DB) {
                        WorkoutSessionDAO dao = new WorkoutSessionDAO();
                        // Fetch existing dates to avoid re-inserting already-synced sessions
                        ArrayList<WorkoutSession> dbSessions = dao.getAllSessions(userName);
                        ArrayList<String> existingDates = new ArrayList<>();
                        for (int i = 0; i < dbSessions.size(); i++) {
                            existingDates.add(dbSessions.get(i).getDate());
                        }
                        for (int i = 0; i < binSessions.size(); i++) {
                            WorkoutSession s = binSessions.get(i);
                            if (!existingDates.contains(s.getDate())) {
                                int result = dao.saveSession(s, userName);
                                if (result == 1) {
                                    pushedCount++;
                                } else {
                                    allSynced = false;
                                }
                            }
                        }
                    } else { // VIA_SERVER
                        // Fetch existing dates via server to avoid re-inserting already-synced sessions
                        String existingData = WorkoutClient.sendAndReceive("GET_SESSIONS:" + userName);
                        ArrayList<String> existingDates = new ArrayList<>();
                        if (!existingData.equals("NONE") && !existingData.startsWith("ERROR")) {
                            String[] lines = existingData.split("\n");
                            for (int i = 0; i < lines.length; i++) {
                                if (lines[i].startsWith("SESSION|")) {
                                    String[] parts = lines[i].split("\\|", 3);
                                    existingDates.add(parts[1]);
                                }
                            }
                        }
                        for (int i = 0; i < binSessions.size(); i++) {
                            WorkoutSession s = binSessions.get(i);
                            if (!existingDates.contains(s.getDate())) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(userName).append("\n");
                                sb.append("SESSION|").append(s.getDate()).append("|").append(s.getNotes());
                                for (Exercise e : s.getExercises()) {
                                    sb.append("\nEXERCISE|").append(e.getName()).append("|")
                                      .append(e.getType().name()).append("|").append(e.getSets())
                                      .append("|").append(e.getReps()).append("|")
                                      .append(e.getWeightKg()).append("|").append(e.getDurationMin());
                                }
                                String response = WorkoutClient.sendAndReceive("ADD_SESSION:" + sb.toString());
                                if (response.startsWith("OK")) {
                                    pushedCount++;
                                } else {
                                    allSynced = false;
                                }
                            }
                        }
                    }
                    if (pushedCount > 0 && allSynced) {
                        FileManager.saveSyncTimestamp(userName);
                        lastSync = FileManager.readSyncTimestamp(userName);
                        System.out.println("Connection restored. " + pushedCount + " session(s) synced to server.");
                    }
                }
            }
        }

        // Step 2: Normal auto-load
        ArrayList<WorkoutSession> sessions = new ArrayList<>();
        if (connected) {
            if (connectionMode == ConnectionMode.DIRECT_DB) {
                WorkoutSessionDAO dao = new WorkoutSessionDAO();
                sessions = dao.getAllSessions(userName);
            } else { // VIA_SERVER
                String response = WorkoutClient.sendAndReceive("GET_SESSIONS:" + userName);
                if (!response.equals("NONE") && !response.startsWith("ERROR")) {
                    String[] lines = response.split("\n");
                    WorkoutSession current = null;
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i].trim();
                        if (line.startsWith("SESSION|")) {
                            String[] parts = line.split("\\|", 3);
                            current = new WorkoutSession(parts[1], parts.length > 2 ? parts[2] : "");
                            sessions.add(current);
                        } else if (line.startsWith("EXERCISE|") && current != null) {
                            String[] parts = line.split("\\|");
                            try {
                                String name = parts[1];
                                ExerciseType type = ExerciseType.valueOf(parts[2]);
                                int sets = Integer.parseInt(parts[3]);
                                int reps = Integer.parseInt(parts[4]);
                                double weight = Double.parseDouble(parts[5]);
                                int duration = Integer.parseInt(parts[6]);
                                Exercise ex = new Exercise(name, sets, reps, weight, duration, type);
                                current.addExercise(ex);
                            } catch (InvalidExerciseException e) {
                                System.err.println("An error occurred.");
                                e.printStackTrace();
                            } catch (DuplicateExerciseException e) {
                                System.err.println("An error occurred.");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < sessions.size(); i++) {
                manager.addSession(sessions.get(i));
            }
            String source = (connectionMode == ConnectionMode.DIRECT_DB) ? "database" : "server";
            System.out.println("Fetched " + sessions.size() + " session(s) from " + source + ".");
            FileManager.saveUserSessions(userName, manager.getAllSessions());
            FileManager.saveSyncTimestamp(userName);
            offlineMode = false;
        } else {
            ArrayList<WorkoutSession> binSessions = FileManager.loadUserSessions(userName);
            offlineMode = true;
            if (!binSessions.isEmpty()) {
                for (int i = 0; i < binSessions.size(); i++) {
                    manager.addSession(binSessions.get(i));
                }
                System.out.println("Could not connect. Running in offline mode.");
            } else {
                System.out.println("Could not connect and no local data found. Starting fresh.");
            }
        }
    }

    private static void autoSave(WorkoutSession session) {
        String userName = manager.getUser().getName();
        if (!offlineMode) {
            if (connectionMode == ConnectionMode.DIRECT_DB) {
                WorkoutSessionDAO dao = new WorkoutSessionDAO();
                dao.saveSession(session, userName);
            } else { // VIA_SERVER
                StringBuilder sb = new StringBuilder();
                sb.append(userName).append("\n");
                sb.append("SESSION|").append(session.getDate()).append("|").append(session.getNotes());
                for (Exercise e : session.getExercises()) {
                    sb.append("\nEXERCISE|").append(e.getName()).append("|").append(e.getType().name())
                      .append("|").append(e.getSets()).append("|").append(e.getReps())
                      .append("|").append(e.getWeightKg()).append("|").append(e.getDurationMin());
                }
                WorkoutClient.sendAndReceive("ADD_SESSION:" + sb.toString());
            }
            FileManager.saveSyncTimestamp(userName);
        }
        FileManager.saveUserSessions(userName, manager.getAllSessions());
    }

    // ------------------------------------------------------------------ Menus

    private static void printMainMenu() {
        String modeLabel = (connectionMode == ConnectionMode.DIRECT_DB) ? "Direct DB" : "Via Server";
        System.out.println("\n============================== [" + modeLabel + "]");
        System.out.println(" 1. Log a new workout session");
        System.out.println(" 2. View all sessions (sorted by date)");
        System.out.println(" 3. Find session by date");
        System.out.println(" 4. Filter exercises by type");
        System.out.println(" 5. View personal records");
        System.out.println(" 6. View summary");
        System.out.println(" 7. View user profile");
        System.out.println("--- Exports ---");
        System.out.println(" 8. Export report to file");
        if (connectionMode == ConnectionMode.DIRECT_DB) {
            System.out.println("--- Database ---");
            System.out.println(" 9. Delete a session from database");
        }
        System.out.println(" 0. Exit");
        System.out.println("==============================");
    }

    // ------------------------------------------------------------------ Option 1: Log session

    private static void logWorkoutSession() {
        System.out.println("\n--- Log Workout Session ---");
        String date = readString("Date (yyyy-MM-dd): ");
        String notes = readString("Notes (or press Enter to skip): ");

        WorkoutSession session = new WorkoutSession(date, notes);

        boolean addingExercises = true;
        while (addingExercises) {
            System.out.println("\n  Add exercise? (1=Yes, 0=Done)");
            int choice = readInt("  > ");
            if (choice != 1) {
                addingExercises = false;
                continue;
            }
            try {
                Exercise e = promptExercise();
                session.addExercise(e);
                System.out.println("  Added: " + e);
            } catch (InvalidExerciseException ex) {
                System.out.println("  Error: " + ex.getMessage());
            } catch (DuplicateExerciseException ex) {
                System.out.println("  Error: " + ex.getMessage());
            }
        }

        manager.addSession(session);
        System.out.println("Session saved for " + date + " with " + session.getExercises().size() + " exercise(s).");
        autoSave(session);
    }

    private static Exercise promptExercise() throws InvalidExerciseException {
        String name = readString("  Exercise name: ");
        System.out.println("  Type: 1=STRENGTH  2=CARDIO  3=FLEXIBILITY");
        int typeChoice = readInt("  Type choice: ");
        ExerciseType type = switch (typeChoice) {
            case 2 -> ExerciseType.CARDIO;
            case 3 -> ExerciseType.FLEXIBILITY;
            default -> ExerciseType.STRENGTH;
        };

        int sets = 0, reps = 0;
        double weight = 0;
        int duration = 0;

        if (type == ExerciseType.CARDIO) {
            duration = readInt("  Duration (min): ");
        } else {
            sets   = readInt("  Sets: ");
            reps   = readInt("  Reps: ");
            weight = readDouble("  Weight (kg, 0 if bodyweight): ");
        }

        return new Exercise(name, sets, reps, weight, duration, type);
    }

    // ------------------------------------------------------------------ Option 2: View all

    private static void viewAllSessions() {
        List<WorkoutSession> sorted = manager.getSessionsSortedByDate();
        if (sorted.isEmpty()) {
            System.out.println("No sessions logged yet.");
            return;
        }
        System.out.println("\n--- All Sessions (sorted by date) ---");
        for (WorkoutSession s : sorted) {
            System.out.println(s);
        }
    }

    // ------------------------------------------------------------------ Option 3: By date

    private static void viewSessionByDate() {
        String date = readString("Enter date (yyyy-MM-dd): ");
        try {
            WorkoutSession s = manager.getSessionByDate(date);
            System.out.println(s);
        } catch (WorkoutNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ Option 4: Filter by type

    private static void filterExercisesByType() {
        System.out.println("Filter by type: 1=STRENGTH  2=CARDIO  3=FLEXIBILITY");
        int choice = readInt("> ");
        ExerciseType type = switch (choice) {
            case 2 -> ExerciseType.CARDIO;
            case 3 -> ExerciseType.FLEXIBILITY;
            default -> ExerciseType.STRENGTH;
        };

        List<Exercise> results = manager.filterExercisesByType(type);
        if (results.isEmpty()) {
            System.out.println("No " + type + " exercises found.");
            return;
        }
        System.out.println("\n--- " + type + " exercises across all sessions ---");
        for (Exercise e : results) {
            System.out.println("  " + e);
        }
    }

    // ------------------------------------------------------------------ Option 5: Personal records

    private static void viewPersonalRecords() {
        ArrayList<Pair<String, Double>> records = manager.getAllPersonalRecords();
        if (records.isEmpty()) {
            System.out.println("No personal records yet. Log sessions with weighted exercises first.");
            return;
        }
        System.out.println("\n--- Personal Records (best weight per exercise) ---");
        for (int i = 0; i < records.size(); i++) {
            Pair<String, Double> pr = records.get(i);
            System.out.printf("  %-20s %.1f kg%n", pr.getKey(), pr.getValue());
        }

        System.out.println("\nLook up a specific record? (1=Yes, 0=No)");
        if (readInt("> ") == 1) {
            String name = readString("Exercise name: ");
            Pair<String, Double> pr = manager.getPersonalRecord(name);
            if (pr == null) {
                System.out.println("No record found for \"" + name + "\".");
            } else {
                System.out.printf("Best for %s: %.1f kg%n", pr.getKey(), pr.getValue());
            }
        }
    }

    // ------------------------------------------------------------------ Option 6: Summary

    private static void viewSummary() {
        System.out.println(manager.getSummary());
    }

    // ------------------------------------------------------------------ Option 7: Profile

    private static void viewUserProfile() {
        System.out.println("\n--- User Profile ---");
        System.out.println(manager.getUser());
    }

    // ------------------------------------------------------------------ Option 8: Export report

    private static void exportReport() {
        FileManager.exportReport(manager);
    }

    // ------------------------------------------------------------------ Option 9: Delete from DB

    private static void deleteSessionFromDB() {
        String date = readString("Enter date of session to delete (yyyy-MM-dd): ");
        String userName = manager.getUser().getName();
        WorkoutSessionDAO dao = new WorkoutSessionDAO();
        int result = dao.deleteSession(date, userName);
        if (result == 1) {
            System.out.println("Session deleted successfully.");
        } else {
            System.out.println("No session found for date: " + date);
        }
    }

    // ------------------------------------------------------------------ Input helpers

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
}
