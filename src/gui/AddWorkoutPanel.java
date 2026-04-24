package gui;

import db.WorkoutSessionDAO;
import exception.DuplicateExerciseException;
import exception.InvalidExerciseException;
import model.Exercise;
import model.ExerciseType;
import model.WorkoutManager;
import model.WorkoutSession;
import network.WorkoutClient;
import util.ConnectionMode;
import util.FileManager;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Add Workout tab – form to build and save a new WorkoutSession.
 */
public class AddWorkoutPanel extends JPanel {

    private final WorkoutManager manager;
    private final Runnable onSessionSaved;

    private JTextField dateField;
    private JTextArea  notesArea;
    private JTextField exNameField;
    private JComboBox<String> typeCombo;
    private JTextField setsField;
    private JTextField repsField;
    private JTextField weightField;
    private JTextField durationField;

    private DefaultTableModel exerciseTableModel;
    private JTable exerciseTable;

    // Stores exercises staged before the session is saved
    private final java.util.ArrayList<Exercise> staged = new java.util.ArrayList<>();

    public AddWorkoutPanel(WorkoutManager manager, Runnable onSessionSaved) {
        this.manager       = manager;
        this.onSessionSaved = onSessionSaved;
        setBackground(AppColors.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Log New Workout");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        // Split: left = session info, right = exercise entry
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setBackground(AppColors.BG);

        content.add(buildSessionCard(), BorderLayout.WEST);
        content.add(buildExerciseCard(), BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    // ---- Left card: date + notes + save button
    private JPanel buildSessionCard() {
        JPanel card = card();
        card.setPreferredSize(new Dimension(260, 0));
        card.setLayout(new BorderLayout(0, 12));

        JLabel heading = new JLabel("Session Info");
        heading.setFont(AppColors.FONT_HEADING);
        heading.setForeground(AppColors.PRIMARY);
        card.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppColors.CARD);

        GridBagConstraints lc = labelConstraints();
        GridBagConstraints fc = fieldConstraints();

        lc.gridy = 0; fc.gridy = 0;
        form.add(label("Date (yyyy-MM-dd):"), lc);
        dateField = field(10);
        form.add(dateField, fc);

        lc.gridy = 1; fc.gridy = 1;
        form.add(label("Notes:"), lc);

        notesArea = new JTextArea(4, 10);
        notesArea.setFont(AppColors.FONT_BODY);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        fc.fill = GridBagConstraints.BOTH;
        fc.weighty = 1.0;
        form.add(new JScrollPane(notesArea), fc);

        card.add(form, BorderLayout.CENTER);

        JButton saveBtn = primaryButton("Save Session");
        saveBtn.addActionListener(e -> saveSession());
        card.add(saveBtn, BorderLayout.SOUTH);

        return card;
    }

    // ---- Right card: exercise entry + staged table
    private JPanel buildExerciseCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));

        JLabel heading = new JLabel("Exercises");
        heading.setFont(AppColors.FONT_HEADING);
        heading.setForeground(AppColors.PRIMARY);
        card.add(heading, BorderLayout.NORTH);

