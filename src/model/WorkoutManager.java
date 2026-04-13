package model;

import exception.WorkoutNotFoundException;
import util.OrderedPair;
import util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central manager for all workout data.
 *
 * Phase 2 collections used:
 *   - ArrayList<WorkoutSession>              : ordered list of sessions
 *   - ArrayList<Exercise>                    : exercise library (linear search by name)
 *   - ArrayList<Pair<String, Double>>        : personal records (exercise name → best weight)
 *   - Pair<K,V> / OrderedPair<K,V>          : generic key-value utility
 */
public class WorkoutManager {

    private final ArrayList<WorkoutSession> sessions;
    private final ArrayList<Exercise> exerciseLibrary;
    private final ArrayList<Pair<String, Double>> personalRecords;
    private User user;

    public WorkoutManager(User user) {
        this.user = user;
        this.sessions = new ArrayList<>();
        this.exerciseLibrary = new ArrayList<>();
        this.personalRecords = new ArrayList<>();
    }

    // ------------------------------------------------------------------ Sessions

    /** Adds a new workout session and updates personal records. */
    public void addSession(WorkoutSession session) {
        sessions.add(session);
        for (Exercise e : session.getExercises()) {
            if (e.getWeightKg() > 0) {
                updatePersonalRecord(e.getName(), e.getWeightKg());
            }
        }
    }

    /**
     * Returns the session logged on the given date (yyyy-MM-dd).
     * @throws WorkoutNotFoundException if no session exists for that date
     */
    public WorkoutSession getSessionByDate(String date) throws WorkoutNotFoundException {
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getDate().equals(date)) {
                return sessions.get(i);
            }
        }
        throw new WorkoutNotFoundException(date);
    }

    /** Returns all sessions in the order they were added. */
    public ArrayList<WorkoutSession> getAllSessions() {
        return sessions;
    }

    /**
     * Returns a new list of all sessions sorted chronologically by date.
     * Uses Collections.sort() with the natural order defined in WorkoutSession.
     */
    public List<WorkoutSession> getSessionsSortedByDate() {
        ArrayList<WorkoutSession> sorted = new ArrayList<>(sessions);
        Collections.sort(sorted);
        return sorted;
    }

    // ------------------------------------------------------------------ Exercise library

    /**
     * Adds an exercise to the reusable library.
     * Replaces any existing entry with the same name (case-insensitive).
     */
    public void addToLibrary(Exercise exercise) {
        for (int i = 0; i < exerciseLibrary.size(); i++) {
            if (exerciseLibrary.get(i).getName().equalsIgnoreCase(exercise.getName())) {
                exerciseLibrary.set(i, exercise);
                return;
            }
        }
        exerciseLibrary.add(exercise);
    }

    /** Returns exercise from library by name, or null if not found. */
    public Exercise getFromLibrary(String name) {
        for (Exercise e : exerciseLibrary) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    /** Returns the full exercise library list. */
    public ArrayList<Exercise> getExerciseLibrary() {
        return exerciseLibrary;
    }

    // ------------------------------------------------------------------ Filtering

    /**
     * Returns all exercises of a given type across all logged sessions.
     */
    public List<Exercise> filterExercisesByType(ExerciseType type) {
        ArrayList<Exercise> result = new ArrayList<>();
        for (WorkoutSession session : sessions) {
            for (Exercise e : session.getExercises()) {
                if (e.getType() == type) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ Personal records

    /**
     * Updates the personal record for an exercise if the new weight is higher.
     * Uses linear search over the ArrayList.
     */
    public void updatePersonalRecord(String exerciseName, double weightKg) {
        for (int i = 0; i < personalRecords.size(); i++) {
            if (personalRecords.get(i).getKey().equalsIgnoreCase(exerciseName)) {
                if (weightKg > personalRecords.get(i).getValue()) {
                    personalRecords.set(i, new OrderedPair<>(exerciseName.toLowerCase(), weightKg));
                }
                return;
            }
        }
        personalRecords.add(new OrderedPair<>(exerciseName.toLowerCase(), weightKg));
    }

    /**
     * Returns the personal record for the given exercise as a Pair<key, value>.
     * Returns null if no record exists.
     */
    public Pair<String, Double> getPersonalRecord(String exerciseName) {
        for (Pair<String, Double> pr : personalRecords) {
            if (pr.getKey().equalsIgnoreCase(exerciseName)) {
                return pr;
            }
        }
        return null;
    }

    /** Returns all personal records as an ArrayList of Pair<name, bestWeight>. */
    public ArrayList<Pair<String, Double>> getAllPersonalRecords() {
        return personalRecords;
    }

    // ------------------------------------------------------------------ Summary

    /**
     * Returns a human-readable summary of the user's workout history.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("==== Workout Summary for ").append(user.getName()).append(" ====\n");
        sb.append("Total sessions: ").append(sessions.size()).append("\n");

        int totalExercises = 0;
        for (WorkoutSession s : sessions) {
            totalExercises += s.getExercises().size();
        }
        sb.append("Total exercises logged: ").append(totalExercises).append("\n");

        if (!personalRecords.isEmpty()) {
            sb.append("\nPersonal Records:\n");
            for (Pair<String, Double> pr : personalRecords) {
                sb.append(String.format("  %-20s %.1f kg%n", pr.getKey(), pr.getValue()));
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ User

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
