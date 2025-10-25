package view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class StartPanel extends JPanel {
    private final WorkoutApp app;
    private JTextField goalField;
    private JTextField timeField;
    private JTextField favField;
    private JTextField specialField;

    private JRadioButton beginnerBtn;
    private JRadioButton intermediateBtn;
    private JRadioButton advancedBtn;

    private static final Color BG = new Color(36,36,36);
    private static final Color FG = Color.WHITE;
    private static final Color BTN_MAIN = new Color(45, 115, 255);
    private static final Color BTN_ALT = new Color(220, 53, 69); // Strong red for contrast
    private static final Color BTN_TEXT = Color.BLACK;

    public StartPanel(WorkoutApp app) {
        this.app = app;
        setLayout(new BorderLayout(20, 20));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JLabel title = new JLabel("Create a Personal Workout Plan", JLabel.CENTER);
        styleHeading(title);
        add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 1, 10, 14)); // panel with 1 column for dynamic rows
        fields.setBackground(BG);

        // Goal input
        JLabel goalLabel = new JLabel("Your main goal:");
        styleLabel(goalLabel);
        fields.add(goalLabel);
        goalField = new JTextField();
        styleField(goalField);
        fields.add(goalField);

        // Fitness level radio buttons
        JLabel levelLabel = new JLabel("Your fitness level:");
        styleLabel(levelLabel);
        fields.add(levelLabel);
        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // panel for radio buttons
        levelPanel.setBackground(BG);
        beginnerBtn = new JRadioButton("Beginner");
        intermediateBtn = new JRadioButton("Intermediate");
        advancedBtn = new JRadioButton("Advanced");
        ButtonGroup levelGroup = new ButtonGroup(); // group for radio buttons
        // tooltips for the radio buttons
        beginnerBtn.setToolTipText("Suitable for those new to exercise or returning after a long break."); 
        intermediateBtn.setToolTipText("Ideal for those with some experience looking to improve.");
        advancedBtn.setToolTipText("Designed for seasoned athletes aiming for peak performance.");
        levelGroup.add(beginnerBtn);
        levelGroup.add(intermediateBtn);
        levelGroup.add(advancedBtn);
        beginnerBtn.setSelected(true); // default selection

        // Style radio buttons
        for (JRadioButton btn : new JRadioButton[]{beginnerBtn, intermediateBtn, advancedBtn}) {
            btn.setBackground(BG);
            btn.setForeground(FG);
            btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 15f));
        }

        levelPanel.add(beginnerBtn);
        levelPanel.add(Box.createHorizontalStrut(15));
        levelPanel.add(intermediateBtn);
        levelPanel.add(Box.createHorizontalStrut(15));
        levelPanel.add(advancedBtn);
        fields.add(levelPanel);

        // Weekly time commitment input
        JLabel timeLabel = new JLabel("Weekly time commitment (e.g., '3 hours', '120 minutes'):");
        styleLabel(timeLabel);
        fields.add(timeLabel);
        timeField = new JTextField();
        styleField(timeField);
        fields.add(timeField);

        // Favorite exercises input
        JLabel favLabel = new JLabel("Favorite Exercises (comma-separated):");
        styleLabel(favLabel);
        fields.add(favLabel);
        favField = new JTextField();
        styleField(favField);
        fields.add(favField);

        // Special conditions input
        JLabel specialLabel = new JLabel("Special Conditions (e.g., diabetes, scoliosis, bad knees):");
        styleLabel(specialLabel);
        fields.add(specialLabel);
        specialField = new JTextField();
        styleField(specialField);
        fields.add(specialField);

        add(fields, BorderLayout.CENTER);

        // Button panel at the bottom
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(BG);

        // Generate plan button
        JButton genBtn = new JButton("Generate CUSTOM! Plan");
        styleButton(genBtn, BTN_MAIN);
        genBtn.addActionListener(this::onGenerate);
        btnPanel.add(genBtn);

        // Tracker button
        JButton trackerBtn = new JButton("View Workout Tracker");
        styleButton(trackerBtn, BTN_ALT);
        trackerBtn.addActionListener(e -> app.showWorkoutTracker());
        btnPanel.add(trackerBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    // Returns the selected fitness level as a string
    private String getSelectedLevel() {
        if (beginnerBtn.isSelected()) return "Beginner";
        if (intermediateBtn.isSelected()) return "Intermediate";
        return "Advanced";
    }

    // Handles the Generate Plan button click
    private void onGenerate(ActionEvent e) {
        String goals = goalField.getText().trim();
        String level = getSelectedLevel();
        String time = timeField.getText().trim();
        String fav = favField.getText().trim();
        String special = specialField.getText().trim();
        // Validate required fields
        if (goals.isEmpty() || time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in your goals and time commitment.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Show loading dialog while generating plan
        final JDialog loading = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Generating Plan", true);
        loading.setLayout(new BorderLayout());
        JLabel loadingLabel = new JLabel("Generating your workout plan... Please wait.", JLabel.CENTER);
        loadingLabel.setForeground(FG);
        loading.setBackground(BG);
        loading.getContentPane().setBackground(BG);
        loading.add(loadingLabel, BorderLayout.CENTER);
        loading.setSize(300, 100);
        loading.setLocationRelativeTo(this);
        
        // Run plan generation on a new thread to avoid freezing the UI
        new Thread(() -> {  
            try {
                SwingUtilities.invokeLater(() -> loading.setVisible(true));
                app.generateAndShowPlan(goals, level, time, fav, special);
                SwingUtilities.invokeLater(() -> loading.dispose());
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    loading.dispose();
                    JOptionPane.showMessageDialog(this, "Failed to get workout plan from AI:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // Styles the heading label
    private void styleHeading(JLabel label) {
        label.setForeground(FG);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        label.setHorizontalAlignment(JLabel.CENTER);
    }
    // Styles regular labels
    private void styleLabel(JLabel label) {
        label.setForeground(FG);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 15f));
    }
    // Styles text fields
    private void styleField(JTextField field) {
        field.setBackground(new Color(50,50,50));
        field.setForeground(FG);
        field.setCaretColor(FG);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 15f));
    }
    // Styles buttons for consistent appearance
    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(BTN_TEXT);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 15f));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}