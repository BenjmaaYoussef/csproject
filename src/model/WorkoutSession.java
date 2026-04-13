package model;

import exception.DuplicateExerciseException;

import java.util.ArrayList;

/**
 * Represents one workout session on a specific date.
 * A session contains a list of exercises and optional notes.
 */
public class WorkoutSession {

    private String date;          // format: yyyy-MM-dd
    private ArrayList<Exercise> exercises;
    private String notes;

    public WorkoutSession(String date, String notes) {
        this.date = date;
        this.notes = notes;
        this.exercises = new ArrayList<>();
    }

    /**
     * Adds an exercise to this session.
     * Throws DuplicateExerciseException if an exercise with the same name already exists.
     */
    public void addExercise(Exercise exercise) throws DuplicateExerciseException {
        for (Exercise e : exercises) {
            if (e.getName().equalsIgnoreCase(exercise.getName())) {
                throw new DuplicateExerciseException(exercise.getName());
            }
        }
        exercises.add(exercise);
    }

    /**
     * Removes an exercise by name (case-insensitive).
     * Returns true if removed, false if not found.
     */
    public boolean removeExercise(String name) {
        return exercises.removeIf(e -> e.getName().equalsIgnoreCase(name));
    }

    // ---------- Getters / Setters ----------

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public ArrayList<Exercise> getExercises() { return exercises; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Workout Session: ").append(date).append(" ===\n");
        if (notes != null && !notes.isEmpty()) {
            sb.append("Notes: ").append(notes).append("\n");
        }
        if (exercises.isEmpty()) {
            sb.append("  (no exercises logged)\n");
        } else {
            for (int i = 0; i < exercises.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(exercises.get(i)).append("\n");
            }
        }
        return sb.toString();
    }
}
