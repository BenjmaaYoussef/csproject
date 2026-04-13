package model;

import exception.WorkoutNotFoundException;
import util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Central manager for all workout data.
 *
 * Phase 2 collections used:
 *   - ArrayList<WorkoutSession>       : ordered list of sessions
 *   - HashMap<String, Exercise>       : exercise library keyed by name
 *   - TreeMap<String, Double>         : personal records (exercise → best weight), sorted by name
 *   - Pair<A,B>                       : generic utility for returning stat pairs
 */
public class WorkoutManager {

    private final ArrayList<WorkoutSession> sessions;
    private final HashMap<String, Exercise> exerciseLibrary;
    private final TreeMap<String, Double> personalRecords;
    private User user;

    public WorkoutManager(User user) {
        this.user = user;
        this.sessions = new ArrayList<>();
        this.exerciseLibrary = new HashMap<>();
        this.personalRecords = new TreeMap<>();
    }

    // ------------------------------------------------------------------ Sessions

    /** Adds a new workout session. */
    public void addSession(WorkoutSession session) {
        sessions.add(session);
        // Update personal records for all strength exercises in the session
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
        for (WorkoutSession s : sessions) {
            if (s.getDate().equals(date)) {
                return s;
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
     * The original list is not modified.
     */
    public List<WorkoutSession> getSessionsSortedByDate() {
        List<WorkoutSession> sorted = new ArrayList<>(sessions);
        Collections.sort(sorted);
        return sorted;
    }

    // ------------------------------------------------------------------ Exercise library

    /**
     * Adds an exercise to the reusable library.
     * Overwrites any existing entry with the same name (case-insensitive key).
     */
    public void addToLibrary(Exercise exercise) {
        exerciseLibrary.put(exercise.getName().toLowerCase(), exercise);
    }

    /** Returns exercise from library by name, or null if not found. */
    public Exercise getFromLibrary(String name) {
        return exerciseLibrary.get(name.toLowerCase());
    }

    /** Returns the full exercise library map (unmodifiable view). */
    public Map<String, Exercise> getExerciseLibrary() {
        return Collections.unmodifiableMap(exerciseLibrary);
    }

    // ------------------------------------------------------------------ Filtering

    /**
     * Returns all exercises of a given type across all logged sessions.
     */
    public List<Exercise> filterExercisesByType(ExerciseType type) {
        List<Exercise> result = new ArrayList<>();
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
     */
    public void updatePersonalRecord(String exerciseName, double weightKg) {
        String key = exerciseName.toLowerCase();
        Double current = personalRecords.get(key);
        if (current == null || weightKg > current) {
            personalRecords.put(key, weightKg);
        }
    }

    /**
     * Returns the personal record for the given exercise as a Pair<exerciseName, bestWeightKg>.
     * Returns null if no record exists.
     */
    public Pair<String, Double> getPersonalRecord(String exerciseName) {
        Double best = personalRecords.get(exerciseName.toLowerCase());
        if (best == null) return null;
        return new Pair<>(exerciseName, best);
    }

    /** Returns all personal records sorted by exercise name (TreeMap order). */
    public TreeMap<String, Double> getAllPersonalRecords() {
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
            for (Map.Entry<String, Double> entry : personalRecords.entrySet()) {
                sb.append(String.format("  %-20s %.1f kg%n", entry.getKey(), entry.getValue()));
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ User

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
