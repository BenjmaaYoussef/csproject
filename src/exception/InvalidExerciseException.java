package exception;

/**
 * Thrown when an Exercise is created or modified with invalid data
 * (e.g., blank name, negative sets/reps/weight).
 */
public class InvalidExerciseException extends WorkoutAppException {

    public InvalidExerciseException(String message) {
        super(message);
    }
}
