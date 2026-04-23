package gui;

import db.WorkoutSessionDAO;
import model.WorkoutManager;
import model.WorkoutSession;
import network.WorkoutClient;
import util.FileManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Data & Sync tab – exposes all I/O operations from Phases 3–6 in the GUI.
 *
 *  Phase 3 (File Handling)  : save text, load text, export report
 *  Phase 4 (Serialization)  : save binary, load binary, export XML
 *  Phase 5 (Database)       : save to DB, load from DB, delete from DB
 *  Phase 6 (Client-Server)  : fetch sessions from server, send session to server
 */
public class DataPanel extends JPanel {

    private final WorkoutManager manager;
    private final Runnable onDataChanged;

    private JTextArea outputArea;

    public DataPanel(WorkoutManager manager, Runnable onDataChanged) {
        this.manager       = manager;
        this.onDataChanged = onDataChanged;
        setBackground(AppColors.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Data & Sync");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        // ---- Button grid (4 sections)
        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setBackground(AppColors.BG);
        grid.setPreferredSize(new Dimension(0, 210));

        grid.add(buildSection("Phase 3 – File (Text)",  AppColors.PRIMARY,
            new String[]{"Save to TXT", "Load from TXT", "Export Report"},
            new Runnable[]{this::saveText, this::loadText, this::exportReport}
        ));
        grid.add(buildSection("Phase 4 – Serialization", AppColors.SUCCESS,
            new String[]{"Save Binary", "Load Binary", "Export XML"},
            new Runnable[]{this::saveBinary, this::loadBinary, this::exportXML}
        ));
        grid.add(buildSection("Phase 5 – Database", new Color(0xF4845F),
            new String[]{"Save to DB", "Load from DB", "Delete from DB"},
            new Runnable[]{this::saveToDB, this::loadFromDB, this::deleteFromDB}
        ));
        grid.add(buildSection("Phase 6 – Server", AppColors.DANGER,
            new String[]{"Fetch from Server", "Send to Server", ""},
            new Runnable[]{this::fetchFromServer, this::sendToServer, null}
        ));

        add(grid, BorderLayout.NORTH);

        // ---- Output log
        outputArea = new JTextArea();
        outputArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(0xFAFAFA));
        outputArea.setForeground(AppColors.TEXT_DARK);
        outputArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel logCard = new JPanel(new BorderLayout());
        logCard.setBackground(AppColors.CARD);
        logCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xEEEEEE)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JLabel logHeader = new JLabel("  Output Log");
        logHeader.setFont(AppColors.FONT_SMALL);
        logHeader.setForeground(AppColors.TEXT_LIGHT);
        logHeader.setOpaque(true);
        logHeader.setBackground(new Color(0xF5F5F5));
        logHeader.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        logCard.add(logHeader, BorderLayout.NORTH);
        logCard.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        add(logCard, BorderLayout.CENTER);
    }

    private JPanel buildSection(String heading, Color accent, String[] labels, Runnable[] actions) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(AppColors.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xEEEEEE)),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel(heading);
        title.setFont(AppColors.FONT_HEADING);
        title.setForeground(accent);
        card.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(labels.length, 1, 0, 8));
        buttons.setBackground(AppColors.CARD);

        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == null || labels[i].isEmpty()) {
                buttons.add(new JLabel()); // spacer
                continue;
            }
            JButton btn = sectionButton(labels[i], accent);
            final Runnable action = actions[i];
            if (action != null) {
                btn.addActionListener(e -> action.run());
            }
            buttons.add(btn);
        }

        card.add(buttons, BorderLayout.CENTER);
        return card;
    }

    private JButton sectionButton(String text, Color accent) {
        JButton btn = new JButton(text);
        btn.setFont(AppColors.FONT_BODY);
        btn.setBackground(accent);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    // ------------------------------------------------------------------ Phase 3: File (Text)

    private void saveText() {
        FileManager.saveSessions(manager.getAllSessions());
        log("Saved " + manager.getAllSessions().size() + " session(s) to workouts.txt");
    }

    private void loadText() {
        ArrayList<WorkoutSession> loaded = FileManager.loadSessions();
        for (WorkoutSession s : loaded) {
            manager.addSession(s);
        }
        log("Loaded " + loaded.size() + " session(s) from workouts.txt");
        onDataChanged.run();
    }

    private void exportReport() {
        FileManager.exportReport(manager);
        log("Report exported to report.txt");
    }

    // ------------------------------------------------------------------ Phase 4: Serialization

    private void saveBinary() {
        FileManager.saveBinary(manager.getAllSessions());
        log("Saved " + manager.getAllSessions().size() + " session(s) to workouts.bin");
    }

    private void loadBinary() {
        ArrayList<WorkoutSession> loaded = FileManager.loadBinary();
        for (WorkoutSession s : loaded) {
            manager.addSession(s);
        }
        log("Loaded " + loaded.size() + " session(s) from workouts.bin");
        onDataChanged.run();
    }

    private void exportXML() {
        FileManager.exportXML(manager.getAllSessions());
        log("Sessions exported to workouts.xml");
    }

    // ------------------------------------------------------------------ Phase 5: Database

    private void saveToDB() {
        ArrayList<WorkoutSession> sessions = manager.getAllSessions();
        if (sessions.isEmpty()) {
            log("No sessions to save.");
            return;
        }
        WorkoutSessionDAO dao = new WorkoutSessionDAO();
        int saved = 0;
        for (int i = 0; i < sessions.size(); i++) {
            if (dao.saveSession(sessions.get(i)) == 1) {
                saved++;
            }
        }
        log("Saved " + saved + " session(s) to database.");
    }

    private void loadFromDB() {
        WorkoutSessionDAO dao = new WorkoutSessionDAO();
        ArrayList<WorkoutSession> sessions = dao.getAllSessions();
        for (WorkoutSession s : sessions) {
            manager.addSession(s);
        }
        log("Loaded " + sessions.size() + " session(s) from database.");
        onDataChanged.run();
    }

    private void deleteFromDB() {
        String date = JOptionPane.showInputDialog(this,
            "Enter session date to delete (yyyy-MM-dd):", "Delete from DB",
            JOptionPane.QUESTION_MESSAGE);
        if (date == null || date.trim().isEmpty()) return;
        WorkoutSessionDAO dao = new WorkoutSessionDAO();
        int result = dao.deleteSession(date.trim());
        if (result == 1) {
            log("Deleted session for " + date + " from database.");
        } else {
            log("No session found for " + date + " in database.");
        }
    }

    // ------------------------------------------------------------------ Phase 6: Client-Server

    private void fetchFromServer() {
        log("Sending GET_SESSIONS to server...");
        String response = capture(() -> WorkoutClient.sendAndReceive("GET_SESSIONS"));
        log(response);
    }

    private void sendToServer() {
        String date = JOptionPane.showInputDialog(this,
            "Date of session to send (yyyy-MM-dd):", "Send to Server",
            JOptionPane.QUESTION_MESSAGE);
        if (date == null || date.trim().isEmpty()) return;

        // Find the session in manager
        ArrayList<WorkoutSession> sessions = manager.getAllSessions();
        WorkoutSession toSend = null;
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getDate().equals(date.trim())) {
                toSend = sessions.get(i);
                break;
            }
        }
        if (toSend == null) {
            log("No session found for date: " + date);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SESSION|").append(toSend.getDate()).append("|").append(toSend.getNotes());
        for (model.Exercise e : toSend.getExercises()) {
            sb.append("\nEXERCISE|").append(e.getName()).append("|").append(e.getType().name())
              .append("|").append(e.getSets()).append("|").append(e.getReps())
              .append("|").append(e.getWeightKg()).append("|").append(e.getDurationMin());
        }

        log("Sending ADD_SESSION to server for date " + date + "...");
        String response = capture(() -> WorkoutClient.sendAndReceive("ADD_SESSION:" + sb.toString()));
        log(response);
    }

    // ------------------------------------------------------------------ Helpers

    /** Appends a line to the output log. */
    private void log(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    /**
     * Runs an action while capturing its System.out output, then returns
     * the captured text. Used to display server responses in the log area.
     */
    private String capture(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString().trim();
    }
}
