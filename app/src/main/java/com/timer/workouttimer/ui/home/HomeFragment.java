package com.timer.workouttimer.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.timer.workouttimer.R;
import com.timer.workouttimer.TimerActivity;
import com.timer.workouttimer.databinding.FragmentHomeBinding;
import com.timer.workouttimer.helper.WorkoutDatabaseHelper;
import com.timer.workouttimer.helper.WorkoutSummary;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    int sets = 2;
    int workTime = 30;
    int restTime = 15;
    boolean skipLastRest = true;
    TextView totalTime, totalWorkout, totalWorkoutMin, totalRestMin;
    boolean ss, vs, ps;
    int psv;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button buttonIncrement = root.findViewById(R.id.button_sets_increment);
        Button buttonDecrement = root.findViewById(R.id.button_sets_decrement);
        TextView textViewValue = root.findViewById(R.id.textview_sets_timer);
        LinearLayout multipleBtn = root.findViewById(R.id.mlutiBtn);

        multipleBtn.setOnClickListener(view -> Toast.makeText(getActivity(), "Will be added in the next version.", Toast.LENGTH_SHORT).show());

        totalWorkout = root.findViewById(R.id.totalWorkout);
        totalWorkoutMin = root.findViewById(R.id.totalWorkoutMin);
        totalRestMin = root.findViewById(R.id.totalRestMin);
        totalTime = root.findViewById(R.id.timer_text);
        ConstraintLayout start_workout = root.findViewById(R.id.start);

        start_workout.setOnClickListener(view -> {
            Intent myIntent = new Intent(getActivity(), TimerActivity.class);
            myIntent.putExtra("sets", sets);
            myIntent.putExtra("work", workTime);
            myIntent.putExtra("rest", restTime);
            myIntent.putExtra("skip", skipLastRest);
            myIntent.putExtra("ss", ss);
            myIntent.putExtra("vs", vs);
            myIntent.putExtra("ps", ps);
            myIntent.putExtra("psv", psv);
            startActivity(myIntent);

            SaveData();
        });

        buttonIncrement.setOnClickListener(v -> {
            sets++;
            textViewValue.setText(String.valueOf(sets));
            SetTotal();
        });

        buttonDecrement.setOnClickListener(v -> {
            if (sets > 1) {
                sets--; //
                textViewValue.setText(String.valueOf(sets));
                SetTotal();
            }
        });


        // Work Section
        TextView workTimer = root.findViewById(R.id.textview_work_timer);
        Button workIncrement = root.findViewById(R.id.button_work_increment);
        Button workDecrement = root.findViewById(R.id.button_work_decrement);

        workIncrement.setOnClickListener(v -> {
            workTime += 5;
            workTimer.setText(formatTime(workTime));
            SetTotal();
        });

        workDecrement.setOnClickListener(v -> {
            if (workTime > 5) {
                workTime -= 5;
                workTimer.setText(formatTime(workTime));
                SetTotal();
            }
        });

        // Rest Section
        TextView restTimer = root.findViewById(R.id.textview_rest_timer);
        Button restIncrement = root.findViewById(R.id.button_rest_increment);
        Button restDecrement = root.findViewById(R.id.button_rest_decrement);

        restIncrement.setOnClickListener(v -> {
            restTime += 5;
            restTimer.setText(formatTime(restTime));
            SetTotal();
        });

        restDecrement.setOnClickListener(v -> {
            if (restTime >= 5) {
                restTime -= 5;
                restTimer.setText(formatTime(restTime));
                SetTotal();
            }
        });

        // Skip Last Rest Switch
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        SwitchCompat skipLastRest = root.findViewById(R.id.switch_skip_last_rest);
        skipLastRest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.skipLastRest = isChecked;
            SetTotal();
        });
        LoadData();
        textViewValue.setText(String.valueOf(sets));
        workTimer.setText(formatTime(workTime));
        restTimer.setText(formatTime(restTime));
        skipLastRest.setChecked(this.skipLastRest);
        SetTotal();

        this.refreshData();
        return root;
    }

    private void SaveData() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("sets", sets);
        editor.putInt("work", workTime);
        editor.putInt("rest", restTime);
        editor.putBoolean("skip", skipLastRest);
        editor.apply();
    }

    private void LoadData() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);

        sets = sharedPreferences.getInt("sets", 2);
        workTime = sharedPreferences.getInt("work", 60);
        restTime = sharedPreferences.getInt("rest", 20);
        skipLastRest = sharedPreferences.getBoolean("skip", true);

        ss = sharedPreferences.getBoolean("ss", true);
        vs = sharedPreferences.getBoolean("vs", false);
        ps = sharedPreferences.getBoolean("ps", true);
        psv = sharedPreferences.getInt("psv", 5);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void SetTotal() {
        int works = sets * workTime;
        int rests = sets * restTime;
        int total = works + rests;
        if (skipLastRest)
            total = total - restTime;
        totalTime.setText(formatTime(total));
        SaveData();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshData();
    }

    public void refreshData() {
        WorkoutDatabaseHelper workoutDatabaseHelper = new WorkoutDatabaseHelper(getActivity());
        WorkoutSummary res = workoutDatabaseHelper.getWorkoutSummary();
        totalWorkoutMin.setText(String.valueOf(res.totalWork / 60));
        totalWorkout.setText(String.valueOf(res.totalWorkouts));
        totalRestMin.setText(String.valueOf((res.totalRest / 60)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::refreshData, 1000);

    }


}