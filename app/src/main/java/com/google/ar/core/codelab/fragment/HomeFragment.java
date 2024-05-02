package com.google.ar.core.codelab.fragment;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.ar.core.codelab.Activities.ImageCheckingActivity;
import com.google.ar.core.codelab.Activities.QuestionaireActivity;
import com.google.ar.core.codelab.depth.DepthCodelabActivity;
import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.function.TimeManager;
import com.google.ar.core.codelab.function.TimerEndedReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private TextView reminderDate;
    private TextView reminderTime;
    private CountDownTimer countDownTimer;

    public HomeFragment(){
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the TextViews
        reminderDate = view.findViewById(R.id.reminderDate);
        reminderTime = view.findViewById(R.id.reminderTime);

        View tstSkinTestingCardView = view.findViewById(R.id.TSTSkinTesting);
        tstSkinTestingCardView.setOnClickListener(v -> navigateToDepthActivity());

        // Set the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE M/dd/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        reminderDate.setText(currentDate);

        // Start the countdown timer for 2 days (Example: 2 days from now)
        TimeManager timerManager = TimeManager.getInstance(getContext());
        //timerManager.resetAndStartTimer(getContext(), TimeUnit.SECONDS.toMillis(10));
        if (!timerManager.isTimerRunning()) {
            timerManager.startTimer(getContext(), TimeUnit.DAYS.toMillis(2)); // or any duration
        }

        // Update the UI based on the remaining time.
        updateTimerUI(timerManager.getRemainingTime());
        long endTime = timerManager.getRemainingTime();
        scheduleReminderAlarms(endTime);

        // Keep the UI updated with the remaining time.
        countDownTimer = new CountDownTimer(timerManager.getRemainingTime(), 1000) {
            public void onTick(long millisUntilFinished) {
                updateTimerUI(millisUntilFinished);
            }
            public void onFinish() {
                reminderTime.setText("00 : 00 : 00");
            }
        }.start();


    }

    private void updateTimerUI(long millisUntilFinished) {
        String time = String.format(Locale.getDefault(), "%02d : %02d : %02d",
                TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));
        reminderTime.setText(time);
    }


    private void scheduleReminderAlarms(long durationMillis) {
        Context context = getContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        long endTime = System.currentTimeMillis() + durationMillis;

        long currentTime = System.currentTimeMillis();
        long oneDayBeforeEndTime =  TimeUnit.DAYS.toMillis(1);
        long oneHourBeforeEndTime = TimeUnit.HOURS.toMillis(1);

        if (oneDayBeforeEndTime > currentTime) {
            scheduleAlarm(context, alarmManager, oneDayBeforeEndTime, "Reminder: Take your picture tomorrow!", 1001);
        }
        if (oneHourBeforeEndTime > currentTime) {
            scheduleAlarm(context, alarmManager, oneHourBeforeEndTime, "Reminder: Take your picture in one hour!", 1002);
        }
        scheduleAlarm(context, alarmManager, endTime, "Time to take your picture!", 1000);
    }


    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAlarm(Context context, AlarmManager alarmManager, long triggerAtMillis, String message, int notificationId) {
        Intent intent = new Intent(context, TimerEndedReceiver.class);
        intent.putExtra("notification_message", message);
        intent.putExtra("notification_id", notificationId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, alarmIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, alarmIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, alarmIntent);
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void navigateToDepthActivity() {
        // Make sure to use the correct context and class names
        Intent intent = new Intent(getActivity(), DepthCodelabActivity.class);
        startActivity(intent);
    }


}
