package util;

import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import model.Exercise;
import model.ExerciseType;
import model.WorkoutManager;
import model.WorkoutSession;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Handles all file I/O for the Workout Tracker app.
 * Phase 3: File Handling using FileWriter and Scanner.
 *
 * File format (workouts.txt):
 *   SESSION|yyyy-MM-dd|notes
 *   EXERCISE|name|type|sets|reps|weightKg|durationMin
 */
public class FileManager {

    private static final String WORKOUTS_FILE = "workouts.txt";
    private static final String REPORT_FILE   = "report.txt";

    // ------------------------------------------------------------------ Save

    /**
     * Saves all sessions to workouts.txt (overwrites existing file).
     */
    public static void saveSessions(ArrayList<WorkoutSession> sessions) {
        try {
            FileWriter writer = new FileWriter(WORKOUTS_FILE);
            for (WorkoutSession session : sessions) {
                writer.write("SESSION|" + session.getDate() + "|" + session.getNotes() + "\n");
                for (Exercise e : session.getExercises()) {
                    writer.write("EXERCISE|" + e.getName() + "|" + e.getType() + "|"
                            + e.getSets() + "|" + e.getReps() + "|"
                            + e.getWeightKg() + "|" + e.getDurationMin() + "\n");
                }
            }
            writer.close();
            System.out.println("Sessions saved to " + WORKOUTS_FILE + ".");
        } catch (IOException e) {
            System.err.println("An error occurred while saving sessions.");
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ Load

    /**
     * Loads sessions from workouts.txt.
     * Returns an empty list if the file does not exist.
     */
    public static ArrayList<WorkoutSession> loadSessions() {
        ArrayList<WorkoutSession> sessions = new ArrayList<>();
        File file = new File(WORKOUTS_FILE);
        if (!file.exists()) {
            return sessions;
        }
        try {
            Scanner reader = new Scanner(file);
            WorkoutSession current = null;
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.startsWith("SESSION|")) {
                    String[] parts = line.split("\\|", 3);
                    String date  = parts[1];
                    String notes = parts.length > 2 ? parts[2] : "";
                    current = new WorkoutSession(date, notes);
                    sessions.add(current);
                } else if (line.startsWith("EXERCISE|") && current != null) {
                    String[] parts = line.split("\\|");
                    String name     = parts[1];
                    ExerciseType type = ExerciseType.valueOf(parts[2]);
                    int    sets     = Integer.parseInt(parts[3]);
                    int    reps     = Integer.parseInt(parts[4]);
                    double weight   = Double.parseDouble(parts[5]);
                    int    duration = Integer.parseInt(parts[6]);
                    Exercise e = new Exercise(name, sets, reps, weight, duration, type);
                    current.addExercise(e);
                }
            }
            reader.close();
            System.out.println("Loaded " + sessions.size() + " session(s) from " + WORKOUTS_FILE + ".");
        } catch (FileNotFoundException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        } catch (InvalidExerciseException | DuplicateExerciseException e) {
            System.err.println("Error reading exercise data: " + e.getMessage());
        }
        return sessions;
    }

    // ------------------------------------------------------------------ Export report

    /**
     * Exports a readable workout history report to report.txt.
     */
    public static void exportReport(WorkoutManager manager) {
        try {
            FileWriter writer = new FileWriter(REPORT_FILE);
            writer.write("========================================\n");
            writer.write("  WORKOUT REPORT – " + manager.getUser().getName() + "\n");
            writer.write("========================================\n\n");
            writer.write(manager.getSummary() + "\n");
            writer.write("---- Session Details ----\n\n");
            for (WorkoutSession s : manager.getSessionsSortedByDate()) {
                writer.write(s.toString() + "\n");
            }
            writer.close();
            System.out.println("Report exported to " + REPORT_FILE + ".");
        } catch (IOException e) {
            System.err.println("An error occurred while exporting the report.");
            e.printStackTrace();
        }
    }
}
