package model;

import exception.DuplicateExerciseException;
import java.io.Serializable;
import java.util.ArrayList;

public class WorkoutSession implements Comparable<WorkoutSession>, Serializable {

    private static final long serialVersionUID = 1;

    private String date;
    private ArrayList<Exercise> exercises;
    private String notes;

    public WorkoutSession() {
        this.date = "";
        this.notes = "";
        this.exercises = new ArrayList<>();
    }

    public WorkoutSession(String date, String notes) {
        this.date = date;
        this.notes = notes;
        this.exercises = new ArrayList<>();
    }

    /**
     * Adds an exercise to this session.
     * @throws DuplicateExerciseException if an exercise with the same name already exists
     */
    public void addExercise(Exercise e) throws DuplicateExerciseException {
        for (Exercise existing : exercises) {
            if (existing.getName().equalsIgnoreCase(e.getName())) {
                throw new DuplicateExerciseException(e.getName());
            }
        }
        exercises.add(e);
    }

    /**
     * Removes an exercise by name (case-insensitive). No-op if not found.
     */
    public void removeExercise(String name) {
        exercises.removeIf(e -> e.getName().equalsIgnoreCase(name));
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /** Natural order: chronological by date string (yyyy-MM-dd sorts correctly lexicographically). */
    @Override
    public int compareTo(WorkoutSession other) {
        return this.date.compareTo(other.date);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Workout [").append(date).append("] ===\n");
        if (notes != null && !notes.isEmpty()) {
            sb.append("Notes: ").append(notes).append("\n");
        }
        if (exercises.isEmpty()) {
            sb.append("  (no exercises)\n");
        } else {
            for (Exercise e : exercises) {
                sb.append("  • ").append(e).append("\n");
            }
        }
        return sb.toString();
    }
}
