package exception;


public class WorkoutAppException extends Exception {

    public WorkoutAppException(String message) {
        super(message);
    }

    public WorkoutAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
