package com.timer.workouttimer.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "workout.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_WORKOUTS = "workouts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_RATE = "rate";

    public WorkoutDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_WORKOUTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_DURATION + " INTEGER, "
                + COLUMN_RATE + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
        onCreate(db);
    }

    // افزودن رکورد جدید
    public void addWorkout(String date, int duration, int rate) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_RATE, rate);

        db.insert(TABLE_WORKOUTS, null, values);
        db.close();
    }

    public WorkoutSummary getWorkoutSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) AS count, SUM(" + COLUMN_DURATION + ") AS totalDuration FROM " + TABLE_WORKOUTS;
        Cursor cursor = db.rawQuery(query, null);

        WorkoutSummary summary = new WorkoutSummary();
        if (cursor.moveToFirst()) {
            summary.totalWorkouts = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            summary.totalDuration = cursor.getInt(cursor.getColumnIndexOrThrow("totalDuration"));
        }
        cursor.close();
        db.close();
        return summary;
    }

    // بازیابی همه رکوردها
    public List<Workout> getAllWorkouts() {
        List<Workout> workoutList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_WORKOUTS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Workout workout = new Workout(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RATE))
                );
                workoutList.add(workout);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return workoutList;
    }

    // حذف همه رکوردها
    public void deleteAllWorkouts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_WORKOUTS);
        db.close();
    }
}
