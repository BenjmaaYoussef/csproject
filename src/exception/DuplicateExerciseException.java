package exception;

/**
 * Thrown when an exercise with the same name is added to a WorkoutSession
 * that already contains it.
 */
public class DuplicateExerciseException extends WorkoutAppException {

    public DuplicateExerciseException(String exerciseName) {
        super("Exercise already exists in this session: " + exerciseName);
    }
}
