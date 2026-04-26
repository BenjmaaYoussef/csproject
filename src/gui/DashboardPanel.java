package gui;

import model.Exercise;
import model.ExerciseType;
import model.WorkoutSession;
import model.WorkoutManager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;


public class DashboardPanel extends JPanel {

    private final WorkoutManager manager;

    private JLabel totalSessionsLabel;
    private JLabel totalExercisesLabel;
    private JLabel totalPRsLabel;
    private BarChartPanel chartPanel;

    public DashboardPanel(WorkoutManager manager) {
        this.manager = manager;
        setBackground(AppColors.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        
        JLabel title = new JLabel("Dashboard");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        cardsRow.setBackground(AppColors.BG);
        cardsRow.setPreferredSize(new Dimension(0, 110));

        totalSessionsLabel  = new JLabel("0");
        totalExercisesLabel = new JLabel("0");
        totalPRsLabel       = new JLabel("0");

        cardsRow.add(statCard("Total Sessions",  totalSessionsLabel,  AppColors.PRIMARY));
        cardsRow.add(statCard("Total Exercises", totalExercisesLabel, AppColors.SUCCESS));
        cardsRow.add(statCard("Personal Records", totalPRsLabel,       AppColors.DANGER));

        JPanel northPanel = new JPanel(new BorderLayout(0, 8));
        northPanel.setBackground(AppColors.BG);
        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(cardsRow, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        
        chartPanel = new BarChartPanel();
        chartPanel.setBackground(AppColors.CARD);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xEEEEEE)),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JScrollPane scroll = new JScrollPane(chartPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppColors.CARD);
        add(scroll, BorderLayout.CENTER);
    }

    
    private JPanel statCard(String labelText, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(AppColors.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xEEEEEE)),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppColors.CARD);

        JLabel bar = new JLabel();
        bar.setOpaque(true);
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(4, 0));
        top.add(bar, BorderLayout.WEST);

        JPanel textArea = new JPanel(new BorderLayout(0, 4));
        textArea.setBackground(AppColors.CARD);
        textArea.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        valueLabel.setFont(AppColors.FONT_STAT);
        valueLabel.setForeground(AppColors.TEXT_DARK);
        textArea.add(valueLabel, BorderLayout.CENTER);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppColors.FONT_SMALL);
        lbl.setForeground(AppColors.TEXT_LIGHT);
        textArea.add(lbl, BorderLayout.SOUTH);

        top.add(textArea, BorderLayout.CENTER);
        card.add(top, BorderLayout.CENTER);
        return card;
    }

    
    public void refresh() {
        ArrayList<WorkoutSession> sessions = manager.getAllSessions();

        int totalEx = 0;
        for (WorkoutSession s : sessions) {
            totalEx += s.getExercises().size();
        }

        totalSessionsLabel.setText(String.valueOf(sessions.size()));
        totalExercisesLabel.setText(String.valueOf(totalEx));
        totalPRsLabel.setText(String.valueOf(manager.getAllPersonalRecords().size()));

        chartPanel.setSessions(sessions);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    
    private static class BarChartPanel extends JPanel {

        private List<WorkoutSession> sessions = new ArrayList<>();

        public void setSessions(List<WorkoutSession> sessions) {
            this.sessions = sessions;
        }

        @Override
        public Dimension getPreferredSize() {
            int width = Math.max(500, sessions.size() * 60 + 80);
            return new Dimension(width, 280);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padLeft = 48, padBottom = 40, padTop = 20, padRight = 16;
            int chartW = w - padLeft - padRight;
            int chartH = h - padBottom - padTop;

            
            g2.setFont(AppColors.FONT_HEADING);
            g2.setColor(AppColors.TEXT_DARK);
            g2.drawString("Exercises per Session", padLeft, padTop - 4);

            if (sessions.isEmpty()) {
                g2.setFont(AppColors.FONT_BODY);
                g2.setColor(AppColors.TEXT_LIGHT);
                g2.drawString("No sessions logged yet.", padLeft + chartW / 2 - 70, padTop + chartH / 2);
                return;
            }

            
            int maxEx = 1;
            for (WorkoutSession s : sessions) {
                if (s.getExercises().size() > maxEx) {
                    maxEx = s.getExercises().size();
                }
            }

            
            g2.setFont(AppColors.FONT_SMALL);
            int steps = Math.min(maxEx, 5);
            for (int i = 0; i <= steps; i++) {
                int val = (int) Math.round((double) maxEx * i / steps);
                int y   = padTop + chartH - (int) ((double) chartH * i / steps);
                g2.setColor(new Color(0xEEEEEE));
                g2.drawLine(padLeft, y, padLeft + chartW, y);
                g2.setColor(AppColors.TEXT_LIGHT);
                String lbl = String.valueOf(val);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, padLeft - fm.stringWidth(lbl) - 4, y + fm.getAscent() / 2);
            }

            
            int n = sessions.size();
            int barAreaW = chartW / Math.max(n, 1);
            int barW = Math.max(8, barAreaW - 12);

            for (int i = 0; i < n; i++) {
                WorkoutSession s = sessions.get(i);
                int exCount = s.getExercises().size();

                int barH = (int) ((double) chartH * exCount / maxEx);
                int x    = padLeft + i * barAreaW + (barAreaW - barW) / 2;
                int y    = padTop + chartH - barH;

                
                Color barColor = pickColor(s.getExercises());
                g2.setColor(barColor);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                
                g2.setColor(AppColors.TEXT_DARK);
                g2.setFont(AppColors.FONT_SMALL);
                FontMetrics fm = g2.getFontMetrics();
                String val = String.valueOf(exCount);
                g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 4);

                
                g2.setColor(AppColors.TEXT_LIGHT);
                String date = s.getDate().length() >= 10 ? s.getDate().substring(5) : s.getDate();
                int dateW = fm.stringWidth(date);
                g2.drawString(date, x + (barW - dateW) / 2, padTop + chartH + 18);
            }

            
            g2.setColor(new Color(0xCCCCCC));
            g2.drawLine(padLeft, padTop + chartH, padLeft + chartW, padTop + chartH);
        }

        private Color pickColor(ArrayList<Exercise> exercises) {
            int str = 0, card = 0, flex = 0;
            for (Exercise e : exercises) {
                if (e.getType() == ExerciseType.STRENGTH)    str++;
                else if (e.getType() == ExerciseType.CARDIO) card++;
                else                                          flex++;
            }
            if (str >= card && str >= flex) return AppColors.STRENGTH;
            if (card >= flex)               return AppColors.CARDIO;
            return AppColors.FLEXIBILITY;
        }
    }
}
