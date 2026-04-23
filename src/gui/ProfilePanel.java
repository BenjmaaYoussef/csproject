package gui;

import model.User;
import model.WorkoutManager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Profile tab – user details and a BMI gauge.
 */
public class ProfilePanel extends JPanel {

    private final WorkoutManager manager;

    private JLabel nameVal;
    private JLabel ageVal;
    private JLabel weightVal;
    private JLabel heightVal;
    private JLabel bmiVal;
    private JLabel bmiCatLabel;
    private BmiGauge bmiGauge;

    public ProfilePanel(WorkoutManager manager) {
        this.manager = manager;
        setBackground(AppColors.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("User Profile");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(20, 0));
        center.setBackground(AppColors.BG);

        center.add(buildInfoCard(), BorderLayout.WEST);
        center.add(buildBmiCard(),  BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildInfoCard() {
        JPanel card = card();
        card.setPreferredSize(new Dimension(280, 0));
        card.setLayout(new GridBagLayout());

        JLabel heading = new JLabel("Details");
        heading.setFont(AppColors.FONT_HEADING);
        heading.setForeground(AppColors.PRIMARY);

        GridBagConstraints hc = new GridBagConstraints();
        hc.gridx = 0; hc.gridy = 0; hc.gridwidth = 2;
        hc.anchor = GridBagConstraints.WEST;
        hc.insets = new Insets(0, 0, 16, 0);
        card.add(heading, hc);

        String[] labels = {"Name", "Age", "Weight", "Height", "BMI"};
        nameVal   = valLabel("-");
        ageVal    = valLabel("-");
        weightVal = valLabel("-");
        heightVal = valLabel("-");
        bmiVal    = valLabel("-");
        bmiVal.setFont(AppColors.FONT_HEADING);
        JLabel[] vals = {nameVal, ageVal, weightVal, heightVal, bmiVal};

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 0, 8, 16);
        lc.gridx  = 0;

        GridBagConstraints vc = new GridBagConstraints();
        vc.anchor  = GridBagConstraints.WEST;
        vc.weightx = 1.0;
        vc.insets  = new Insets(8, 0, 8, 0);
        vc.gridx   = 1;

        for (int i = 0; i < labels.length; i++) {
            lc.gridy = i + 1;
            vc.gridy = i + 1;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(AppColors.FONT_BODY);
            lbl.setForeground(AppColors.TEXT_LIGHT);
            card.add(lbl, lc);
            card.add(vals[i], vc);
        }

        // BMI category label
        bmiCatLabel = new JLabel("");
        bmiCatLabel.setFont(AppColors.FONT_SMALL);
        GridBagConstraints catC = new GridBagConstraints();
        catC.gridx = 1; catC.gridy = 6;
        catC.anchor = GridBagConstraints.WEST;
        catC.insets = new Insets(0, 0, 0, 0);
        card.add(bmiCatLabel, catC);

        return card;
    }

    private JPanel buildBmiCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));

        JLabel heading = new JLabel("BMI Gauge");
        heading.setFont(AppColors.FONT_HEADING);
        heading.setForeground(AppColors.PRIMARY);
        card.add(heading, BorderLayout.NORTH);

        bmiGauge = new BmiGauge();
        card.add(bmiGauge, BorderLayout.CENTER);

        return card;
    }

    public void refresh() {
        User u = manager.getUser();
        if (u == null) return;

        nameVal.setText(u.getName());
        ageVal.setText(u.getAge() + " years");
        weightVal.setText(u.getWeightKg() + " kg");
        heightVal.setText(u.getHeightCm() + " cm");

        double bmi = u.getBMI();
        bmiVal.setText(String.format("%.1f", bmi));

        String cat;
        Color catColor;
        if (bmi < 18.5)      { cat = "Underweight"; catColor = AppColors.WARNING; }
        else if (bmi < 25.0) { cat = "Normal";       catColor = AppColors.SUCCESS; }
        else if (bmi < 30.0) { cat = "Overweight";   catColor = AppColors.WARNING; }
        else                 { cat = "Obese";         catColor = AppColors.DANGER;  }

        bmiCatLabel.setText(cat);
        bmiCatLabel.setForeground(catColor);
        bmiGauge.setBmi(bmi);
        bmiGauge.repaint();
    }

    // ---- Helpers

    private JLabel valLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppColors.FONT_BODY.deriveFont(Font.BOLD));
        l.setForeground(AppColors.TEXT_DARK);
        return l;
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(AppColors.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xEEEEEE)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return p;
    }

    // ---- Inner BMI gauge

    private static class BmiGauge extends JPanel {

        private double bmi = 0;

        public void setBmi(double bmi) { this.bmi = bmi; }

        @Override
        public Dimension getPreferredSize() { return new Dimension(300, 200); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h - 30;
            int radius = Math.min(cx - 20, cy - 20);

            // Draw arc segments: underweight / normal / overweight / obese
            // BMI range mapped to 180 degrees (left = 10, right = 40+)
            int[] angles = {35, 55, 50, 40}; // degrees for each segment
            Color[] segColors = {
                new Color(0x91C8E4),
                AppColors.SUCCESS,
                AppColors.WARNING,
                AppColors.DANGER
            };
            int startAngle = 180;
            for (int i = 0; i < angles.length; i++) {
                g2.setColor(segColors[i]);
                g2.fillArc(cx - radius, cy - radius, radius * 2, radius * 2,
                            startAngle, -angles[i]);
                startAngle -= angles[i];
            }

            // White inner arc (donut)
            int inner = (int) (radius * 0.62);
            g2.setColor(AppColors.CARD);
            g2.fillArc(cx - inner, cy - inner, inner * 2, inner * 2, 0, -180);

            // Needle
            if (bmi > 0) {
                double clampedBmi = Math.max(10, Math.min(40, bmi));
                // Map 10–40 → 180–0 degrees
                double angle = 180 - ((clampedBmi - 10) / 30.0) * 180;
                double rad   = Math.toRadians(angle);
                int nx = (int) (cx + (radius * 0.75) * Math.cos(rad));
                int ny = (int) (cy - (radius * 0.75) * Math.sin(rad));

                g2.setColor(AppColors.TEXT_DARK);
                g2.setStroke(new java.awt.BasicStroke(3, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, cy, nx, ny);
                g2.fillOval(cx - 6, cy - 6, 12, 12);
            }

            // Legend labels
            g2.setFont(AppColors.FONT_SMALL);
            g2.setColor(AppColors.TEXT_LIGHT);
            g2.drawString("10", cx - radius - 2, cy + 14);
            g2.drawString("40", cx + radius - 16, cy + 14);
            g2.drawString("25", cx - 8, cy - radius - 4);
        }
    }
}
