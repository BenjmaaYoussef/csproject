package exception;

/**
 * Base checked exception for all Workout App errors.
 * All custom exceptions extend this class.
 */
public class WorkoutAppException extends Exception {

    public WorkoutAppException(String message) {
        super(message);
    }

    public WorkoutAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
