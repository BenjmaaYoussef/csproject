package model;

import exception.InvalidExerciseException;
import java.io.Serializable;

public class Exercise implements Serializable {

    private static final long serialVersionUID = 1;

    private String name;
    private int sets;
    private int reps;
    private double weightKg;
    private int durationMin;
    private ExerciseType type;

    public Exercise() {
        this.name = "default";
        this.type = ExerciseType.STRENGTH;
    }

    public Exercise(String name, int sets, int reps, double weightKg,
                    int durationMin, ExerciseType type) throws InvalidExerciseException {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
        this.durationMin = durationMin;
        this.type = type;
        validate();
    }

    
    private void validate() throws InvalidExerciseException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidExerciseException("Exercise name cannot be blank.");
        }
        if (sets < 0) {
            throw new InvalidExerciseException("Sets cannot be negative.");
        }
        if (reps < 0) {
            throw new InvalidExerciseException("Reps cannot be negative.");
        }
        if (weightKg < 0) {
            throw new InvalidExerciseException("Weight cannot be negative.");
        }
        if (durationMin < 0) {
            throw new InvalidExerciseException("Duration cannot be negative.");
        }
        if (type == null) {
            throw new InvalidExerciseException("Exercise type cannot be null.");
        }
    }

    
    public String getName() { return name; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public double getWeightKg() { return weightKg; }
    public int getDurationMin() { return durationMin; }
    public ExerciseType getType() { return type; }

    
    public void setName(String name) throws InvalidExerciseException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidExerciseException("Exercise name cannot be blank.");
        }
        this.name = name;
    }

    public void setSets(int sets) throws InvalidExerciseException {
        if (sets < 0) throw new InvalidExerciseException("Sets cannot be negative.");
        this.sets = sets;
    }

    public void setReps(int reps) throws InvalidExerciseException {
        if (reps < 0) throw new InvalidExerciseException("Reps cannot be negative.");
        this.reps = reps;
    }

    public void setWeightKg(double weightKg) throws InvalidExerciseException {
        if (weightKg < 0) throw new InvalidExerciseException("Weight cannot be negative.");
        this.weightKg = weightKg;
    }

    public void setDurationMin(int durationMin) throws InvalidExerciseException {
        if (durationMin < 0) throw new InvalidExerciseException("Duration cannot be negative.");
        this.durationMin = durationMin;
    }

    public void setType(ExerciseType type) throws InvalidExerciseException {
        if (type == null) throw new InvalidExerciseException("Exercise type cannot be null.");
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == ExerciseType.CARDIO) {
            return String.format("[%s] %s – %d min", type, name, durationMin);
        }
        return String.format("[%s] %s – %d sets x %d reps @ %.1f kg",
                type, name, sets, reps, weightKg);
    }
}
