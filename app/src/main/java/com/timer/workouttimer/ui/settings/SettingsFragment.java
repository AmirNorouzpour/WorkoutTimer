package com.timer.workouttimer.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.timer.workouttimer.R;
import com.timer.workouttimer.databinding.FragmentSettingsBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch soundSwitch, SwVibrate, SwPrepare;
    boolean soundSwitchValue, VibrateSwValue, PrepareSwValue;
    int PrepareSecValue = 5;
    TextInputEditText prepareEditTime;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        soundSwitch = root.findViewById(R.id.switch_sound);
        SwVibrate = root.findViewById(R.id.switch_vibration);
        SwPrepare = root.findViewById(R.id.switch_prepare);
        prepareEditTime = root.findViewById(R.id.prepare_time_value);

        LoadData();
        soundSwitch.setChecked(this.soundSwitchValue);
        SwVibrate.setChecked(this.VibrateSwValue);
        SwPrepare.setChecked(this.PrepareSwValue);
        prepareEditTime.setText(Integer.toString(PrepareSecValue));

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.soundSwitchValue = isChecked;
            SaveData();
        });

        SwVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.VibrateSwValue = isChecked;
            SaveData();
        });

        SwPrepare.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.PrepareSwValue = isChecked;
            SaveData();
        });

        prepareEditTime.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                prepareEditTime.post(() -> prepareEditTime.selectAll());
            }
            return false;
        });


        prepareEditTime.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    PrepareSecValue = Integer.parseInt(Objects.requireNonNull(s.toString()));
                    SaveData();
                }
            }
        });
        return root;
    }

    private void SaveData() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("ss", soundSwitchValue);
        editor.putBoolean("vs", VibrateSwValue);
        editor.putBoolean("ps", PrepareSwValue);
        editor.putInt("psv", PrepareSecValue);
        editor.apply();
    }

    private void LoadData() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);

        soundSwitchValue = sharedPreferences.getBoolean("ss", true);
        VibrateSwValue = sharedPreferences.getBoolean("vs", false);
        PrepareSwValue = sharedPreferences.getBoolean("ps", true);
        PrepareSecValue = sharedPreferences.getInt("psv", 5);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}