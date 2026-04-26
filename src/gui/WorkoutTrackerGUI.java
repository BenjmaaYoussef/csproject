package gui;

import db.DatabaseUtility;
import db.WorkoutSessionDAO;
import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import exception.WorkoutAppException;
import model.Exercise;
import model.ExerciseType;
import model.User;
import model.WorkoutManager;
import model.WorkoutSession;
import network.WorkoutClient;
import util.ConnectionMode;
import util.FileManager;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Phase 7 – Main Swing GUI for the Workout Tracker.
 *
 * Layout:
 *   JFrame
 *   └── header bar (title + user name)
 *   └── JTabbedPane
 *         ├── Dashboard  (DashboardPanel)
 *         ├── Add Workout (AddWorkoutPanel)
 *         ├── History    (HistoryPanel)
 *         ├── Profile    (ProfilePanel)
 *         └── Data & Sync (DataPanel)  – Phases 3-6 file/DB/network ops
 *   └── status bar
 */
public class WorkoutTrackerGUI extends JFrame {

    public static ConnectionMode connectionMode = ConnectionMode.DIRECT_DB;
    public static boolean offlineMode = false;

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/workout_tracker";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "youssef123";

    private WorkoutManager manager;

    private DashboardPanel   dashboardPanel;
    private AddWorkoutPanel  addWorkoutPanel;
    private HistoryPanel     historyPanel;
    private ProfilePanel     profilePanel;
    private DataPanel        dataPanel;

    private JLabel statusLabel;
    private JLabel userLabel;

