import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import exception.WorkoutNotFoundException;
import manager.WorkoutManager;
import model.Exercise;
import model.ExerciseType;
import model.User;
import model.WorkoutSession;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * CS202 – Advanced OO Programming
 * Workout Tracker Application
 *
 * Entry point. Drives the console menu and demonstrates
 * custom exception handling (Chapter 2).
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static WorkoutManager manager;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("    Welcome to Workout Tracker App!");
        System.out.println("=========================================");

        // ----- Create user profile on startup -----
        User user = createUserProfile();
        manager = new WorkoutManager(user);

        System.out.println("\nProfile created! " + user);

        // ----- Main menu loop -----
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> addWorkoutSession();
                case 2 -> viewAllSessions();
                case 3 -> viewSessionByDate();
                case 4 -> addExerciseToSession();
                case 5 -> removeExerciseFromSession();
                case 6 -> deleteSession();
                case 7 -> showProfile();
                case 8 -> showSummary();
                case 0 -> {
                    System.out.println("Goodbye! Stay consistent!");
                    running = false;
                }
                default -> System.out.println("[!] Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    // ================================================================
    //  Menu Handlers
    // ================================================================

    private static void addWorkoutSession() {
        System.out.println("\n--- Add Workout Session ---");
        String date = readString("Enter date (yyyy-MM-dd): ");
        String notes = readString("Enter notes (or press Enter to skip): ");

        WorkoutSession session = new WorkoutSession(date, notes);
        manager.addSession(session);
        System.out.println("[+] Session added for " + date);
    }

    private static void viewAllSessions() {
        System.out.println("\n--- All Workout Sessions ---");
        ArrayList<WorkoutSession> sessions = manager.getAllSessions();

        if (sessions.isEmpty()) {
            System.out.println("No sessions recorded yet.");
            return;
        }

        for (WorkoutSession s : sessions) {
            System.out.println(s);
        }
    }

    private static void viewSessionByDate() {
        System.out.println("\n--- View Session by Date ---");
        String date = readString("Enter date (yyyy-MM-dd): ");

        try {
            WorkoutSession session = manager.getSessionByDate(date);
            System.out.println(session);
        } catch (WorkoutNotFoundException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private static void addExerciseToSession() {
        System.out.println("\n--- Add Exercise to Session ---");
        String date = readString("Enter session date (yyyy-MM-dd): ");

        WorkoutSession session;
        try {
            session = manager.getSessionByDate(date);
        } catch (WorkoutNotFoundException e) {
            System.out.println("[!] " + e.getMessage());
            return;
        }

        String name = readString("Exercise name: ");

        System.out.println("Type: 1) STRENGTH  2) CARDIO  3) FLEXIBILITY");
        int typeChoice = readInt("Choose type: ");
        ExerciseType type;
        switch (typeChoice) {
            case 1 -> type = ExerciseType.STRENGTH;
            case 2 -> type = ExerciseType.CARDIO;
            case 3 -> type = ExerciseType.FLEXIBILITY;
            default -> {
                System.out.println("[!] Invalid type. Defaulting to STRENGTH.");
                type = ExerciseType.STRENGTH;
            }
        }

        int sets = 0, reps = 0, duration = 0;
        double weight = 0;

        if (type == ExerciseType.CARDIO) {
            duration = readInt("Duration (minutes): ");
        } else {
            sets = readInt("Sets: ");
            reps = readInt("Reps: ");
            weight = readDouble("Weight (kg): ");
        }

        try {
            Exercise exercise = new Exercise(name, sets, reps, weight, duration, type);
            session.addExercise(exercise);
            System.out.println("[+] Exercise added: " + exercise);
        } catch (InvalidExerciseException e) {
            System.out.println("[!] Invalid exercise data – " + e.getMessage());
        } catch (DuplicateExerciseException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private static void removeExerciseFromSession() {
        System.out.println("\n--- Remove Exercise from Session ---");
        String date = readString("Enter session date (yyyy-MM-dd): ");

        WorkoutSession session;
        try {
            session = manager.getSessionByDate(date);
        } catch (WorkoutNotFoundException e) {
            System.out.println("[!] " + e.getMessage());
            return;
        }

        String exerciseName = readString("Exercise name to remove: ");
        boolean removed = session.removeExercise(exerciseName);

        if (removed) {
            System.out.println("[-] Exercise removed: " + exerciseName);
        } else {
            System.out.println("[!] Exercise not found: " + exerciseName);
        }
    }

    private static void deleteSession() {
        System.out.println("\n--- Delete Session ---");
        String date = readString("Enter session date to delete (yyyy-MM-dd): ");

        boolean removed = manager.removeSessionByDate(date);
        if (removed) {
            System.out.println("[-] Session for " + date + " deleted.");
        } else {
            System.out.println("[!] No session found for " + date);
        }
    }

    private static void showProfile() {
        System.out.println("\n--- User Profile ---");
        System.out.println(manager.getUser());
    }

    private static void showSummary() {
        System.out.println("\n--- Summary ---");
        System.out.println(manager.getSummary());
    }

    // ================================================================
    //  Helper Methods
    // ================================================================

    private static User createUserProfile() {
        System.out.println("\nLet's set up your profile first.");
        String name = readString("Your name: ");
        int age = readInt("Your age: ");
        double weight = readDouble("Your weight (kg): ");
        double height = readDouble("Your height (cm): ");
        return new User(name, age, weight, height);
    }

    private static void printMainMenu() {
        System.out.println("\n=========================================");
        System.out.println("               MAIN MENU");
        System.out.println("=========================================");
        System.out.println(" 1. Add workout session");
        System.out.println(" 2. View all sessions");
        System.out.println(" 3. View session by date");
        System.out.println(" 4. Add exercise to session");
        System.out.println(" 5. Remove exercise from session");
        System.out.println(" 6. Delete session");
        System.out.println(" 7. View profile");
        System.out.println(" 8. View summary");
        System.out.println(" 0. Exit");
        System.out.println("=========================================");
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("[!] Please enter a valid integer.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("[!] Please enter a valid number.");
            }
        }
    }
}
