package gui;

import model.User;
import util.FileManager;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

/**
 * Modal dialog shown on launch.
 * - If saved users exist: shows a user-selection screen (JComboBox).
 * - If no saved users, or user clicks "New Profile": shows the create-profile form.
 */
public class ProfileSetupDialog extends JDialog {

    private static final String CARD_SELECT = "SELECT";
    private static final String CARD_CREATE = "CREATE";

    private User result = null;
    private ArrayList<User> savedUsers;

    // Selection screen
    private JComboBox<String> userCombo;

    // Create screen
    private JTextField nameField;
    private JTextField ageField;
    private JTextField weightField;
    private JTextField heightField;

    private CardLayout cardLayout;
    private JPanel cards;

    public ProfileSetupDialog(JFrame parent) {
        super(parent, "Welcome – Workout Tracker", true);
        savedUsers = FileManager.loadUsers();
        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void buildUI() {
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setBackground(AppColors.CARD);

        cards.add(buildSelectCard(), CARD_SELECT);
        cards.add(buildCreateCard(), CARD_CREATE);

        if (savedUsers.isEmpty()) {
            cardLayout.show(cards, CARD_CREATE);
        } else {
            cardLayout.show(cards, CARD_SELECT);
        }

        setContentPane(cards);
    }

    // ------------------------------------------------------------------ Select card

    private JPanel buildSelectCard() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppColors.CARD);

        JLabel header = new JLabel("Select Your Profile");
        header.setFont(AppColors.FONT_TITLE);
        header.setForeground(AppColors.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(AppColors.CARD);
        center.setBorder(BorderFactory.createEmptyBorder(12, 28, 8, 28));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 0, 6, 14);
        lc.gridx = 0;
        lc.gridy = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(6, 0, 6, 0);
        fc.gridx = 1;
        fc.gridy = 0;

        JLabel lbl = new JLabel("Who are you?");
        lbl.setFont(AppColors.FONT_BODY);
        lbl.setForeground(AppColors.TEXT_DARK);
        center.add(lbl, lc);

        String[] names = new String[savedUsers.size()];
        for (int i = 0; i < savedUsers.size(); i++) {
            names[i] = savedUsers.get(i).getName();
        }
        userCombo = new JComboBox<>(new DefaultComboBoxModel<>(names));
        userCombo.setFont(AppColors.FONT_BODY);
        center.add(userCombo, fc);

        root.add(center, BorderLayout.CENTER);

        // Buttons
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(AppColors.CARD);
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 28, 24, 28));

        JButton selectBtn = new JButton("Start");
        selectBtn.setFont(AppColors.FONT_HEADING);
        selectBtn.setBackground(AppColors.PRIMARY);
        selectBtn.setForeground(AppColors.CARD);
        selectBtn.setFocusPainted(false);
        selectBtn.setBorderPainted(false);
        selectBtn.setOpaque(true);
        selectBtn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        selectBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        selectBtn.addActionListener(e -> onSelectUser());

        JButton newBtn = new JButton("New Profile");
        newBtn.setFont(AppColors.FONT_HEADING);
        newBtn.setBackground(AppColors.CARD);
        newBtn.setForeground(AppColors.PRIMARY);
        newBtn.setFocusPainted(false);
        newBtn.setBorder(BorderFactory.createLineBorder(AppColors.PRIMARY));
        newBtn.setOpaque(true);
        newBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.PRIMARY),
            BorderFactory.createEmptyBorder(11, 20, 11, 20)
        ));
        newBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        newBtn.addActionListener(e -> {
            cardLayout.show(cards, CARD_CREATE);
            pack();
        });

        bottom.add(newBtn,   BorderLayout.WEST);
        bottom.add(selectBtn, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    private void onSelectUser() {
        int idx = userCombo.getSelectedIndex();
        if (idx >= 0) {
            result = savedUsers.get(idx);
            dispose();
        }
    }

    // ------------------------------------------------------------------ Create card

    private JPanel buildCreateCard() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppColors.CARD);

        JLabel header = new JLabel("Create Your Profile");
        header.setFont(AppColors.FONT_TITLE);
        header.setForeground(AppColors.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        root.add(header, BorderLayout.NORTH);

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

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(AppColors.CARD);
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 28, 24, 28));

        JButton createBtn = new JButton("Start Tracking");
        createBtn.setFont(AppColors.FONT_HEADING);
        createBtn.setBackground(AppColors.PRIMARY);
        createBtn.setForeground(AppColors.CARD);
        createBtn.setFocusPainted(false);
        createBtn.setBorderPainted(false);
        createBtn.setOpaque(true);
        createBtn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        createBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        createBtn.addActionListener(e -> onSubmit());

        bottom.add(createBtn, BorderLayout.CENTER);

        // Only show "Back" button if there are existing users to go back to
        if (!savedUsers.isEmpty()) {
            JButton backBtn = new JButton("Back");
            backBtn.setFont(AppColors.FONT_HEADING);
            backBtn.setBackground(AppColors.CARD);
            backBtn.setForeground(AppColors.PRIMARY);
            backBtn.setFocusPainted(false);
            backBtn.setOpaque(true);
            backBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.PRIMARY),
                BorderFactory.createEmptyBorder(11, 20, 11, 20)
            ));
            backBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            backBtn.addActionListener(e -> {
                cardLayout.show(cards, CARD_SELECT);
                pack();
            });
            bottom.add(backBtn, BorderLayout.WEST);
        }

        root.add(bottom, BorderLayout.SOUTH);

        return root;
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

            // Add to saved users if not already present
            boolean found = false;
            for (int i = 0; i < savedUsers.size(); i++) {
                if (savedUsers.get(i).getName().equalsIgnoreCase(name)) {
                    savedUsers.set(i, result);
                    found = true;
                    break;
                }
            }
            if (!found) {
                savedUsers.add(result);
            }
            FileManager.saveUsers(savedUsers);

            dispose();
        } catch (NumberFormatException ex) {
            ageField.setBorder(BorderFactory.createLineBorder(AppColors.DANGER, 2));
        }
    }

    /** Returns the User selected or created, or null if cancelled. */
    public User getResult() {
        return result;
    }
}
