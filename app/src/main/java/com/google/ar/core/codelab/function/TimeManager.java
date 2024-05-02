package com.google.ar.core.codelab.function;

import android.content.Context;
import android.content.SharedPreferences;

public class TimeManager {
    private static final String PREFERENCES_FILE = "timer_preferences";
    private static final String TIMER_END_TIME = "timer_end_time";
    private static TimeManager instance;
    private long endTime;

    private TimeManager(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        endTime = prefs.getLong(TIMER_END_TIME, 0);
    }

    public static synchronized TimeManager getInstance(Context context) {
        if (instance == null) {
            instance = new TimeManager(context.getApplicationContext());
        }
        return instance;
    }

    public void resetAndStartTimer(Context context, long durationMillis) {
        endTime = System.currentTimeMillis() + durationMillis;
        // Save the new end time, effectively resetting the timer
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        prefs.edit().putLong(TIMER_END_TIME, endTime).apply();
    }

    public void startTimer(Context context, long durationMillis) {
        endTime = System.currentTimeMillis() + durationMillis;
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        prefs.edit().putLong(TIMER_END_TIME, endTime).apply();
    }

    public long getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        return Math.max(endTime - currentTime, 0);
    }

    public boolean isTimerRunning() {
        return getRemainingTime() > 0;
    }
}
