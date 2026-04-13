package exception;

/**
 * Thrown when a WorkoutSession for a given date cannot be found.
 */
public class WorkoutNotFoundException extends WorkoutAppException {

    public WorkoutNotFoundException(String date) {
        super("No workout session found for date: " + date);
    }
}
