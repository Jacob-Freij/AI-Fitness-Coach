package view;

import controller.WorkoutController;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import model.Workout;

/**
 * Panel for tracking and managing workouts.
 */
public class WorkoutTrackerPanel extends JPanel {
    // This is the main app window, so we can talk to it and switch screens
    private final WorkoutApp app;
    // Table and its model for showing all your workouts
    private JTable table;
    private DefaultTableModel model;
    // Buttons for navigation and actions
    private JButton backBtn, addBtn, delBtn;
    // Where the workout plan is shown
    private JTextArea planArea;
    // The panel that pops up for adding a workout
    private JPanel inputPanel;
    // Fields for entering workout info
    private JTextField nameField, durationField, setsField, repsField, weightField;
    private JTextArea notesField;
    // Radio buttons to pick how you want to track (duration or sets/reps)
    private JRadioButton durationRadio, setsRepsRadio;

    // Some colors for the dark theme
    private static final Color BG_COLOR = new Color(50,50,50); // dark gray/black
    private static final Color FG_COLOR = Color.WHITE; // white text
    private static final Color PANEL_COLOR = new Color(36, 36, 36);
    private static final Color FIELD_COLOR = new Color(32, 32, 32);

    // Button colors
    private static final Color BTN_BG_MAIN = new Color(45, 115, 255); // Blue
    private static final Color BTN_BG_ALT = new Color(220, 53, 69);  // Red
    private static final Color BTN_BG_NEUTRAL = new Color(40, 167, 69); // Green
    private static final Color BTN_BG_WHITE = Color.WHITE;
    private static final Color BTN_TEXT = Color.BLACK;
    private static final Color BTN_TEXT_DARK = Color.BLACK;

