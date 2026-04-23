package gui;

import model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Modal dialog shown on first launch to collect user profile data.
 */
public class ProfileSetupDialog extends JDialog {

    private User result = null;

    private JTextField nameField;
    private JTextField ageField;
    private JTextField weightField;
    private JTextField heightField;

    public ProfileSetupDialog(JFrame parent) {
        super(parent, "Welcome – Set Up Your Profile", true);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppColors.CARD);

        // Header
        JLabel header = new JLabel("Create Your Profile");
        header.setFont(AppColors.FONT_TITLE);
        header.setForeground(AppColors.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppColors.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(8, 28, 8, 28));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 0, 6, 14);
        lc.gridx = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(6, 0, 6, 0);
        fc.gridx = 1;

        String[] labels = {"Name:", "Age:", "Weight (kg):", "Height (cm):"};
        nameField   = styledField(20);
        ageField    = styledField(6);
        weightField = styledField(6);
        heightField = styledField(6);
        JTextField[] fields = {nameField, ageField, weightField, heightField};

        for (int i = 0; i < labels.length; i++) {
            lc.gridy = i;
            fc.gridy = i;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(AppColors.FONT_BODY);
            lbl.setForeground(AppColors.TEXT_DARK);
            form.add(lbl, lc);
            form.add(fields[i], fc);
        }

        root.add(form, BorderLayout.CENTER);

        // Button
        JButton btn = new JButton("Start Tracking");
        btn.setFont(AppColors.FONT_HEADING);
        btn.setBackground(AppColors.PRIMARY);
        btn.setForeground(AppColors.CARD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btn.addActionListener(e -> onSubmit());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(AppColors.CARD);
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 28, 24, 28));
        bottom.add(btn, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(AppColors.FONT_BODY);
        f.setForeground(AppColors.TEXT_DARK);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(0xDDDDDD)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return f;
    }

    private void onSubmit() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            nameField.setBorder(BorderFactory.createLineBorder(AppColors.DANGER, 2));
            return;
        }
        try {
            int age    = Integer.parseInt(ageField.getText().trim());
            double wt  = Double.parseDouble(weightField.getText().trim());
            double ht  = Double.parseDouble(heightField.getText().trim());
            result = new User(name, age, wt, ht);
            dispose();
        } catch (NumberFormatException ex) {
            // highlight fields
            ageField.setBorder(BorderFactory.createLineBorder(AppColors.DANGER, 2));
        }
    }

    /** Returns the User created, or null if the dialog was cancelled. */
    public User getResult() {
        return result;
    }
}
