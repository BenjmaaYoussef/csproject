package gui;

import db.WorkoutSessionDAO;
import model.WorkoutManager;
import model.WorkoutSession;
import util.FileManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exports tab – manual export and database management operations.
 *
 * Auto-save/auto-load (Phases B & C) handle all routine persistence.
 * This tab provides:
 *   - Export TXT  : human-readable pipe-delimited text file (Phase 3)
 *   - Export XML  : machine-readable XML archive (Phase 4)
 *   - Export Report : formatted plain-text summary report (Phase 3)
 *   - Delete from DB : remove a specific session by date (Phase 5)
 */
public class DataPanel extends JPanel {

    private final WorkoutManager manager;
    private final Runnable onDataChanged;

    private JTextArea logArea;

    public DataPanel(WorkoutManager manager, Runnable onDataChanged) {
        this.manager       = manager;
        this.onDataChanged = onDataChanged;
        setBackground(AppColors.BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private void buildUI() {
        add(buildTop(),    BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    /** Page header with title, mode badge, and subtitle. */
    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppColors.BG);
        top.setBorder(BorderFactory.createEmptyBorder(24, 24, 0, 24));

        // Title + mode badge on same row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setBackground(AppColors.BG);

        JLabel title = new JLabel("Exports & Data");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_DARK);
        titleRow.add(title);
        titleRow.add(javax.swing.Box.createHorizontalStrut(12));
        titleRow.add(buildModeBadge());

        top.add(titleRow, BorderLayout.NORTH);

        boolean isDB = WorkoutTrackerGUI.connectionMode == util.ConnectionMode.DIRECT_DB;
        String modeNote = isDB
            ? "Sessions are saved automatically to the database."
            : "Sessions are saved automatically via the server.";
        JLabel sub = new JLabel(modeNote + "  Use these controls to export local files"
            + (isDB ? " or delete a database record." : "."));
        sub.setFont(AppColors.FONT_BODY);
        sub.setForeground(AppColors.TEXT_LIGHT);
        sub.setBorder(BorderFactory.createEmptyBorder(6, 0, 16, 0));
        top.add(sub, BorderLayout.CENTER);

        return top;
    }

    /** Small pill badge showing the active connection mode. */
    private JLabel buildModeBadge() {
        boolean isDB = WorkoutTrackerGUI.connectionMode == util.ConnectionMode.DIRECT_DB;
        String text  = isDB ? "Direct DB" : "Via Server";
        Color  bg    = isDB ? new Color(0xE8EDFF) : new Color(0xE6FAF4);
        Color  fg    = isDB ? AppColors.PRIMARY   : AppColors.SUCCESS;

        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg.brighter(), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }

    /** Card grid (top) + activity log (bottom). */
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(AppColors.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16, 24, 24, 24));

        center.add(buildCards(), BorderLayout.NORTH);
        center.add(buildLog(),   BorderLayout.CENTER);

