package tests;

import controller.WorkoutController;
import model.Workout;
import model.WorkoutManager;
import model.WorkoutPlan;
import model.PlanManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tests {
    public static void main(String[] args) throws Exception {
        System.out.println("Running tests...");

        int count = 0;
        int passed = 0;

        // Prepare date and duration
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse("2025-06-15");
        int duration = 30;

        // Test adding and retrieving a workout
        count++;
        WorkoutManager manager = new WorkoutManager();
        Workout w = new Workout("Test Workout", date, duration, "Push-ups", "Felt good");
        manager.add(w);
        boolean found = manager.getAll().stream().anyMatch(workout -> workout.getName().equals("Test Workout"));
        if (found) passed++;
        System.out.println("Add/Retrieve Workout: " + (found ? "SUCCESS" : "FAIL"));

        // Test saving and loading a workout plan
        count++;
        WorkoutPlan plan = new WorkoutPlan("Plan Content", "Build muscle", "Beginner", "3 hours", "Push-ups", "");
        PlanManager.savePlan(plan);
        String loadedContent = PlanManager.loadPlanContent();
        System.out.println("Loaded content: [" + loadedContent + "]");
        boolean planMatch = loadedContent != null && loadedContent.trim().equals("Plan Content");
        if (planMatch) passed++;
        System.out.println("Save/Load Plan: " + (planMatch ? "SUCCESS" : "FAIL"));

        // Test invalid input (empty workout name)
        count++;
        Workout invalid = new Workout("", date, duration, "Push-ups", "");
        boolean validName = !invalid.getName().isEmpty();
        if (!validName) passed++;
        System.out.println("Invalid Input (Empty Name): " + (!validName ? "SUCCESS" : "FAIL"));

        // Test controller integration (simple)
        count++;
        WorkoutController controller = WorkoutController.getInstance("AIzaSyDFUMdIN2OoBQbaIHRF9ntV-uVOwfV4tlo");
        boolean controllerExists = controller != null;
        if (controllerExists) passed++;
        System.out.println("Controller Instance: " + (controllerExists ? "SUCCESS" : "FAIL"));

        // Summary
        System.out.println("Total tests: " + count + ", Passed: " + passed);
        if (count == passed) {
            System.out.println("All tests passed!");
        } else {
            System.out.println("Some tests failed. Please check the output.");
        }
    }
}