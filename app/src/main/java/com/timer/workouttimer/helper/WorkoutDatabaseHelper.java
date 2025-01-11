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
    private static final String COLUMN_WORK = "work";
    private static final String COLUMN_REST = "rest";
    private static final String COLUMN_RATE = "rate";

    public WorkoutDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_WORKOUTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_WORK + " INTEGER, "
                + COLUMN_REST + " INTEGER, "
                + COLUMN_RATE + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
        onCreate(db);
    }

    public void addWorkout(String date, int work, int rest, int rate) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WORK, work);
        values.put(COLUMN_REST, rest);
        values.put(COLUMN_RATE, rate);

        db.insert(TABLE_WORKOUTS, null, values);
        db.close();
    }

    public WorkoutSummary getWorkoutSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) AS count, SUM(" + COLUMN_WORK + ") AS totalWork, SUM(" + COLUMN_REST + ") AS totalRest FROM " + TABLE_WORKOUTS;
        Cursor cursor = db.rawQuery(query, null);

        WorkoutSummary summary = new WorkoutSummary();
        if (cursor.moveToFirst()) {
            summary.totalWorkouts = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            summary.totalWork = cursor.getInt(cursor.getColumnIndexOrThrow("totalWork"));
            summary.totalRest = cursor.getInt(cursor.getColumnIndexOrThrow("totalRest"));
        }
        cursor.close();
        db.close();
        return summary;
    }

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
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORK)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REST)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RATE))
                );
                workoutList.add(workout);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return workoutList;
    }

    public void deleteAllWorkouts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_WORKOUTS);
        db.close();
    }
}
