package network;

import db.WorkoutSessionDAO;
import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import model.Exercise;
import model.ExerciseType;
import model.WorkoutSession;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class WorkoutServer {

    private static ServerSocket serverSocket;
    private static int port = 9876;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("WorkoutServer started on port " + port);
            while (true) {
                System.out.println("Waiting for client request...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(socket.getInputStream());
                String command = in.readUTF();
                System.out.println("Command received: " + command);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                String response;
                try {
                    response = handleCommand(command);
                } catch (Exception e) {
                    System.err.println("An error occurred.");
                    e.printStackTrace();
                    response = "ERROR: " + e.getMessage();
                }
                out.writeUTF(response);
                out.flush();
                in.close();
                out.close();
                socket.close();
                if (command.equalsIgnoreCase("EXIT")) {
                    break;
                }
            }
            System.out.println("Shutting down WorkoutServer.");
            serverSocket.close();
        } catch (Exception e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static String handleCommand(String command) {
        if (command.equalsIgnoreCase("EXIT")) {
            return "Server shutting down. Goodbye!";
        } else if (command.startsWith("GET_SESSIONS:")) {
            String userName = command.substring("GET_SESSIONS:".length()).trim();
            WorkoutSessionDAO dao = new WorkoutSessionDAO();
            ArrayList<WorkoutSession> sessions = dao.getAllSessions(userName);
            if (sessions.isEmpty()) {
                return "No sessions found.";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sessions.size(); i++) {
                sb.append(sessions.get(i).toString());
            }
            return sb.toString();
        } else if (command.startsWith("ADD_SESSION:")) {
            // Format: ADD_SESSION:<userName>\n<session data>
            String payload = command.substring("ADD_SESSION:".length());
            int newline = payload.indexOf('\n');
            if (newline < 0) {
                return "ERROR: Missing user name in ADD_SESSION command.";
            }
            String userName = payload.substring(0, newline).trim();
            String data = payload.substring(newline + 1);
            WorkoutSession session = parseSession(data);
            if (session == null) {
                return "ERROR: Invalid session data.";
            }
            WorkoutSessionDAO dao = new WorkoutSessionDAO();
            int result = dao.saveSession(session, userName);
            if (result == 1) {
                return "OK: Session saved for " + session.getDate() + ".";
            } else {
                return "ERROR: Could not save session.";
            }
        } else {
            return "ERROR: Unknown command: " + command;
        }
    }

    // Parses session data in the same pipe-delimited format used by FileManager (Phase 3).
    // Format: SESSION|date|notes\nEXERCISE|name|type|sets|reps|weightKg|durationMin\n...
    private static WorkoutSession parseSession(String data) {
        String[] lines = data.split("\n");
        WorkoutSession session = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("SESSION|")) {
                String[] parts = line.split("\\|", 3);
                String date  = parts[1];
                String notes = parts.length > 2 ? parts[2] : "";
                session = new WorkoutSession(date, notes);
            } else if (line.startsWith("EXERCISE|") && session != null) {
                String[] parts = line.split("\\|");
                try {
                    String name       = parts[1];
                    ExerciseType type = ExerciseType.valueOf(parts[2]);
                    int sets          = Integer.parseInt(parts[3]);
                    int reps          = Integer.parseInt(parts[4]);
                    double weightKg   = Double.parseDouble(parts[5]);
                    int durationMin   = Integer.parseInt(parts[6]);
                    Exercise e = new Exercise(name, sets, reps, weightKg, durationMin, type);
                    session.addExercise(e);
                } catch (InvalidExerciseException ex) {
                    System.err.println("An error occurred.");
                    ex.printStackTrace();
                } catch (DuplicateExerciseException ex) {
                    System.err.println("An error occurred.");
                    ex.printStackTrace();
                }
            }
        }
        return session;
    }
}
