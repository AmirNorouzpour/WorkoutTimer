package com.timer.workouttimer.helper;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.skydoves.powermenu.CircularEffect;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.timer.workouttimer.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private final List<Workout> workoutList;

    public WorkoutAdapter(List<Workout> workoutList) {
        this.workoutList = workoutList;
    }

    ViewGroup _parent;

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        _parent = parent;


        return new WorkoutViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);

        holder.tvWorkoutTime.setText("Workout at " + getRelativeDateTime(workout.getDate()));
        holder.tvDuration.setText("Workout : " + formatTime(workout.getWork()));
        holder.tvExercises.setText("Rest : " + formatTime(workout.getRest()));

        if (workout.getRate() == 1)
            holder.iRate.setImageResource(R.drawable.easy);
        if (workout.getRate() == 2)
            holder.iRate.setImageResource(R.drawable.good);
        if (workout.getRate() == 3)
            holder.iRate.setImageResource(R.drawable.hard);

        PowerMenuItem item = new PowerMenuItem("Are you sure ?", false);
        item.tag = workout;
        PowerMenu powerDialog = new PowerMenu.Builder(_parent.getContext())
                .setFooterView(R.layout.layout_custom_dialog_footer)
                .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                .addItem(item)
                .setMenuRadius(10f)
                .setTextColor(ContextCompat.getColor(_parent.getContext(), R.color.white))
                .setMenuColor(ContextCompat.getColor(_parent.getContext(), R.color.box))
                .setMenuShadow(10f)
                .setWidth(1000)
                .setSelectedEffect(false)
                .build();

        PowerMenuItem delItem = new PowerMenuItem("Delete", false);
        delItem.tag = powerDialog;
        PowerMenu powerMenu = new PowerMenu.Builder(_parent.getContext())
                .addItem(delItem)
                .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT).
                .setMenuRadius(10f) // sets the corner radius.
                .setMenuShadow(10f) // sets the shadow.
                .setTextColor(ContextCompat.getColor(_parent.getContext(), R.color.white))
                .setTextGravity(Gravity.CENTER)
                .setSelectedTextColor(Color.WHITE)
                .setAutoDismiss(true)
                .setCircularEffect(CircularEffect.BODY)
                .setMenuColor(ContextCompat.getColor(_parent.getContext(), R.color.box))
                .setSelectedMenuColor(ContextCompat.getColor(_parent.getContext(), R.color.box))
                .setOnMenuItemClickListener(onMenuItemClickListener)
                .build();

        holder.iMenu.setOnClickListener(powerMenu::showAsDropDown);


    }

    private final OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            if (item.title == "Delete") {
                PowerMenu powerDialog = (PowerMenu) item.tag;
                View footerView;
                if (powerDialog != null) {
                    footerView = powerDialog.getFooterView();

                    Button btn_yes = footerView.findViewById(R.id.btn_yes);
                    Button btn_no = footerView.findViewById(R.id.btn_no);

                    btn_no.setOnClickListener(view -> powerDialog.dismiss());

                    btn_yes.setOnClickListener(view -> {
                        powerDialog.dismiss();
                        Workout workout = (Workout) powerDialog.getItemList().get(0).tag;
                        int p = workoutList.indexOf(workout);
                        WorkoutDatabaseHelper helper = new WorkoutDatabaseHelper(_parent.getContext());
                        if (workout != null) {
                            helper.deleteWorkout(workout.getId());
                            workoutList.remove(workout);
                            notifyItemRemoved(p);
                            notifyItemRangeChanged(p, workoutList.size());
                        }
                    });

                    powerDialog.showAtCenter(_parent);
                }
            }
        }
    };

    public static String getRelativeDateTime(Date date) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            if (date == null) {
                return "";
            }

            Calendar today = Calendar.getInstance();
            Calendar inputDate = Calendar.getInstance();
            inputDate.setTime(date);

            if (today.get(Calendar.YEAR) == inputDate.get(Calendar.YEAR)
                    && today.get(Calendar.DAY_OF_YEAR) == inputDate.get(Calendar.DAY_OF_YEAR)) {
                return "Today " + timeFormat.format(date);
            }

            today.add(Calendar.DAY_OF_YEAR, -1);
            if (today.get(Calendar.YEAR) == inputDate.get(Calendar.YEAR)
                    && today.get(Calendar.DAY_OF_YEAR) == inputDate.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday " + timeFormat.format(date);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            return dateFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkoutTime, tvDuration, tvExercises;
        ImageView iRate, iMenu;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutTime = itemView.findViewById(R.id.tv_workout_time);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvExercises = itemView.findViewById(R.id.tv_exercises);
            iRate = itemView.findViewById(R.id.rate);
            iMenu = itemView.findViewById(R.id.menuBtn);
        }

    }

}
