package model;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorkoutManager {
    // Stores all workouts in memory
    private List<Workout> workouts;
    // File where workouts are saved/loaded
    private static final String FILE = "workouts.txt";
    // Date format for saving/loading
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Constructor: loads workouts from file on creation
    public WorkoutManager() {
        workouts = new ArrayList<>();
        load();
    }

    // Adds a workout and saves to file
    public void add(Workout w) {
        workouts.add(w);
        save();
    }
    // Removes a workout and saves to file
    public void remove(Workout w) {
        workouts.remove(w);
        save();
    }
    // Returns a copy of all workouts
    public List<Workout> getAll() {
        return new ArrayList<>(workouts);
    }
    // Returns workouts within a date range (inclusive)
    public List<Workout> getByDate(Date start, Date end) {
        List<Workout> res = new ArrayList<>();
        for (Workout w : workouts) {
            Date d = w.getDate();
            if ((d.equals(start) || d.after(start)) && (d.equals(end) || d.before(end))) res.add(w);
        }
        return res;
    }
    // Returns the most recent 'count' workouts, sorted by date (newest first)
    public List<Workout> getRecent(int count) {
        List<Workout> sorted = new ArrayList<>(workouts);
        sorted.sort((w1, w2) -> w2.getDate().compareTo(w1.getDate()));
        int n = Math.min(count, sorted.size());
        return sorted.subList(0, n);
    }
    // Loads workouts from the file into memory
    private void load() {
        workouts.clear(); // clear existing workouts so it only loads whats in the txt file
        File file = new File(FILE);
        if (!file.exists()) return; // if file doesn't exist, skips loading
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = in.readLine()) != null) {
                if (first) { first = false; continue; } // skips the header
                if (line.trim().isEmpty()) continue; // skips empty lines
                String[] p = line.split("\\|", 5); // splits by |, max 5 parts
                // if there are at least 5 parts, tries to place them into a Workout object
                if (p.length >= 5) {
                    try {
                        Date date = sdf.parse(p[0].trim());
                        String name = p[1].trim();
                        int duration = Integer.parseInt(p[2].trim());
                        String desc = p[3].trim();
                        String notes = p[4].trim();
                        workouts.add(new Workout(name, date, duration, desc, notes));
                    } catch (ParseException | NumberFormatException e) {
                        System.err.println("Error Integrating workout: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading workouts: " + e.getMessage());
        }
    }
    // Saves all workouts saves by writing them to the workouts.txt file
    private void save() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE))) {
            out.println("# Workout Data - Format: Date|Name|Duration|Description|Notes");
            for (Workout w : workouts) {
                String d = sdf.format(w.getDate());
                out.println(d + "|" + w.getName() + "|" + w.getDuration() + "|" + w.getDescription() + "|" + w.getNotes());
            }
        } catch (IOException e) {
            System.err.println("Error saving workouts: " + e.getMessage());
        }
    }
}