        return center;
    }

    /** Three export cards + one mode-specific card in a horizontal row. */
    private JPanel buildCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setBackground(AppColors.BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        row.add(buildCard(
            "Text Export",
            AppColors.PRIMARY,
            "Export TXT",
            "Saves a pipe-delimited .txt file you\ncan re-import or open in a text editor.",
            this::exportTXT
        ));

        row.add(buildCard(
            "XML Archive",
            AppColors.SUCCESS,
            "Export XML",
            "Produces a structured XML file useful\nfor backups or external tools.",
            this::exportXML
        ));

        row.add(buildCard(
            "Summary Report",
            new Color(0xF4845F),
            "Export Report",
            "Generates a human-readable report_<name>.txt\nsummarising all your sessions and exercises.",
            this::exportReport
        ));

        // 4th card depends on connection mode
        if (WorkoutTrackerGUI.connectionMode == util.ConnectionMode.DIRECT_DB) {
            row.add(buildCard(
                "Database",
                AppColors.DANGER,
                "Delete Session",
                "Permanently removes a session from the\ndatabase by date. Cannot be undone.",
                this::deleteFromDB
            ));
        } else {
            row.add(buildUnavailableCard());
        }

        return row;
    }

    /**
     * Greyed-out card shown in Via Server mode where DB operations are not available.
     */
    private JPanel buildUnavailableCard() {
        Color grey = new Color(0xCCCCCC);

        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(new Color(0xF7F7F7));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, grey),
            BorderFactory.createLineBorder(new Color(0xE8E8E8))
        ));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(new Color(0xF7F7F7));
        inner.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        JLabel title = new JLabel("Database");
        title.setFont(AppColors.FONT_HEADING);
        title.setForeground(grey);
        title.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(title);
        inner.add(Box.createVerticalStrut(8));

        String html = "<html><body style='width:120px;color:#AAAAAA;font-family:Segoe UI;font-size:11px'>"
            + "Not available in Via Server mode.<br><br>"
            + "Direct database operations require<br>the Direct (Database) connection mode."
            + "</body></html>";
        JLabel desc = new JLabel(html);
        desc.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(desc);
        inner.add(Box.createVerticalGlue());
        inner.add(Box.createVerticalStrut(12));

        // Not using setEnabled(false) — Metal L&F overrides setBackground() on disabled buttons.
        // The grey colors below already communicate the unavailable state visually.
        JButton btn = new JButton("Not Available");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(0xDDDDDD));
        btn.setForeground(new Color(0x999999));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        // No action listener — clicking does nothing
        inner.add(btn);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /**
     * Builds a single card with a coloured top border, heading, description,
     * and a single action button.
     */
    private JPanel buildCard(String heading, Color accent,
                             String btnLabel, String description,
                             Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(AppColors.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
            BorderFactory.createLineBorder(new Color(0xE8E8E8))
        ));

        // ---- inner padding panel
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(AppColors.CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        // heading
        JLabel title = new JLabel(heading);
        title.setFont(AppColors.FONT_HEADING);
        title.setForeground(accent);
        title.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(title);
        inner.add(Box.createVerticalStrut(8));

        // description (multi-line via html)
        String htmlDesc = "<html><body style='width:120px;color:#8D99AE;font-family:Segoe UI;font-size:11px'>"
            + description.replace("\n", "<br>") + "</body></html>";
        JLabel desc = new JLabel(htmlDesc);
        desc.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(desc);
        inner.add(Box.createVerticalGlue());
        inner.add(Box.createVerticalStrut(12));

        // action button
        JButton btn = new JButton(btnLabel);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(accent);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> action.run());

        // hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color base = accent;
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(base.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(base);
            }
        });

        inner.add(btn);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /** Activity log area at the bottom of the tab. */
    private JPanel buildLog() {
        JPanel logCard = new JPanel(new BorderLayout());
        logCard.setBackground(AppColors.CARD);
        logCard.setBorder(BorderFactory.createLineBorder(new Color(0xE8E8E8)));

        // header row with label + clear button
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setBackground(new Color(0xF7F8FA));
        logHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE8E8E8)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JLabel logTitle = new JLabel("Activity Log");
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logTitle.setForeground(AppColors.TEXT_MID);
        logHeader.add(logTitle, BorderLayout.WEST);

        JLabel hint = new JLabel("Results of export operations appear here");
        hint.setFont(AppColors.FONT_SMALL);
        hint.setForeground(AppColors.TEXT_LIGHT);
        logHeader.add(hint, BorderLayout.CENTER);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(AppColors.FONT_SMALL);
        clearBtn.setForeground(AppColors.TEXT_LIGHT);
        clearBtn.setBackground(new Color(0xF7F8FA));
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> logArea.setText(""));
        logHeader.add(clearBtn, BorderLayout.EAST);

        logCard.add(logHeader, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setBackground(new Color(0xFDFDFD));
        logArea.setForeground(AppColors.TEXT_DARK);
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        logCard.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return logCard;
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    private void exportTXT() {
        String name = manager.getUser().getName();
        int count = manager.getAllSessions().size();
        FileManager.saveSessions(name, manager.getAllSessions());
        log("OK", "workouts_" + name + ".txt — " + count + " session(s) written.");
    }

    private void exportXML() {
        String name = manager.getUser().getName();
        int count = manager.getAllSessions().size();
        FileManager.exportXML(name, manager.getAllSessions());
        log("OK", "workouts_" + name.replaceAll("\\s+", "_") + ".xml — " + count + " session(s) written.");
    }

    private void exportReport() {
        FileManager.exportReport(manager);
        String name = manager.getUser().getName();
        log("OK", "report_" + name + ".txt — summary report written.");
    }

    private void deleteFromDB() {
        String date = JOptionPane.showInputDialog(this,
            "Enter the session date to delete (yyyy-MM-dd):",
            "Delete Session from Database",
            JOptionPane.WARNING_MESSAGE);
        if (date == null || date.trim().isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Permanently delete the session on " + date.trim() + " from the database?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String userName = manager.getUser().getName();
        WorkoutSessionDAO dao = new WorkoutSessionDAO();
        int result = dao.deleteSession(date.trim(), userName);
        if (result == 1) {
            log("DELETED", "Session on " + date.trim() + " removed from database.");
            if (onDataChanged != null) onDataChanged.run();
        } else {
            log("NOT FOUND", "No session for " + date.trim() + " found in database.");
        }
    }

    // -------------------------------------------------------------------------
    // Logging helpers
    // -------------------------------------------------------------------------

    private void log(String tag, String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "]  " + tag + "  " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
