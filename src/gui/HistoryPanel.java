package gui;

import model.Exercise;
import model.WorkoutManager;
import model.WorkoutSession;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * History tab – JTable of sessions; clicking a row shows details below.
 */
public class HistoryPanel extends JPanel {

    private final WorkoutManager manager;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea detailArea;

    public HistoryPanel(WorkoutManager manager) {
        this.manager = manager;
        setBackground(AppColors.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Workout History");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"Date", "Exercises", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(AppColors.FONT_BODY);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(0xEEEEEE));
        table.getTableHeader().setFont(AppColors.FONT_BODY.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(new Color(0xF5F5F5));
        table.getTableHeader().setForeground(AppColors.TEXT_DARK);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(400);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail(table.getSelectedRow());
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(0xEEEEEE)));

        // Detail area
        detailArea = new JTextArea();
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        detailArea.setEditable(false);
        detailArea.setBackground(new Color(0xFAFAFA));
        detailArea.setForeground(AppColors.TEXT_DARK);
        detailArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        detailArea.setText("Select a session to see details.");

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createLineBorder(new Color(0xEEEEEE)));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailScroll);
        split.setResizeWeight(0.6);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerSize(6);

        add(split, BorderLayout.CENTER);
    }

    /** Rebuilds the table from the manager's session list. */
    public void refresh() {
        tableModel.setRowCount(0);
        List<WorkoutSession> sorted = manager.getSessionsSortedByDate();
        for (WorkoutSession s : sorted) {
            tableModel.addRow(new Object[]{
                s.getDate(),
                s.getExercises().size(),
                s.getNotes() == null ? "" : s.getNotes()
            });
        }
        detailArea.setText("Select a session to see details.");
    }

    private void showDetail(int row) {
        if (row < 0) return;
        List<WorkoutSession> sorted = manager.getSessionsSortedByDate();
        if (row >= sorted.size()) return;

        WorkoutSession s = sorted.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Date:  ").append(s.getDate()).append("\n");
        if (s.getNotes() != null && !s.getNotes().isEmpty()) {
            sb.append("Notes: ").append(s.getNotes()).append("\n");
        }
        sb.append("\nExercises:\n");
        ArrayList<Exercise> exercises = s.getExercises();
        if (exercises.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (int i = 0; i < exercises.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(exercises.get(i)).append("\n");
            }
        }
        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
    }
}
