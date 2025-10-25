package model;

import java.util.Date;

public class Workout {
    // The name of the workout (like "Push Day" or "Cardio")
    private String name;
    // When the workout happened
    private Date date;
    // How long the workout lasted (in minutes)
    private int duration;
    // Description of what you did (could be sets/reps or just "Running")
    private String desc;
    // Any extra notes you want to remember
    private String notes;

    // When you make a Workout, you give it all the details
    public Workout(String name, Date date, int duration, String desc, String notes) {
        this.name = name;
        this.date = date;
        this.duration = duration;
        this.desc = desc;
        this.notes = notes;
    }

    // Getters and setters let you read or change the info if you need to
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public String getDescription() { return desc; }
    public void setDescription(String desc) { this.desc = desc; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // This makes it easy to turn a Workout into a line for saving to a file
    @Override
    public String toString() {
        return date + "|" + name + "|" + duration + "|" + desc + "|" + notes;
    }
}