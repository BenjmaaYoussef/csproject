package db;

import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import model.Exercise;
import model.ExerciseType;
import model.WorkoutSession;

import java.sql.*;
import java.util.ArrayList;

public class WorkoutSessionDAO {

    private static final String URL       = "jdbc:mysql://localhost:3306/workout_tracker";
    private static final String USER_NAME = "root";
    private static final String PASSWORD  = "3152005medA";

    public int saveSession(WorkoutSession session) {
        Connection connection = null;
        PreparedStatement insertSessionStmt = null;
        PreparedStatement insertExerciseStmt = null;
        ResultSet generatedKeys = null;
        int rowsUpdated = 0;
        try {
            connection = DatabaseUtility.getConnection(URL, USER_NAME, PASSWORD);
            connection.setAutoCommit(false);

            String sessionQuery = "INSERT INTO workout_sessions (session_date, notes) VALUES (?, ?)";
            insertSessionStmt = connection.prepareStatement(sessionQuery, Statement.RETURN_GENERATED_KEYS);
            insertSessionStmt.setString(1, session.getDate());
            insertSessionStmt.setString(2, session.getNotes());
            insertSessionStmt.executeUpdate();

            generatedKeys = insertSessionStmt.getGeneratedKeys();
            int sessionId = -1;
            if (generatedKeys.next()) {
                sessionId = generatedKeys.getInt(1);
            }

            String exerciseQuery = "INSERT INTO session_exercises (session_id, name, exercise_type, sets, reps, weight_kg, duration_min) VALUES (?, ?, ?, ?, ?, ?, ?)";
            insertExerciseStmt = connection.prepareStatement(exerciseQuery);
            ArrayList<Exercise> exercises = session.getExercises();
            for (int i = 0; i < exercises.size(); i++) {
                Exercise e = exercises.get(i);
                insertExerciseStmt.setInt(1, sessionId);
                insertExerciseStmt.setString(2, e.getName());
                insertExerciseStmt.setString(3, e.getType().name());
                insertExerciseStmt.setInt(4, e.getSets());
                insertExerciseStmt.setInt(5, e.getReps());
                insertExerciseStmt.setDouble(6, e.getWeightKg());
                insertExerciseStmt.setInt(7, e.getDurationMin());
                insertExerciseStmt.executeUpdate();
            }

            connection.commit();
            rowsUpdated = 1;
        } catch (SQLException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    System.err.println("An error occurred.");
                    ex.printStackTrace();
                }
            }
        } finally {
            if (generatedKeys != null) {
                try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (insertExerciseStmt != null) {
                try { insertExerciseStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (insertSessionStmt != null) {
                try { insertSessionStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return rowsUpdated;
    }

    public ArrayList<WorkoutSession> getAllSessions() {
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet rs = null;
        ArrayList<WorkoutSession> sessions = new ArrayList<>();
        try {
            connection = DatabaseUtility.getConnection(URL, USER_NAME, PASSWORD);

            String sqlQuery =
                "SELECT ws.id, ws.session_date, ws.notes, " +
                "se.name, se.exercise_type, se.sets, se.reps, se.weight_kg, se.duration_min " +
                "FROM workout_sessions ws " +
                "LEFT JOIN session_exercises se ON ws.id = se.session_id " +
                "ORDER BY ws.id";

            selectStatement = connection.prepareStatement(sqlQuery);
            rs = selectStatement.executeQuery();

            int lastId = -1;
            WorkoutSession currentSession = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != lastId) {
                    if (currentSession != null) {
                        sessions.add(currentSession);
                    }
                    String date  = rs.getString("session_date");
                    String notes = rs.getString("notes");
                    currentSession = new WorkoutSession(date, notes);
                    lastId = id;
                }
                String exerciseName = rs.getString("name");
                if (exerciseName != null) {
                    String typeStr   = rs.getString("exercise_type");
                    int sets         = rs.getInt("sets");
                    int reps         = rs.getInt("reps");
                    double weightKg  = rs.getDouble("weight_kg");
                    int durationMin  = rs.getInt("duration_min");
                    ExerciseType type = ExerciseType.valueOf(typeStr);
                    try {
                        Exercise e = new Exercise(exerciseName, sets, reps, weightKg, durationMin, type);
                        currentSession.addExercise(e);
                    } catch (InvalidExerciseException ex) {
                        System.err.println("An error occurred.");
                        ex.printStackTrace();
                    } catch (DuplicateExerciseException ex) {
                        System.err.println("An error occurred.");
                        ex.printStackTrace();
                    }
                }
            }
            if (currentSession != null) {
                sessions.add(currentSession);
            }
        } catch (SQLException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (selectStatement != null) {
                try { selectStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return sessions;
    }

    public int deleteSession(String date) {
        Connection connection = null;
        PreparedStatement deleteStatement = null;
        int rowsUpdated = 0;
        try {
            connection = DatabaseUtility.getConnection(URL, USER_NAME, PASSWORD);

            String sqlQuery = "DELETE FROM workout_sessions WHERE session_date = ?";
            deleteStatement = connection.prepareStatement(sqlQuery);
            deleteStatement.setString(1, date);
            rowsUpdated = deleteStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        } finally {
            if (deleteStatement != null) {
                try { deleteStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return rowsUpdated;
    }

    public int updateSessionNotes(String date, String newNotes) {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        int rowsUpdated = 0;
        try {
            connection = DatabaseUtility.getConnection(URL, USER_NAME, PASSWORD);

            String sqlQuery = "UPDATE workout_sessions SET notes = ? WHERE session_date = ?";
            updateStatement = connection.prepareStatement(sqlQuery);
            updateStatement.setString(1, newNotes);
            updateStatement.setString(2, date);
            rowsUpdated = updateStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        } finally {
            if (updateStatement != null) {
                try { updateStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return rowsUpdated;
    }
}
