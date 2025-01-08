package com.example.workouttimer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TimerActivity extends AppCompatActivity {

    private TextView progressTextTime; // TextView برای نمایش متن زمان و ست
    private TextView progressTextPhase; // TextView برای نمایش متن زمان و ست
    private int totalSets; // تعداد کل ست‌ها (برای نمایش شماره ست‌ها)
    private TextView totalTimerText; // TextView برای تایمر کلی
    private ImageButton closeBtn;
    private int totalRemainingTime; // زمان کل باقی‌مانده به ثانیه
    private Handler totalTimerHandler = new Handler();
    private Runnable totalTimerRunnable;
    private SoundPool soundPool;
    private int sound1, sound2, sound3, sound4, sound5; // شناسه‌های صداها
    private int lastTimeRead = 1000; // برای جلوگیری از پخش چندباره
    boolean isFirst = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_timer);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .build();

        sound1 = soundPool.load(this, R.raw.number_1, 1);
        sound2 = soundPool.load(this, R.raw.number_2, 1);
        sound3 = soundPool.load(this, R.raw.number_3, 1);
        sound4 = soundPool.load(this, R.raw.number_4, 1);
        sound5 = soundPool.load(this, R.raw.number_5, 1);


        // دسترسی به TextView
        progressTextPhase = findViewById(R.id.progress_text_phase);
        progressTextTime = findViewById(R.id.progress_text_time);
        totalTimerText = findViewById(R.id.total_timer);
        closeBtn = findViewById(R.id.close_button);

        closeBtn.setOnClickListener(v -> {
            finish();
        });

        // دریافت داده‌های Intent
        totalSets = getIntent().getIntExtra("sets", 2); // تعداد کل ست‌ها
        int work = getIntent().getIntExtra("work", 30); // مدت زمان work
        int rest = getIntent().getIntExtra("rest", 15); // مدت زمان rest
        boolean skip = getIntent().getBooleanExtra("skip", true); // آیا استراحت آخر باید رد شود


        int prepareTime = 5; // زمان آماده‌سازی
        totalRemainingTime = (work + rest) * totalSets + prepareTime - (skip ? rest : 0);

        // شروع تایمر کل
        startTotalTimer();

        // اجرای انیمیشن‌ها
        executeSet(totalSets, work, rest, skip);
    }

    private void executeSet(int sets, int workTime, int restTime, boolean skipLastRest) {
        if (sets <= 0) return; // اگر تعداد ست‌ها صفر باشد، کار متوقف می‌شود.

        int currentSetNumber = totalSets - sets + 1; // محاسبه شماره ست از 1 شروع شود

        // نمایش Work
        if (isFirst)
            updateProgressText(currentSetNumber, "Prepare", -1);
        executeProgress(isFirst ? 5 : 0, "#696969", () -> {

            updateProgressText(currentSetNumber, "Work", totalSets);
            executeProgress(isFirst ? workTime - 1 : workTime, "#008080", () -> { // اجرای Work
                if (sets > 1 || !skipLastRest) { // نمایش Rest
                    isFirst = false;
                    updateProgressText(currentSetNumber, "Rest", skipLastRest ? totalSets - 1 : totalSets);
                    executeProgress(restTime, "#1E90FF", () -> {
                        executeSet(sets - 1, workTime, restTime, skipLastRest); // اجرای ست بعدی
                    });
                } else {
                    this.finish();
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

            if (remainingTime <= 6 && remainingTime > 0 && lastTimeRead > remainingTime) {
                int remainingInt = (int) Math.ceil(remainingTime);
                lastTimeRead = remainingInt - 1;
                playCountdownSound(remainingInt);
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


    private void playCountdownSound(int number) {
        new Thread(() -> {
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