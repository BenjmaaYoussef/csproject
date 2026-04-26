package exception;


public class WorkoutNotFoundException extends WorkoutAppException {

    public WorkoutNotFoundException(String date) {
        super("No workout session found for date: " + date);
    }
}
