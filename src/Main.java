import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import exception.WorkoutNotFoundException;
import model.*;
import util.FileManager;
import util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Workout Tracker console application.
 * Phase 3: File Handling
 */
public class Main {

    private static WorkoutManager manager;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════╗");
        System.out.println("║     Workout Tracker v4.0     ║");
        System.out.println("╚══════════════════════════════╝");

        manager = setupUser();

        ArrayList<WorkoutSession> saved = FileManager.loadBinary();
        for (WorkoutSession s : saved) {
            manager.addSession(s);
        }

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
                case 9  -> exportXML();
                case 0  -> running = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    // ------------------------------------------------------------------ Setup

    private static WorkoutManager setupUser() {
        System.out.println("\n--- Profile Setup ---");
        String name = readString("Your name: ");
        int age = readInt("Age: ");
        double weight = readDouble("Weight (kg): ");
        double height = readDouble("Height (cm): ");
        User user = new User(name, age, weight, height);
        System.out.println("Welcome, " + name + "! BMI = " + String.format("%.1f", user.getBMI()));
        return new WorkoutManager(user);
    }

    // ------------------------------------------------------------------ Menus

    private static void printMainMenu() {
        System.out.println("\n==============================");
        System.out.println(" 1. Log a new workout session");
        System.out.println(" 2. View all sessions (sorted by date)");
        System.out.println(" 3. Find session by date");
        System.out.println(" 4. Filter exercises by type");
        System.out.println(" 5. View personal records");
        System.out.println(" 6. View summary");
        System.out.println(" 7. View user profile");
        System.out.println(" 8. Export report to file");
        System.out.println(" 9. Export sessions to XML");
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
        FileManager.saveBinary(manager.getAllSessions());
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

    // ------------------------------------------------------------------ Option 9: Export to XML

    private static void exportXML() {
        FileManager.exportXML(manager.getAllSessions());
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
