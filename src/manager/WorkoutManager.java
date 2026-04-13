package manager;

import exception.WorkoutNotFoundException;
import model.User;
import model.WorkoutSession;

import java.util.ArrayList;

/**
 * Manages the user and their collection of workout sessions.
 * Acts as the central service layer between Main and the model classes.
 */
public class WorkoutManager {

    private User user;
    private ArrayList<WorkoutSession> sessions;

    public WorkoutManager(User user) {
        this.user = user;
        this.sessions = new ArrayList<>();
    }

    /**
     * Adds a new workout session to the manager.
     */
    public void addSession(WorkoutSession session) {
        sessions.add(session);
    }

    /**
     * Retrieves a session by date (yyyy-MM-dd).
     * Throws WorkoutNotFoundException if no session matches the date.
     */
    public WorkoutSession getSessionByDate(String date) throws WorkoutNotFoundException {
        for (WorkoutSession s : sessions) {
            if (s.getDate().equals(date)) {
                return s;
            }
        }
        throw new WorkoutNotFoundException(date);
    }

    /**
     * Removes a session by date.
     * Returns true if a session was removed, false otherwise.
     */
    public boolean removeSessionByDate(String date) {
        return sessions.removeIf(s -> s.getDate().equals(date));
    }

    /**
     * Returns all recorded sessions.
     */
    public ArrayList<WorkoutSession> getAllSessions() {
        return sessions;
    }

    /**
     * Returns a summary string showing total sessions and total exercises logged.
     */
    public String getSummary() {
        int totalExercises = 0;
        for (WorkoutSession s : sessions) {
            totalExercises += s.getExercises().size();
        }
        return String.format(
            "Total sessions: %d | Total exercises logged: %d",
            sessions.size(), totalExercises
        );
    }

    // ---------- Getters / Setters ----------

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
