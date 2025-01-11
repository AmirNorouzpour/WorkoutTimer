package com.timer.workouttimer.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.timer.workouttimer.R;
import com.timer.workouttimer.databinding.FragmentHistoryBinding;
import com.timer.workouttimer.databinding.FragmentHomeBinding;
import com.timer.workouttimer.helper.Workout;
import com.timer.workouttimer.helper.WorkoutAdapter;
import com.timer.workouttimer.helper.WorkoutDatabaseHelper;

import java.util.List;


public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        WorkoutDatabaseHelper dbHelper = new WorkoutDatabaseHelper(getActivity());
        List<Workout> workoutList = dbHelper.getAllWorkouts();

        TextView emptyTv = root.findViewById(R.id.emptyTv);
        if (!workoutList.isEmpty())
            emptyTv.setVisibility(View.GONE);

        RecyclerView recyclerView = root.findViewById(R.id.rv_workouts);
        WorkoutAdapter adapter = new WorkoutAdapter(workoutList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}