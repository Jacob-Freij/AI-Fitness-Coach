package view;

import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import model.Workout;
import model.WorkoutPlan;

public class WorkoutPlanPanel extends JPanel {
    // Reference to the main app window so we can switch screens or add workouts
    private final WorkoutApp app;
    // Where the workout plan text is shown
    private JTextArea planArea;
    // Buttons for navigation and actions
    private JButton backBtn;
    private JButton saveBtn;
    private JButton trackerBtn;
    // Holds the current plan object
    private WorkoutPlan plan;

    // Some colors for the dark theme and buttons
    private static final Color BG = new Color(24, 24, 24);
    private static final Color FG = Color.WHITE;
    private static final Color PANEL = new Color(36, 36, 36);
    private static final Color BTN_MAIN = new Color(45, 115, 255); // Blue
    private static final Color BTN_ALT = new Color(220, 53, 69);   // Red
    private static final Color BTN_NEUTRAL = new Color(40, 167, 69); // Green
    private static final Color BTN_TEXT = Color.BLACK;

    public WorkoutPlanPanel(WorkoutApp app) {
        this.app = app;
        setLayout(new BorderLayout(15, 15));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Big title at the top
        JLabel title = new JLabel("Your Custom Workout Plan", JLabel.CENTER);
        styleHeading(title);
        add(title, BorderLayout.NORTH);

        // The text area where the plan is shown
        planArea = new JTextArea();
        styleTextArea(planArea);
        planArea.setEditable(false);
        planArea.setText("Your workout plan will appear here...");
        JScrollPane scroll = new JScrollPane(planArea);
        scroll.setBorder(BorderFactory.createLineBorder(PANEL));
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);

        // Buttons at the bottom (Back, Save as Workout, Go to Tracker)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(BG);

        // Back button goes to the start screen
        backBtn = new JButton("Back to Start");
        styleButton(backBtn, BTN_NEUTRAL);
        backBtn.addActionListener(e -> app.showStart());
        btnPanel.add(backBtn);

        // Save button adds this plan as a workout to the tracker
        saveBtn = new JButton("Save as Workout");
        styleButton(saveBtn, BTN_MAIN);
        saveBtn.addActionListener(this::onSaveWorkout);
        btnPanel.add(saveBtn);

        // Tracker button goes to the workout tracker screen
        trackerBtn = new JButton("Go to Tracker");
        styleButton(trackerBtn, BTN_ALT);
        trackerBtn.addActionListener(e -> app.showWorkoutTracker());
        btnPanel.add(trackerBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    // Sets the current workout plan and updates the display
    public void setWorkoutPlan(WorkoutPlan plan) {
        this.plan = plan;
        setWorkoutText(plan.getContent());
    }

    // Sets the text in the plan area and scrolls to the top
    public void setWorkoutText(String text) {
        planArea.setText(text);
        planArea.setCaretPosition(0);
    }

    // When you click "Save as Workout", this adds the plan to your tracker
    private void onSaveWorkout(ActionEvent e) {
        if (plan == null) {
            JOptionPane.showMessageDialog(this, "No workout plan to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Name is based on your goals, but trimmed if too long
        String name = "Workout Plan - " + plan.getGoals();
        if (name.length() > 50) name = name.substring(0, 47) + "...";
        // Make a Workout object from the plan
        Workout workout = new Workout(name, new Date(), 0, plan.getContent(), "Generated workout plan based on goals: " + plan.getGoals());
        // Add it to the tracker using the controller
        app.getController().addWorkout(workout.getName(), workout.getDate(), workout.getDuration(), workout.getDescription(), workout.getNotes());
        JOptionPane.showMessageDialog(this, "Workout plan saved to tracker!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Styles the big heading label
    private void styleHeading(JLabel label) {
        label.setForeground(FG);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        label.setHorizontalAlignment(JLabel.CENTER);
    }

    // Styles the text area for the plan
    private void styleTextArea(JTextArea area) {
        area.setBackground(PANEL);
        area.setForeground(FG);
        area.setCaretColor(FG);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(area.getFont().deriveFont(Font.PLAIN, 15f));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    }

    // Styles buttons for a consistent look
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