    // Fonts for headings and labels
    private static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 22);
    private static final Font FONT_SUBHEADING = new Font("SansSerif", Font.BOLD, 16);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 14);

    public WorkoutTrackerPanel(WorkoutApp app) {
        this.app = app;

        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Big title at the top
        JLabel title = new JLabel("Workout Tracker", JLabel.CENTER);
        styleHeading(title);
        add(title, BorderLayout.NORTH);

        // Split the panel into left (plan) and right (tracker)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); // left/right split
        split.setDividerLocation(450); // divider starts at 450px from the left
        split.setDividerSize(10); // divider bar is 10px wide
        split.setBorder(null); // no border for a clean look

        // Left side: shows your workout plan
        JPanel planPanel = createPlanDisplayPanel();
        split.setLeftComponent(planPanel);

        // Right side: shows your workout log and add form
        JPanel trackPanel = createTrackingPanel();
        split.setRightComponent(trackPanel);

        add(split, BorderLayout.CENTER);

        // Buttons at the bottom (Back, Add, Delete)
        JPanel btnPanel = createButtonPanel();
        add(btnPanel, BorderLayout.SOUTH);

        // Load the workouts into the table when this panel is created
        refreshWorkouts();
    }

    // Makes the panel that shows your workout plan and a button to open the plan file
    private JPanel createPlanDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_COLOR);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);

        // "Your Workout Plan" label
        JLabel planTitle = new JLabel("Your Workout Plan");
        styleSubheading(planTitle);
        headerPanel.add(planTitle, BorderLayout.CENTER);

        // Button to open the plan file in your text editor
        JButton openFileBtn = new JButton("Open Plan File");
        styleButton(openFileBtn, BTN_BG_WHITE, BTN_TEXT_DARK); // White background, black text
        openFileBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        openFileBtn.addActionListener(e -> openPlanFile());
        headerPanel.add(openFileBtn, BorderLayout.EAST);

        // The text area where the plan is shown
        planArea = new JTextArea();
        styleTextArea(planArea);
        planArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(planArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(PANEL_COLOR));
        scrollPane.getViewport().setBackground(BG_COLOR);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Opens the workout plan file if it exists, or shows a message if not
    private void openPlanFile() {
        File planFile = new File("workout_plan.txt");
        if (!planFile.exists()) {
            JOptionPane.showMessageDialog(this, 
                "No workout plan file found. Generate a plan first.", 
                "File Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Tries to open the file with your default text editor
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(planFile);
            } else {
                throw new UnsupportedOperationException("System doesn't support opening files, or this type of file.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Could not open the workout plan file: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Makes the right side: the workout table and the add workout form
    private JPanel createTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_COLOR);

        // Table that shows all your workouts
        createWorkoutTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(FIELD_COLOR);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(PANEL_COLOR));

        // The form for adding a new workout (hidden by default)
        inputPanel = createInputPanel();
        inputPanel.setVisible(false);

        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Makes the form for adding a new workout
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PANEL_COLOR),
            "Add Workout", TitledBorder.LEFT, TitledBorder.TOP,
            FONT_SUBHEADING, FG_COLOR));
        panel.setBackground(BG_COLOR);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 7));
        formPanel.setBackground(BG_COLOR);

        // Name field
        JLabel nameLabel = new JLabel("Exercise Name:");
        styleLabel(nameLabel);
        formPanel.add(nameLabel);
        nameField = new JTextField();
        styleField(nameField);
        formPanel.add(nameField);

        // Radio buttons to pick how you want to track (duration or sets/reps)
        ButtonGroup group = new ButtonGroup();
        durationRadio = new JRadioButton("Duration (minutes)");
        setsRepsRadio = new JRadioButton("Sets x Reps + Weight");
        styleRadioButton(durationRadio);
        styleRadioButton(setsRepsRadio);
        group.add(durationRadio);
        group.add(setsRepsRadio);
        durationRadio.setSelected(true);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.setBackground(BG_COLOR);
        radioPanel.add(durationRadio);
        radioPanel.add(setsRepsRadio);

        // Add radio buttons to the form
        JLabel trackLabel = new JLabel("Track by:");
        styleLabel(trackLabel);
        formPanel.add(trackLabel);
        formPanel.add(radioPanel);

        // Duration field
        JLabel durationLabel = new JLabel("Duration (minutes):");
        styleLabel(durationLabel);
        formPanel.add(durationLabel);
        durationField = new JTextField();
        styleField(durationField);
        formPanel.add(durationField);

        // Sets, Reps, Weight fields (start disabled)
        JLabel setsLabel = new JLabel("Sets:");
        styleLabel(setsLabel);
        formPanel.add(setsLabel);
        setsField = new JTextField();
        styleField(setsField);
        setsField.setEnabled(false);
        formPanel.add(setsField);

        JLabel repsLabel = new JLabel("Reps:");
        styleLabel(repsLabel);
        formPanel.add(repsLabel);
        repsField = new JTextField();
        styleField(repsField);
        repsField.setEnabled(false);
        formPanel.add(repsField);

        JLabel weightLabel = new JLabel("Weight (lbs/kg):");
        styleLabel(weightLabel);
        formPanel.add(weightLabel);
        weightField = new JTextField();
        styleField(weightField);
        weightField.setEnabled(false);
        formPanel.add(weightField);

        // Notes area
        JLabel notesLabel = new JLabel("Notes:");
        styleLabel(notesLabel);
        formPanel.add(notesLabel);
        notesField = new JTextArea(3, 20);
        styleTextArea(notesField);
        JScrollPane notesScroll = new JScrollPane(notesField);
        formPanel.add(notesScroll);

        // When you pick duration, only duration field is enabled
        durationRadio.addActionListener(e -> {
            durationField.setEnabled(true);
            setsField.setEnabled(false);
            repsField.setEnabled(false);
            weightField.setEnabled(false);
        });

        // When you pick sets/reps, those fields are enabled
        setsRepsRadio.addActionListener(e -> {
            durationField.setEnabled(false);
            setsField.setEnabled(true);
            repsField.setEnabled(true);
            weightField.setEnabled(true);
        });

        // Save and Cancel buttons for the form
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BG_COLOR);

        JButton saveBtn = new JButton("Save");
        styleButton(saveBtn, BTN_BG_NEUTRAL, BTN_TEXT); // Green background, white text
        saveBtn.addActionListener(e -> saveWorkout());

        JButton cancelBtn = new JButton("Cancel");
        styleButton(cancelBtn, BTN_BG_ALT, BTN_TEXT); // Red background, white text
        cancelBtn.addActionListener(e -> inputPanel.setVisible(false));

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        // Put the form and buttons in a scrollable panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.add(formPanel);
        contentPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Makes the panel with Back, Add Workout, and Delete Selected buttons
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setBackground(BG_COLOR);

        // Back button goes to the start screen
        backBtn = new JButton("Back");
        styleButton(backBtn, BTN_BG_NEUTRAL, BTN_TEXT); // Green background, white text
        backBtn.addActionListener(e -> app.showStart());
        panel.add(backBtn);

        // Add Workout button shows the add workout form
        addBtn = new JButton("Add Workout");
        styleButton(addBtn, BTN_BG_MAIN, BTN_TEXT); // Blue background, white text
        addBtn.addActionListener(e -> showAddWorkoutForm());
        panel.add(addBtn);

        // Delete Selected button removes the selected workout
        delBtn = new JButton("Delete Selected");
        styleButton(delBtn, BTN_BG_ALT, BTN_TEXT); // Red background, white text
        delBtn.addActionListener(e -> deleteSelectedWorkout());
        delBtn.setEnabled(false); // Disabled until a row is selected
        panel.add(delBtn);

        return panel;
    }

    // Styles a button with background and foreground colors
    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(BTN_TEXT);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Sets up the table for showing workouts
    private void createWorkoutTable() {
        String[] columnNames = {"Date", "Exercise", "Duration/Details", "Notes"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // can't edit cells directly
            }
        };

        table = new JTable(model);
        table.setBackground(FIELD_COLOR);
        table.setForeground(FG_COLOR);
        table.setGridColor(PANEL_COLOR);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(PANEL_COLOR);
        table.getTableHeader().setForeground(BG_COLOR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Enable Delete button only when a row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            delBtn.setEnabled(hasSelection);
        });

        // Set column widths for a nice look
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(120);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(200);
    }

    // Loads workouts from the controller and updates the table
    public void refreshWorkouts() {
        WorkoutController controller = app.getController();
        model.setRowCount(0); // clear the table
        List<Workout> workouts = controller.getAllWorkouts();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Workout workout : workouts) {
            String dateStr = sdf.format(workout.getDate());
            String detailsStr;

            // If the description has sets/reps, show that, otherwise show duration
            if (workout.getDescription().contains("sets") && workout.getDescription().contains("reps")) {
                detailsStr = workout.getDescription();
            } else {
                detailsStr = workout.getDuration() + " mins";
            }

            model.addRow(new Object[]{
                dateStr,
                workout.getName(),
                detailsStr,
                workout.getNotes()
            });
        }

        delBtn.setEnabled(false); // disable delete until a row is selected

        // Update the workout plan display on the left
        updatePlanDisplay();
    }

    // Shows the add workout form and resets all fields
    private void showAddWorkoutForm() {
        nameField.setText("");
        durationField.setText("");
        setsField.setText("");
        repsField.setText("");
        weightField.setText("");
        notesField.setText("");

        // Default to duration tracking
        durationRadio.setSelected(true);
        durationField.setEnabled(true);
        setsField.setEnabled(false);
        repsField.setEnabled(false);
        weightField.setEnabled(false);

        inputPanel.setVisible(true);
    }

    // Saves a new workout from the form fields
    private void saveWorkout() {
        String name = nameField.getText().trim();
        String notes = notesField.getText().trim();

        // Must have a name
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter an exercise name.", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String description;
        int duration = 0;

        // If tracking by duration
        if (durationRadio.isSelected()) {
            try {
                duration = Integer.parseInt(durationField.getText().trim());
                description = duration + " minutes";
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid duration in minutes.", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // If tracking by sets/reps/weight
            String sets = setsField.getText().trim();
            String reps = repsField.getText().trim();
            String weight = weightField.getText().trim();

            if (sets.isEmpty() || reps.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter both sets and reps.", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            description = sets + " sets × " + reps + " reps";
            if (!weight.isEmpty()) {
                description += " @ " + weight + " lbs";
            }

            // Just a guess: 2 minutes per set for duration
            try {
                int numSets = Integer.parseInt(sets);
                duration = numSets * 2;
            } catch (NumberFormatException e) {
                duration = 0;
            }
        }

        // Add the workout using the controller
        app.getController().addWorkout(name, new Date(), duration, description, notes);

        refreshWorkouts(); // update the table
        inputPanel.setVisible(false); // hide the form
    }

    // Deletes the selected workout from the table and the data
    private void deleteSelectedWorkout() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return; // nothing selected
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this workout?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            List<Workout> workouts = app.getController().getAllWorkouts();
            if (selectedRow < workouts.size()) {
                app.getController().removeWorkout(workouts.get(selectedRow));
                refreshWorkouts();
            }
        }
    }

    // Updates the workout plan area with the latest plan
    public void updatePlanDisplay() {
        String plan = app.getLastGeneratedPlan();
        if (plan != null && !plan.isEmpty()) {
            planArea.setText(plan);
            planArea.setCaretPosition(0);
        } else {
            planArea.setText("No workout plan has been generated yet.\n\n" +
                "Go back to the main screen and generate a workout plan to see it displayed here.\n\n" +
                "You can also view your workout plan in the file: workout_plan.txt");
        }
    }

    // --- Styling helpers ---

    // Styles the big heading label
    private void styleHeading(JLabel label) {
        label.setForeground(FG_COLOR);
        label.setFont(FONT_HEADING);
        label.setHorizontalAlignment(JLabel.CENTER);
    }

    // Styles subheadings
    private void styleSubheading(JLabel label) {
        label.setForeground(FG_COLOR);
        label.setFont(FONT_SUBHEADING);
        label.setHorizontalAlignment(JLabel.CENTER);
    }

    // Styles regular labels
    private void styleLabel(JLabel label) {
        label.setForeground(FG_COLOR);
        label.setFont(FONT_LABEL);
    }

    // Styles text fields
    private void styleField(JTextField field) {
        field.setBackground(FIELD_COLOR);
        field.setForeground(FG_COLOR);
        field.setCaretColor(FG_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));
        field.setFont(FONT_LABEL);
    }

    // Styles text areas
    private void styleTextArea(JTextArea area) {
        area.setBackground(PANEL_COLOR);
        area.setForeground(FG_COLOR);
        area.setCaretColor(FG_COLOR);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(FONT_LABEL);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    }

    // Styles radio buttons
    private void styleRadioButton(JRadioButton radio) {
        radio.setBackground(BG_COLOR);
        radio.setForeground(FG_COLOR);
        radio.setFont(FONT_LABEL);
    }
}