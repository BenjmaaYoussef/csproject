package gui;

import model.User;
import model.WorkoutManager;
import model.WorkoutSession;
import util.FileManager;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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

    private WorkoutManager manager;

    private DashboardPanel   dashboardPanel;
    private AddWorkoutPanel  addWorkoutPanel;
    private HistoryPanel     historyPanel;
    private ProfilePanel     profilePanel;
    private DataPanel        dataPanel;

    private JLabel statusLabel;
    private JLabel userLabel;

    public WorkoutTrackerGUI() {
        super("Workout Tracker v7.0");
        initLookAndFeel();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));

        // 1. Ask user for profile
        User user = showProfileDialog();
        if (user == null) {
            System.exit(0);
        }

        // 2. Create manager and load saved sessions
        manager = new WorkoutManager(user);
        ArrayList<WorkoutSession> saved = FileManager.loadBinary();
        for (WorkoutSession s : saved) {
            manager.addSession(s);
        }

        buildUI();
        refreshAll();

        setSize(1050, 680);
        setLocationRelativeTo(null);
        setVisible(true);
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
        tabs.addTab("  Data & Sync  ", dataPanel);

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

        JLabel version = new JLabel("CS202 Phase 7 – Swing GUI");
        version.setFont(AppColors.FONT_SMALL);
        version.setForeground(AppColors.TEXT_LIGHT);
        bar.add(version, BorderLayout.EAST);

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
        ProfileSetupDialog dlg = new ProfileSetupDialog(this);
        dlg.setVisible(true);
        User u = dlg.getResult();
        if (u == null) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "No profile entered. Exit?",
                "Exit",
                JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) return null;
        }
        return u;
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
