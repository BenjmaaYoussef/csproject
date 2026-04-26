package manager;

import exception.WorkoutNotFoundException;
import model.User;
import model.WorkoutSession;

import java.util.ArrayList;


public class WorkoutManager {

    private User user;
    private ArrayList<WorkoutSession> sessions;

    public WorkoutManager(User user) {
        this.user = user;
        this.sessions = new ArrayList<>();
    }

    
    public void addSession(WorkoutSession session) {
        sessions.add(session);
    }

    
    public WorkoutSession getSessionByDate(String date) throws WorkoutNotFoundException {
        for (WorkoutSession s : sessions) {
            if (s.getDate().equals(date)) {
                return s;
            }
        }
        throw new WorkoutNotFoundException(date);
    }

    
    public boolean removeSessionByDate(String date) {
        return sessions.removeIf(s -> s.getDate().equals(date));
    }

    
    public ArrayList<WorkoutSession> getAllSessions() {
        return sessions;
    }

    
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

    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
