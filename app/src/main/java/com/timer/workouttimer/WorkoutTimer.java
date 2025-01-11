package com.timer.workouttimer;

import android.app.Application;
import com.timer.workouttimer.helper.TypefaceUtil;

public class WorkoutTimer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceUtil.overrideFont(getApplicationContext(), "Roboto", "font/rm.ttf");
    }
}
