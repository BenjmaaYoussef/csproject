package network;

import db.WorkoutSessionDAO;
import model.Exercise;
import model.WorkoutSession;
import util.FileManager;

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
                return "NONE";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sessions.size(); i++) {
                WorkoutSession s = sessions.get(i);
                sb.append("SESSION|").append(s.getDate()).append("|").append(s.getNotes()).append("\n");
                ArrayList<Exercise> exercises = s.getExercises();
                for (int j = 0; j < exercises.size(); j++) {
                    Exercise e = exercises.get(j);
                    sb.append("EXERCISE|").append(e.getName()).append("|").append(e.getType().name())
                      .append("|").append(e.getSets()).append("|").append(e.getReps())
                      .append("|").append(e.getWeightKg()).append("|").append(e.getDurationMin()).append("\n");
                }
            }
            return sb.toString();
        } else if (command.startsWith("ADD_SESSION:")) {
            
            String payload = command.substring("ADD_SESSION:".length());
            int newline = payload.indexOf('\n');
            if (newline < 0) {
                return "ERROR: Missing user name in ADD_SESSION command.";
            }
            String userName = payload.substring(0, newline).trim();
            String data = payload.substring(newline + 1);
            WorkoutSession session = FileManager.parseSessionFromText(data);
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

}
