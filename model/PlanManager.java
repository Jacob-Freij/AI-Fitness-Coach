package model;

import java.io.*;
import java.text.SimpleDateFormat;

public class PlanManager {
    // This is the file where the workout plan is saved and loaded from
    private static final String FILE = "workout_plan.txt";

    // Saves a WorkoutPlan to the file, including some header info
    public static void savePlan(WorkoutPlan plan) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE))) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            out.println("# WORKOUT PLAN");
            out.println("# Generated: " + sdf.format(plan.getCreated()));
            out.println("# Goals: " + plan.getGoals());
            out.println("# Level: " + plan.getLevel());
            out.println("# Time: " + plan.getTime());
            // Only print favorite and special if they aren't empty
            if (plan.getFav() != null && !plan.getFav().isEmpty()) out.println("# Favorite: " + plan.getFav());
            if (plan.getSpecial() != null && !plan.getSpecial().isEmpty()) out.println("# Special: " + plan.getSpecial());
            out.println("#");
            out.println("# ========================");
            out.println();
            // The actual workout plan content goes here
            out.println(plan.getContent());
            System.out.println("Workout plan saved to " + FILE);
        } catch (IOException e) {
            System.err.println("Error saving workout plan: " + e.getMessage());
        }
    }

    // Loads just the plan content (skips all the header lines)
    public static String loadPlanContent() {
        File file = new File(FILE);
        if (!file.exists()) return null; // If the file doesn't exist, nothing to load
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            boolean inPlan = false;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("#")) continue; // skip header lines
                // When we hit the first empty line after headers, start reading the plan
                if (line.trim().isEmpty() && !inPlan) { inPlan = true; continue; }
                if (inPlan) sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            System.err.println("Error loading workout plan: " + e.getMessage());
            return null;
        }
    }

    // Checks if the plan file exists and isn't empty
    public static boolean planExists() {
        File file = new File(FILE);
        return file.exists() && file.length() > 0;
    }
}