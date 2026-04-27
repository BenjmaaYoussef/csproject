package model;

import exception.WorkoutNotFoundException;
import util.OrderedPair;
import util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WorkoutManager {

    private final ArrayList<WorkoutSession> sessions;
    private final ArrayList<Pair<String, Double>> personalRecords;
    private User user;

    public WorkoutManager(User user) {
        this.user = user;
        this.sessions = new ArrayList<>();
        this.personalRecords = new ArrayList<>();
    }

    
    public void addSession(WorkoutSession session) {
        sessions.add(session);
        for (Exercise e : session.getExercises()) {
            if (e.getWeightKg() > 0) {
                updatePersonalRecord(e.getName(), e.getWeightKg());
            }
        }
    }

    
    public WorkoutSession getSessionByDate(String date) throws WorkoutNotFoundException {
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getDate().equals(date)) {
                return sessions.get(i);
            }
        }
        throw new WorkoutNotFoundException(date);
    }

    
    public ArrayList<WorkoutSession> getAllSessions() {
        return sessions;
    }

    
    public List<WorkoutSession> getSessionsSortedByDate() {
        ArrayList<WorkoutSession> sorted = new ArrayList<>(sessions);
        Collections.sort(sorted);
        return sorted;
    }


    public List<Exercise> filterExercisesByType(ExerciseType type) {
        ArrayList<Exercise> result = new ArrayList<>();
        for (WorkoutSession session : sessions) {
            for (Exercise e : session.getExercises()) {
                if (e.getType() == type) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    
    public void updatePersonalRecord(String exerciseName, double weightKg) {
        for (int i = 0; i < personalRecords.size(); i++) {
            if (personalRecords.get(i).getKey().equalsIgnoreCase(exerciseName)) {
                if (weightKg > personalRecords.get(i).getValue()) {
                    personalRecords.set(i, new OrderedPair<>(exerciseName.toLowerCase(), weightKg));
                }
                return;
            }
        }
        personalRecords.add(new OrderedPair<>(exerciseName.toLowerCase(), weightKg));
    }

    
    public Pair<String, Double> getPersonalRecord(String exerciseName) {
        for (Pair<String, Double> pr : personalRecords) {
            if (pr.getKey().equalsIgnoreCase(exerciseName)) {
                return pr;
            }
        }
        return null;
    }

    
    public ArrayList<Pair<String, Double>> getAllPersonalRecords() {
        return personalRecords;
    }

    
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("==== Workout Summary for ").append(user.getName()).append(" ====\n");
        sb.append("Total sessions: ").append(sessions.size()).append("\n");

        int totalExercises = 0;
        for (WorkoutSession s : sessions) {
            totalExercises += s.getExercises().size();
        }
        sb.append("Total exercises logged: ").append(totalExercises).append("\n");

        if (!personalRecords.isEmpty()) {
            sb.append("\nPersonal Records:\n");
            for (Pair<String, Double> pr : personalRecords) {
                sb.append(String.format("  %-20s %.1f kg%n", pr.getKey(), pr.getValue()));
            }
        }
        return sb.toString();
    }

    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
