package view;

import controller.WorkoutController;
import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import model.WorkoutPlan;

public class WorkoutApp extends JFrame {
    // API key for the AI service (DONT SHARE)
    public static final String API_KEY = "YOU THOUHT I WOULD LEAK MY API KEY? THINK AGAIN.";
    // Colors for the app's dark theme
    public static final Color BG = Color.BLACK;
    public static final Color PANEL_BG = Color.BLACK;
    public static final Color FIELD_BG = Color.BLACK;
    public static final Color TEXT = Color.BLACK;

    // CardLayout lets us switch between different screens (panels)
    private CardLayout layout;
    private JPanel mainPanel;
    // The three main screens of the app
    private StartPanel startPanel;
    private WorkoutPlanPanel planPanel;
    private WorkoutTrackerPanel trackerPanel;
    // The controller connects the UI to the data and AI
    private WorkoutController controller;
    // Stores the last generated workout plan text
    private String lastPlan;

    // This is the main window setup
    public WorkoutApp() {
        // Get the controller (singleton pattern, so only one exists)
        controller = WorkoutController.getInstance(API_KEY);
        setTitle("AI Workout Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // window size
        setLocationRelativeTo(null); // center on screen

        // Use CardLayout so we can switch between screens easily
        layout = new CardLayout();
        mainPanel = new JPanel(layout);

        // Create each screen and give them a reference to this app
        startPanel = new StartPanel(this);
        planPanel = new WorkoutPlanPanel(this);
        trackerPanel = new WorkoutTrackerPanel(this);

        // Add each screen to the main panel with a name
        mainPanel.add(startPanel, "START");
        mainPanel.add(planPanel, "PLAN");
        mainPanel.add(trackerPanel, "TRACKER");

        // Add the main panel to the window
        add(mainPanel);

        // Show the start screen first
        showStart();
    }

    // Switch to the start screen
    public void showStart() {
        layout.show(mainPanel, "START");
    }

    // Generate a workout plan using the AI and show the plan screen
    public void generateAndShowPlan(String goals, String experience, String time, String fav, String special) {
        try {
            // Ask the controller to generate a plan
            WorkoutPlan plan = controller.generateWorkoutPlan(goals, experience, time, fav, special);
            this.lastPlan = plan.getContent(); // save the plan text
            planPanel.setWorkoutPlan(plan); // show the plan in the plan panel
            layout.show(mainPanel, "PLAN"); // switch to the plan screen
        } catch (IOException e) {
            // If something goes wrong, show an error message
            JOptionPane.showMessageDialog(this, "Error generating workout plan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Switch to the workout tracker screen and refresh the data
    public void showWorkoutTracker() {
        trackerPanel.refreshWorkouts();
        layout.show(mainPanel, "TRACKER");
    }

    // Get the controller so panels can add/remove workouts, etc.
    public WorkoutController getController() {
        return controller;
    }

    // Get the last generated plan text (used by tracker panel to display the plan)
    public String getLastGeneratedPlan() {
        if (lastPlan == null || lastPlan.isEmpty()) {
            String planContent = controller.getCurrentPlanContent();
            if (planContent != null) {
                lastPlan = planContent;
            }
        }
        return lastPlan;
    }

    // This is the entry point for the whole app
    public static void main(String[] args) {
        try {
            // Make the app look like the user's operating system
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Start the app on the Swing event thread
        SwingUtilities.invokeLater(() -> {
            WorkoutApp app = new WorkoutApp();
            app.setVisible(true);
        });
    }
}