        // Form row
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppColors.CARD);

        GridBagConstraints lc = labelConstraints();
        GridBagConstraints fc = fieldConstraints();

        lc.gridy = 0; fc.gridy = 0;
        form.add(label("Name:"), lc);
        exNameField = field(10);
        form.add(exNameField, fc);

        lc.gridy = 1; fc.gridy = 1;
        form.add(label("Type:"), lc);
        typeCombo = new JComboBox<>(new DefaultComboBoxModel<>(new String[]{"STRENGTH", "CARDIO", "FLEXIBILITY"}));
        typeCombo.setFont(AppColors.FONT_BODY);
        typeCombo.addActionListener(e -> updateFieldVisibility());
        form.add(typeCombo, fc);

        lc.gridy = 2; fc.gridy = 2;
        form.add(label("Sets:"), lc);
        setsField = field(6);
        form.add(setsField, fc);

        lc.gridy = 3; fc.gridy = 3;
        form.add(label("Reps:"), lc);
        repsField = field(6);
        form.add(repsField, fc);

        lc.gridy = 4; fc.gridy = 4;
        form.add(label("Weight (kg):"), lc);
        weightField = field(6);
        form.add(weightField, fc);

        lc.gridy = 5; fc.gridy = 5;
        form.add(label("Duration (min):"), lc);
        durationField = field(6);
        form.add(durationField, fc);

        // Add exercise button
        JButton addExBtn = new JButton("+ Add Exercise");
        addExBtn.setFont(AppColors.FONT_BODY);
        addExBtn.setBackground(AppColors.SUCCESS);
        addExBtn.setForeground(Color.WHITE);
        addExBtn.setFocusPainted(false);
        addExBtn.setBorderPainted(false);
        addExBtn.setOpaque(true);
        addExBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        addExBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addExBtn.addActionListener(e -> addExercise());

        GridBagConstraints btnC = new GridBagConstraints();
        btnC.gridx = 0; btnC.gridy = 6;
        btnC.gridwidth = 2;
        btnC.fill = GridBagConstraints.HORIZONTAL;
        btnC.insets = new Insets(12, 0, 0, 0);
        form.add(addExBtn, btnC);

        card.add(form, BorderLayout.NORTH);

        // Staged exercises table
        String[] cols = {"Name", "Type", "Detail"};
        exerciseTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        exerciseTable = new JTable(exerciseTableModel);
        exerciseTable.setFont(AppColors.FONT_BODY);
        exerciseTable.setRowHeight(26);
        exerciseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        exerciseTable.getTableHeader().setFont(AppColors.FONT_BODY.deriveFont(Font.BOLD));
        exerciseTable.getTableHeader().setBackground(new Color(0xF5F5F5));
        exerciseTable.setGridColor(new Color(0xEEEEEE));

        JScrollPane scroll = new JScrollPane(exerciseTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xEEEEEE)));
        card.add(scroll, BorderLayout.CENTER);

        // Remove button
        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.setFont(AppColors.FONT_SMALL);
        removeBtn.setForeground(AppColors.DANGER);
        removeBtn.setBackground(AppColors.CARD);
        removeBtn.setBorder(BorderFactory.createLineBorder(AppColors.DANGER));
        removeBtn.setFocusPainted(false);
        removeBtn.setOpaque(true);
        removeBtn.addActionListener(e -> removeSelectedExercise());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setBackground(AppColors.CARD);
        south.add(removeBtn);
        card.add(south, BorderLayout.SOUTH);

        updateFieldVisibility();
        return card;
    }

    private void updateFieldVisibility() {
        boolean isCardio = "CARDIO".equals(typeCombo.getSelectedItem());
        setsField.setEnabled(!isCardio);
        repsField.setEnabled(!isCardio);
        weightField.setEnabled(!isCardio);
        durationField.setEnabled(isCardio);
    }

    private void addExercise() {
        String name = exNameField.getText().trim();
        String typeStr = (String) typeCombo.getSelectedItem();
        ExerciseType type = ExerciseType.valueOf(typeStr);

        try {
            Exercise ex;
            if (type == ExerciseType.CARDIO) {
                int dur = parseIntField(durationField, "Duration");
                ex = new Exercise(name, 0, 0, 0, dur, type);
            } else {
                int sets   = parseIntField(setsField, "Sets");
                int reps   = parseIntField(repsField, "Reps");
                double wt  = parseDoubleField(weightField, "Weight");
                ex = new Exercise(name, sets, reps, wt, 0, type);
            }
            staged.add(ex);
            String detail = (type == ExerciseType.CARDIO)
                ? ex.getDurationMin() + " min"
                : ex.getSets() + " x " + ex.getReps() + " @ " + ex.getWeightKg() + " kg";
            exerciseTableModel.addRow(new Object[]{ex.getName(), ex.getType().name(), detail});
            clearExerciseForm();
        } catch (InvalidExerciseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Exercise", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeSelectedExercise() {
        int row = exerciseTable.getSelectedRow();
        if (row >= 0) {
            staged.remove(row);
            exerciseTableModel.removeRow(row);
        }
    }

    private void saveSession() {
        String date  = dateField.getText().trim();
        String notes = notesArea.getText().trim();

        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date is required.", "Missing Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        WorkoutSession session = new WorkoutSession(date, notes);
        for (Exercise ex : staged) {
            try {
                session.addExercise(ex);
            } catch (DuplicateExerciseException e) {
                JOptionPane.showMessageDialog(this,
                    "Duplicate exercise: " + e.getMessage(),
                    "Duplicate", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        manager.addSession(session);
        autoSave(session);

        // Reset form
        dateField.setText("");
        notesArea.setText("");
        staged.clear();
        exerciseTableModel.setRowCount(0);

        JOptionPane.showMessageDialog(this,
            "Session for " + date + " saved with " + session.getExercises().size() + " exercise(s).",
            "Saved", JOptionPane.INFORMATION_MESSAGE);

        onSessionSaved.run();
    }

    private void autoSave(WorkoutSession session) {
        String userName = manager.getUser().getName();
        if (!WorkoutTrackerGUI.offlineMode) {
            if (WorkoutTrackerGUI.connectionMode == ConnectionMode.DIRECT_DB) {
                WorkoutSessionDAO dao = new WorkoutSessionDAO();
                dao.saveSession(session, userName);
            } else { // VIA_SERVER
                StringBuilder sb = new StringBuilder();
                sb.append(userName).append("\n");
                sb.append("SESSION|").append(session.getDate()).append("|").append(session.getNotes());
                for (Exercise ex : session.getExercises()) {
                    sb.append("\nEXERCISE|").append(ex.getName()).append("|").append(ex.getType().name())
                      .append("|").append(ex.getSets()).append("|").append(ex.getReps())
                      .append("|").append(ex.getWeightKg()).append("|").append(ex.getDurationMin());
                }
                WorkoutClient.sendAndReceive("ADD_SESSION:" + sb.toString());
            }
            FileManager.saveSyncTimestamp(userName);
        }
        FileManager.saveUserSessions(userName, manager.getAllSessions());
    }

    private void clearExerciseForm() {
        exNameField.setText("");
        setsField.setText("");
        repsField.setText("");
        weightField.setText("");
        durationField.setText("");
    }

    // ---- Helpers

    private int parseIntField(JTextField f, String fieldName) {
        try {
            String t = f.getText().trim();
            if (t.isEmpty()) return 0;
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    private double parseDoubleField(JTextField f, String fieldName) {
        try {
            String t = f.getText().trim();
            if (t.isEmpty()) return 0;
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a number.");
        }
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(AppColors.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xEEEEEE)),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return p;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppColors.FONT_BODY);
        l.setForeground(AppColors.TEXT_DARK);
        return l;
    }

    private JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(AppColors.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return f;
    }

    private JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppColors.FONT_HEADING);
        btn.setBackground(AppColors.PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    private GridBagConstraints labelConstraints() {
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor  = GridBagConstraints.WEST;
        lc.insets  = new Insets(5, 0, 5, 10);
        lc.gridx   = 0;
        return lc;
    }

    private GridBagConstraints fieldConstraints() {
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(5, 0, 5, 0);
        fc.gridx   = 1;
        return fc;
    }
}
