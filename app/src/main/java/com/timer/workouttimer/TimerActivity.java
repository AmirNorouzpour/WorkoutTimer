package com.timer.workouttimer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.timer.workouttimer.helper.WorkoutDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimerActivity extends AppCompatActivity {

    private TextView progressTextTime, resultTime; // TextView برای نمایش متن زمان و ست
    private TextView progressTextPhase; // TextView برای نمایش متن زمان و ست
    private int totalSets; // تعداد کل ست‌ها (برای نمایش شماره ست‌ها)
    private TextView totalTimerText; // TextView برای تایمر کلی
    private ImageButton closeBtn;
    private FrameLayout timerBox;
    private LinearLayout endBox;
    private Button closeEndBtn;
    private int totalRemainingTime; // زمان کل باقی‌مانده به ثانیه
    private Handler totalTimerHandler = new Handler();
    private Runnable totalTimerRunnable;
    private SoundPool soundPool;
    private int sound1, sound2, sound3, sound4, sound5; // شناسه‌های صداها
    private int lastTimeRead = 1000; // برای جلوگیری از پخش چندباره
    boolean isFirst = true;
    private boolean ss, vs, ps;
    private int psv;
    private int rate = 2;
    private ImageView hard, good, easy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_timer);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // دریافت داده‌های Intent
        totalSets = getIntent().getIntExtra("sets", 2); // تعداد کل ست‌ها
        int work = getIntent().getIntExtra("work", 30); // مدت زمان work
        int rest = getIntent().getIntExtra("rest", 15); // مدت زمان rest
        boolean skip = getIntent().getBooleanExtra("skip", true); // آیا استراحت آخر باید رد شود
        ss = getIntent().getBooleanExtra("ss", true);
        vs = getIntent().getBooleanExtra("vs", false);
        ps = getIntent().getBooleanExtra("ps", true);
        psv = getIntent().getIntExtra("psv", 5);

        resultTime = findViewById(R.id.resultTime);
        hard = findViewById(R.id.hard);
        hard.setOnClickListener(view -> {
            rate = 3;
            hard.setBackgroundColor(Color.parseColor("#333333"));
            easy.setBackgroundColor(Color.parseColor("#141212"));
            good.setBackgroundColor(Color.parseColor("#141212"));
        });

        good = findViewById(R.id.good);
        good.setOnClickListener(view -> {
            rate = 2;
            good.setBackgroundColor(Color.parseColor("#333333"));
            easy.setBackgroundColor(Color.parseColor("#141212"));
            hard.setBackgroundColor(Color.parseColor("#141212"));
        });

        easy = findViewById(R.id.easy);
        easy.setOnClickListener(view -> {
            rate = 1;
            easy.setBackgroundColor(Color.parseColor("#333333"));
            hard.setBackgroundColor(Color.parseColor("#141212"));
            good.setBackgroundColor(Color.parseColor("#141212"));
        });


        if (ss) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .build();

            sound1 = soundPool.load(this, R.raw.number_1, 1);
            sound2 = soundPool.load(this, R.raw.number_2, 1);
            sound3 = soundPool.load(this, R.raw.number_3, 1);
            sound4 = soundPool.load(this, R.raw.number_4, 1);
            sound5 = soundPool.load(this, R.raw.number_5, 1);
        }

        // دسترسی به TextView
        progressTextPhase = findViewById(R.id.progress_text_phase);
        progressTextTime = findViewById(R.id.progress_text_time);
        totalTimerText = findViewById(R.id.total_timer);
        closeBtn = findViewById(R.id.close_button);
        closeEndBtn = findViewById(R.id.closeEndbtn);
        timerBox = findViewById(R.id.timerBox);
        endBox = findViewById(R.id.endBox);

        closeBtn.setOnClickListener(v -> this.finish());
        closeEndBtn.setOnClickListener(view -> this.finish());

        psv = ps ? psv : 0; // زمان آماده‌سازی
        totalRemainingTime = (work + rest) * totalSets + psv - (skip ? rest : 0);

        // شروع تایمر کل
        startTotalTimer();

        // اجرای انیمیشن‌ها
        executeSet(totalSets, work, rest, skip);
    }

    private void executeSet(int sets, int workTime, int restTime, boolean skipLastRest) {
        if (sets <= 0) return; // اگر تعداد ست‌ها صفر باشد، کار متوقف می‌شود.

        int currentSetNumber = totalSets - sets + 1; // محاسبه شماره ست از 1 شروع شود

        // نمایش Work
        if (isFirst && ps)
            updateProgressText(currentSetNumber, "Prepare", -1);
        executeProgress(isFirst && ps ? psv : 0, "#696969", () -> {

            updateProgressText(currentSetNumber, "Work", totalSets);
            executeProgress(isFirst ? workTime - 1 : workTime, "#008080", () -> { // اجرای Work
                if (sets > 1 || !skipLastRest) { // نمایش Rest
                    isFirst = false;
                    updateProgressText(currentSetNumber, "Rest", skipLastRest ? totalSets - 1 : totalSets);
                    executeProgress(restTime, "#1E90FF", () -> {
                        executeSet(sets - 1, workTime, restTime, skipLastRest); // اجرای ست بعدی
                    });
                } else {
                    SaveTimes(UsedSecs - psv);
                }
            });
        });

    }

    private void executeProgress(int time, String color, Runnable onComplete) {

        View bottomView = findViewById(R.id.bottom_view);
        bottomView.setBackgroundColor(Color.parseColor(color));

        int screenHeight = getResources().getDisplayMetrics().heightPixels; // ارتفاع کل صفحه

        ValueAnimator animator = ValueAnimator.ofFloat(0, screenHeight);
        animator.setDuration(time * 1000L);

        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = bottomView.getLayoutParams();
            layoutParams.height = (int) animatedValue;
            bottomView.setLayoutParams(layoutParams);

            double remainingTime = time - (animation.getCurrentPlayTime() / 1000.0);
            updateTimeText(Math.max(0, remainingTime));

            if (ss)
                if (remainingTime <= 6 && remainingTime > 0 && lastTimeRead > remainingTime) {
                    int remainingInt = (int) Math.ceil(remainingTime);
                    lastTimeRead = remainingInt - 1;
                    playCountdownSound(remainingInt, vs);
                }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) {
                    onComplete.run();
                    lastTimeRead = 1000;
                }
            }
        });

        animator.start();
    }

    public void vibrateWithPatternForcefully(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 300, 1000, 300, 1000, 300, 1000, 300, 1000, 300}; // توقف، لرزش، توقف، لرزش
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1)); // -1 برای عدم تکرار
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    public void vibrateDeviceForcefully(Context context, long durationInMillis) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = VibrationEffect.createOneShot(durationInMillis, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(vibrationEffect);
            } else {
                vibrator.vibrate(durationInMillis);
            }
        }
    }

    private void playCountdownSound(int number, boolean withVibrate) {
        new Thread(() -> {
            if (withVibrate)
                vibrateDeviceForcefully(this, 200);
            switch (number) {
                case 1:
                    soundPool.play(sound1, 1, 1, 0, 0, 1);
                    break;
                case 2:
                    soundPool.play(sound2, 1, 1, 0, 0, 1);
                    break;
                case 3:
                    soundPool.play(sound3, 1, 1, 0, 0, 1);
                    break;
                case 4:
                    soundPool.play(sound4, 1, 1, 0, 0, 1);
                    break;
                case 5:
                    soundPool.play(sound5, 1, 1, 0, 0, 1);
                    break;
            }
        }).start();
    }


    private void updateProgressText(int setNumber, String phase, int totalSets) {
        String text = phase + "  " + setNumber + "/" + totalSets;
        if (totalSets == -1)
            text = phase;

        updateTextWithAnimation(progressTextPhase, text);
        progressTextPhase.setText(text);
    }


    private void updateTimeText(double remainingTime) {
        String formattedTime = formatTime((int) Math.ceil(remainingTime));
        updateTextWithAnimation(progressTextTime, formattedTime);
        progressTextTime.setText(formattedTime);

    }

    public int UsedSecs = 0;

    private void updateTotalTimerText() {
        String formattedTime = formatTime(totalRemainingTime);
        updateTextWithAnimation(totalTimerText, formattedTime);
        totalTimerText.setText(formattedTime);
    }

    private void startTotalTimer() {
        totalTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (totalRemainingTime > 0) {
                    totalRemainingTime--; // کم کردن یک ثانیه
                    updateTotalTimerText(); // به‌روزرسانی تایمر کل
                    UsedSecs++;
                    // اجرای دوباره Runnable بعد از 1 ثانیه
                    totalTimerHandler.postDelayed(this, 1000);
                } else {
                    // وقتی تایمر کل به پایان رسید
                    totalTimerHandler.removeCallbacks(totalTimerRunnable);

                }
            }
        };

        // شروع اجرای Runnable
        totalTimerHandler.post(totalTimerRunnable);
    }

    private void SaveTimes(int usedSecs) {
        timerBox.setVisibility(View.GONE);
        endBox.setVisibility(View.VISIBLE);
        resultTime.setText(formatTime(usedSecs));
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int times = sharedPreferences.getInt("times", 0);
        int secs = sharedPreferences.getInt("secs", 0);

        editor.putInt("secs", secs + usedSecs);
        editor.putInt("times", times + 1);
        editor.apply();

        WorkoutDatabaseHelper dbHelper = new WorkoutDatabaseHelper(this);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String currentDate = df.format(c);
        int userRate = 3;
        dbHelper.addWorkout(currentDate, usedSecs, userRate);

    }

    private void updateTextWithAnimation(TextView textView, String newText) {
        String string = textView.getText().toString();
        if (!string.equals(newText)) {
            textView.setText(newText);
            animateTextView(textView);
        }
    }

    private void animateTextView(TextView textView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.2f, // بزرگ شدن از ۱ به ۱.۲
                1.0f, 1.2f, // بزرگ شدن از ۱ به ۱.۲
                Animation.RELATIVE_TO_SELF, 0.5f, // مرکز افقی
                Animation.RELATIVE_TO_SELF, 0.5f // مرکز عمودی
        );
        scaleAnimation.setDuration(300); // مدت زمان انیمیشن
        scaleAnimation.setRepeatMode(Animation.REVERSE); // بازگشت به حالت اولیه
        scaleAnimation.setRepeatCount(1); // انیمیشن یک بار اجرا شود
        textView.startAnimation(scaleAnimation);
    }


    @SuppressLint("DefaultLocale")
    private String formatTime(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (totalTimerHandler != null && totalTimerRunnable != null) {
            totalTimerHandler.removeCallbacks(totalTimerRunnable);
        }

        if (soundPool != null) {
            soundPool.release();
        }
    }

}