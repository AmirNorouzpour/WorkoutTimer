package com.timer.workouttimer.helper;

public class Workout {
    private final int id;
    private final String date;
    private final int work;
    private final int rest;
    private final int rate;

    public Workout(int id, String date, int work, int rest, int rate) {
        this.id = id;
        this.date = date;
        this.work = work;
        this.rest = rest;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getWork() {
        return work;
    }

    public int getRest() {
        return rest;
    }

    public int getRate() {
        return rate;
    }
}

