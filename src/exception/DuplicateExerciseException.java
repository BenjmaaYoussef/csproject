package exception;


public class DuplicateExerciseException extends WorkoutAppException {

    public DuplicateExerciseException(String exerciseName) {
        super("Exercise already exists in this session: " + exerciseName);
    }
}
