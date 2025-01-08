package com.example.workouttimer.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.workouttimer.R;
import com.example.workouttimer.TimerActivity;
import com.example.workouttimer.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    int sets = 2;
    int workTime = 30;
    int restTime = 15;
    boolean skipLastRest = true;
    TextView timer_text, tvMin, tvTime;
    boolean ss, vs, ps;
    int psv, times, secs;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button buttonIncrement = root.findViewById(R.id.button_sets_increment);
        Button buttonDecrement = root.findViewById(R.id.button_sets_decrement);
        TextView textViewValue = root.findViewById(R.id.textview_sets_timer);
        tvMin = root.findViewById(R.id.tvMins);
        tvTime = root.findViewById(R.id.tvTimes);
        timer_text = root.findViewById(R.id.timer_text);
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

        buttonIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sets++;
                textViewValue.setText(String.valueOf(sets));
                SetTotal();
            }
        });

        buttonDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sets > 1) {
                    sets--; //
                    textViewValue.setText(String.valueOf(sets));
                    SetTotal();
                }
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
            if (restTime > 5) {
                restTime -= 5;
                restTimer.setText(formatTime(restTime));
                SetTotal();
            }
        });

        // Skip Last Rest Switch
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch skipLastRest = root.findViewById(R.id.switch_skip_last_rest);
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
        tvMin.setText((secs / 60) + "");
        tvTime.setText(times + "");
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

        times = sharedPreferences.getInt("times", 0);
        secs = sharedPreferences.getInt("secs", 0);
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
        timer_text.setText(formatTime(total));
    }

    @Override
    public void onStart() {
        super.onStart();
        RefreshData();
    }

    public void RefreshData() {
        LoadData();
        tvMin.setText((secs / 60) + "");
        tvTime.setText(times + "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}