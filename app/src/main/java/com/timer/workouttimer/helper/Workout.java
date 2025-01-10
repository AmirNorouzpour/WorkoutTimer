package com.timer.workouttimer.helper;

public class Workout {
    private int id;
    private String date;
    private int duration;
    private int rate;

    public Workout(int id, String date, int duration, int rate) {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getDuration() {
        return duration;
    }

    public int getRate() {
        return rate;
    }
}
