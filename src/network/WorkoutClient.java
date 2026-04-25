package network;

import model.ExerciseType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class WorkoutClient {

    private static final String HOST = "localhost";
    private static final int PORT = 9876;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        System.out.println("WorkoutClient started. Server: " + HOST + ":" + PORT);
        System.out.print("Your name: ");
        String userName = scanner.nextLine().trim();

        while (running) {
            System.out.println("\n=== Client Menu ===");
            System.out.println("1. Get all sessions from server");
            System.out.println("2. Add a session to server");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String line = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
                continue;
            }
            if (choice == 0) {
                sendAndReceive("EXIT");
                running = false;
            } else if (choice == 1) {
                sendAndReceive("GET_SESSIONS:" + userName);
            } else if (choice == 2) {
                String sessionData = buildSessionData(scanner, userName);
                sendAndReceive("ADD_SESSION:" + sessionData);
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
        System.out.println("WorkoutClient terminated.");
    }

    public static String sendAndReceive(String command) {
        Socket socket = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        String response = "ERROR: No response.";
        try {
            socket = new Socket(HOST, PORT);
            System.out.println("Connected to " + socket.getRemoteSocketAddress());
            out = new DataOutputStream(socket.getOutputStream());
            in  = new DataInputStream(socket.getInputStream());
            out.writeUTF(command);
            out.flush();
            response = in.readUTF();
            System.out.println("Server response:\n" + response);
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        }
        return response;
    }

    // Builds a pipe-delimited session string matching the FileManager (Phase 3) format.
    // Format: <userName>\nSESSION|date|notes\nEXERCISE|name|type|sets|reps|weightKg|durationMin\n...
    private static String buildSessionData(Scanner scanner, String userName) {
        System.out.print("Date (yyyy-MM-dd): ");
        String date = scanner.nextLine().trim();
        System.out.print("Notes (or press Enter to skip): ");
        String notes = scanner.nextLine().trim();
        StringBuilder sb = new StringBuilder();
        sb.append(userName).append("\n");
        sb.append("SESSION|").append(date).append("|").append(notes);
        boolean addingExercises = true;
        while (addingExercises) {
            System.out.println("Add exercise? (1=Yes, 0=Done)");
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (!input.equals("1")) {
                addingExercises = false;
                continue;
            }
            System.out.print("Exercise name: ");
            String name = scanner.nextLine().trim();
            System.out.println("Type: 1=STRENGTH  2=CARDIO  3=FLEXIBILITY");
            System.out.print("Type choice: ");
            int typeChoice = 0;
            try {
                typeChoice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, defaulting to STRENGTH.");
            }
            ExerciseType type;
            if      (typeChoice == 2) type = ExerciseType.CARDIO;
            else if (typeChoice == 3) type = ExerciseType.FLEXIBILITY;
            else                      type = ExerciseType.STRENGTH;
            int sets = 0, reps = 0, duration = 0;
            double weight = 0;
            if (type == ExerciseType.CARDIO) {
                System.out.print("Duration (min): ");
                try {
                    duration = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, defaulting to 0.");
                }
            } else {
                System.out.print("Sets: ");
                try {
                    sets = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, defaulting to 0.");
                }
                System.out.print("Reps: ");
                try {
                    reps = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, defaulting to 0.");
                }
                System.out.print("Weight (kg, 0 if bodyweight): ");
                try {
                    weight = Double.parseDouble(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, defaulting to 0.");
                }
            }
            sb.append("\nEXERCISE|").append(name).append("|").append(type.name())
              .append("|").append(sets).append("|").append(reps)
              .append("|").append(weight).append("|").append(duration);
        }
        return sb.toString();
    }
}