    public WorkoutTrackerGUI() {
        super("Workout Tracker");
        initLookAndFeel();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));

        // C4: Pre-login report reader
        int reportChoice = JOptionPane.showConfirmDialog(
            null,
            "View your last report before logging in?",
            "Pre-Login Report",
            JOptionPane.YES_NO_OPTION
        );
        if (reportChoice == JOptionPane.YES_OPTION) {
            String reportName = JOptionPane.showInputDialog(null, "Enter your username:");
            if (reportName != null && !reportName.trim().isEmpty()) {
                try {
                    String content = FileManager.readReport(reportName.trim());
                    JTextArea textArea = new JTextArea(content);
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
                    JOptionPane.showMessageDialog(null, scrollPane,
                        "Report for " + reportName.trim(), JOptionPane.INFORMATION_MESSAGE);
                } catch (WorkoutAppException e) {
                    JOptionPane.showMessageDialog(null, "No report found for this user.",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        // 1. Ask user for profile and connection mode
        User user = showProfileDialog();
        if (user == null) {
            System.exit(0);
        }

        // 2. Create manager
        manager = new WorkoutManager(user);

        // 3. Auto-load sessions from chosen source
        autoLoad(user.getName());

        buildUI();
        refreshAll();

        setSize(1050, 680);
        setLocationRelativeTo(null);

        // 4. Write .bin on clean exit
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                FileManager.saveUserSessions(
                    manager.getUser().getName(), manager.getAllSessions());
                if (!offlineMode) {
                    FileManager.saveSyncTimestamp(manager.getUser().getName());
                }
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void autoLoad(String userName) {
        boolean connected = false;

        // Step 1: Test connectivity
        if (connectionMode == ConnectionMode.DIRECT_DB) {
            Connection conn = DatabaseUtility.getConnection(DB_URL, DB_USER, DB_PASS);
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
                connected = true;
            }
        } else { // VIA_SERVER
            try {
                Socket testSocket = new Socket("localhost", 9876);
                testSocket.close();
                connected = true;
            } catch (IOException e) {
                connected = false;
            }
        }

        File binFile = new File(FileManager.getUserBinPath(userName));
        long lastSync = FileManager.readSyncTimestamp(userName);

        // C1: Offline sync – if connected and .bin is newer than last sync, push local data first
        if (connected) {
            if (binFile.exists() && binFile.lastModified() > lastSync) {
                ArrayList<WorkoutSession> binSessions = FileManager.loadUserSessions(userName);
                if (!binSessions.isEmpty()) {
                    boolean allSynced = true;
                    int pushedCount = 0;
                    if (connectionMode == ConnectionMode.DIRECT_DB) {
                        WorkoutSessionDAO dao = new WorkoutSessionDAO();
                        // Fetch existing dates to avoid re-inserting already-synced sessions
                        ArrayList<WorkoutSession> dbSessions = dao.getAllSessions(userName);
                        ArrayList<String> existingDates = new ArrayList<>();
                        for (int i = 0; i < dbSessions.size(); i++) {
                            existingDates.add(dbSessions.get(i).getDate());
                        }
                        for (int i = 0; i < binSessions.size(); i++) {
                            WorkoutSession s = binSessions.get(i);
                            if (!existingDates.contains(s.getDate())) {
                                int result = dao.saveSession(s, userName);
                                if (result == 1) {
                                    pushedCount++;
                                } else {
                                    allSynced = false;
                                }
                            }
                        }
                    } else { // VIA_SERVER
                        // Fetch existing dates via server to avoid re-inserting already-synced sessions
                        String existingData = WorkoutClient.sendAndReceive("GET_SESSIONS:" + userName);
                        ArrayList<String> existingDates = new ArrayList<>();
                        if (!existingData.equals("NONE") && !existingData.startsWith("ERROR")) {
                            String[] lines = existingData.split("\n");
                            for (int i = 0; i < lines.length; i++) {
                                if (lines[i].startsWith("SESSION|")) {
                                    String[] parts = lines[i].split("\\|", 3);
                                    existingDates.add(parts[1]);
                                }
                            }
                        }
                        for (int i = 0; i < binSessions.size(); i++) {
                            WorkoutSession s = binSessions.get(i);
                            if (!existingDates.contains(s.getDate())) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(userName).append("\n");
                                sb.append("SESSION|").append(s.getDate()).append("|").append(s.getNotes());
                                for (Exercise e : s.getExercises()) {
                                    sb.append("\nEXERCISE|").append(e.getName()).append("|")
                                      .append(e.getType().name()).append("|").append(e.getSets())
                                      .append("|").append(e.getReps()).append("|")
                                      .append(e.getWeightKg()).append("|").append(e.getDurationMin());
                                }
                                String response = WorkoutClient.sendAndReceive("ADD_SESSION:" + sb.toString());
                                if (response.startsWith("OK")) {
                                    pushedCount++;
                                } else {
                                    allSynced = false;
                                }
                            }
                        }
                    }
                    if (pushedCount > 0 && allSynced) {
                        FileManager.saveSyncTimestamp(userName);
                        lastSync = FileManager.readSyncTimestamp(userName);
                        final int finalPushedCount = pushedCount;
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            WorkoutTrackerGUI.this,
                            "Connection restored. " + finalPushedCount + " session(s) synced to server.",
                            "Sync", JOptionPane.INFORMATION_MESSAGE));
                    }
                }
            }
        }

        // Step 2: Normal auto-load
        ArrayList<WorkoutSession> sessions = new ArrayList<>();
        if (connected) {
            if (connectionMode == ConnectionMode.DIRECT_DB) {
                WorkoutSessionDAO dao = new WorkoutSessionDAO();
                sessions = dao.getAllSessions(userName);
            } else { // VIA_SERVER
                String response = WorkoutClient.sendAndReceive("GET_SESSIONS:" + userName);
                if (!response.equals("NONE") && !response.startsWith("ERROR")) {
                    String[] lines = response.split("\n");
                    WorkoutSession current = null;
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i].trim();
                        if (line.startsWith("SESSION|")) {
                            String[] parts = line.split("\\|", 3);
                            current = new WorkoutSession(parts[1], parts.length > 2 ? parts[2] : "");
                            sessions.add(current);
                        } else if (line.startsWith("EXERCISE|") && current != null) {
                            String[] parts = line.split("\\|");
                            try {
                                String name = parts[1];
                                ExerciseType type = ExerciseType.valueOf(parts[2]);
                                int sets = Integer.parseInt(parts[3]);
                                int reps = Integer.parseInt(parts[4]);
                                double weight = Double.parseDouble(parts[5]);
                                int duration = Integer.parseInt(parts[6]);
                                Exercise ex = new Exercise(name, sets, reps, weight, duration, type);
                                current.addExercise(ex);
                            } catch (InvalidExerciseException e) {
                                System.err.println("An error occurred.");
                                e.printStackTrace();
                            } catch (DuplicateExerciseException e) {
                                System.err.println("An error occurred.");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < sessions.size(); i++) {
                manager.addSession(sessions.get(i));
            }
            String source = (connectionMode == ConnectionMode.DIRECT_DB) ? "database" : "server";
            System.out.println("Fetched " + sessions.size() + " session(s) from " + source + ".");
            FileManager.saveUserSessions(userName, manager.getAllSessions());
            FileManager.saveSyncTimestamp(userName);
            offlineMode = false;
        } else {
            ArrayList<WorkoutSession> binSessions = FileManager.loadUserSessions(userName);
            offlineMode = true;
            if (!binSessions.isEmpty()) {
                for (int i = 0; i < binSessions.size(); i++) {
                    manager.addSession(binSessions.get(i));
                }
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    WorkoutTrackerGUI.this,
                    "Could not connect. Using latest saved version from disk.",
                    "Offline Mode", JOptionPane.WARNING_MESSAGE));
            } else {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    WorkoutTrackerGUI.this,
                    "Could not connect and no local data found. Starting fresh.",
                    "Offline Mode", JOptionPane.WARNING_MESSAGE));
            }
        }
    }

    // ---------------------------------------------------------------------- UI

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.BG);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel appTitle = new JLabel("Workout Tracker");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(Color.WHITE);
        header.add(appTitle, BorderLayout.WEST);

        userLabel = new JLabel("", SwingConstants.RIGHT);
        userLabel.setFont(AppColors.FONT_BODY);
        userLabel.setForeground(new Color(0xCCDDFF));
        header.add(userLabel, BorderLayout.EAST);

        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppColors.FONT_BODY);
        tabs.setBackground(AppColors.BG);
        tabs.setFocusable(false);

        dashboardPanel  = new DashboardPanel(manager);
        addWorkoutPanel = new AddWorkoutPanel(manager, this::onSessionSaved);
        historyPanel    = new HistoryPanel(manager);
        profilePanel    = new ProfilePanel(manager);
        dataPanel       = new DataPanel(manager, this::onDataChanged);

        tabs.addTab("  Dashboard  ",  dashboardPanel);
        tabs.addTab("  Add Workout  ", addWorkoutPanel);
        tabs.addTab("  History  ",    historyPanel);
        tabs.addTab("  Profile  ",    profilePanel);
        tabs.addTab("  Exports  ",      dataPanel);

        // Refresh panels when their tab is selected
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 0) dashboardPanel.refresh();
            if (idx == 2) historyPanel.refresh();
            if (idx == 3) profilePanel.refresh();
        });

        return tabs;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0xE8EAF0));
        bar.setBorder(BorderFactory.createEmptyBorder(5, 16, 5, 16));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(AppColors.FONT_SMALL);
        statusLabel.setForeground(AppColors.TEXT_LIGHT);
        bar.add(statusLabel, BorderLayout.WEST);

        return bar;
    }

    // ---------------------------------------------------------------------- Actions

    private void onSessionSaved() {
        refreshAll();
        setStatus("Session saved successfully.");
    }

    private void onDataChanged() {
        refreshAll();
        setStatus("Data updated.");
    }

    private void refreshAll() {
        User u = manager.getUser();
        userLabel.setText("Welcome, " + u.getName() + "  |  BMI " + String.format("%.1f", u.getBMI()));
        dashboardPanel.refresh();
        historyPanel.refresh();
        profilePanel.refresh();
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    // ---------------------------------------------------------------------- Profile dialog

    private User showProfileDialog() {
        while (true) {
            ProfileSetupDialog dlg = new ProfileSetupDialog(this);
            dlg.setVisible(true);
            User u = dlg.getResult();
            if (u != null) return u;
            int choice = JOptionPane.showConfirmDialog(
                this,
                "No profile entered. Exit?",
                "Exit",
                JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) return null;
        }
    }

    // ---------------------------------------------------------------------- Look & Feel

    private static void initLookAndFeel() {
        // Use Metal (cross-platform) L&F so setBackground/setForeground on
        // JButton work on all platforms including macOS.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // keep default
        }

        // Tab style tweaks
        UIManager.put("TabbedPane.selected",          AppColors.CARD);
        UIManager.put("TabbedPane.background",        AppColors.BG);
        UIManager.put("TabbedPane.contentBorderInsets", new java.awt.Insets(0, 0, 0, 0));
    }

    // ---------------------------------------------------------------------- Entry point

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WorkoutTrackerGUI::new);
    }
}
