package controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import model.AiClient;
import model.PlanManager;
import model.Workout;
import model.WorkoutManager;
import model.WorkoutPlan;

/**
 * Controller class that mediates between the UI and the model.
 * Implements the Singleton pattern.
 */
public class WorkoutController {
    // This is the only instance of the controller (singleton pattern)
    private static WorkoutController instance;
    // Handles all the workout data (add, remove, get, etc.)
    private WorkoutManager workoutManager = new WorkoutManager();
    // Talks to the AI to generate plans
    private AiClient aiClient;
    // Stores the current workout plan
    private WorkoutPlan plan;

    // Private constructor so only one controller can be made
    private WorkoutController(String apiKey) {
        this.aiClient = new AiClient(apiKey);
    }

    // Gets the one and only controller instance (makes it if it doesn't exist yet)
    public static synchronized WorkoutController getInstance(String apiKey) {
        if (instance == null) {
            instance = new WorkoutController(apiKey);
        }
        return instance;
    }

    // Asks the AI to make a workout plan, saves it, and returns it
    public WorkoutPlan generateWorkoutPlan(String goals, String level, String time, String fav, String special) throws IOException {
        String content = this.aiClient.generateWorkoutPlan(goals, level, time, fav, special);
        this.plan = new WorkoutPlan(content, goals, level, time, fav, special);
        PlanManager.savePlan(this.plan);
        return this.plan;
    }

    // Gets the current plan (loads from file if needed)
    public WorkoutPlan getCurrentPlan() {
        if (this.plan == null && PlanManager.planExists()) {
            String content = PlanManager.loadPlanContent();
            if (content != null) {
                this.plan = new WorkoutPlan(content, "", "", "", "", "");
            }
        }
        return this.plan;
    }

    // Gets just the text/content of the current plan
    public String getCurrentPlanContent() {
        WorkoutPlan p = this.getCurrentPlan();
        return p != null ? p.getContent() : null;
    }

    // Adds a workout to the list (and saves it)
    public void addWorkout(String name, Date date, int duration, String desc, String notes) {
        Workout w = new Workout(name, date, duration, desc, notes);
        this.workoutManager.add(w);
    }

    // Removes a workout from the list (and saves the new list)
    public void removeWorkout(Workout w) {
        this.workoutManager.remove(w);
    }

    // Gets all workouts as a list
    public List<Workout> getAllWorkouts() {
        return this.workoutManager.getAll();
    }

    // Gets workouts between two dates
    public List<Workout> getWorkoutsByDateRange(Date start, Date end) {
        return this.workoutManager.getByDate(start, end);
    }

    // Gets the most recent 'count' workouts
    public List<Workout> getRecentWorkouts(int count) {
        return this.workoutManager.getRecent(count);
    }
}