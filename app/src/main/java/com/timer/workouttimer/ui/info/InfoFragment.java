package com.timer.workouttimer.ui.info;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.timer.workouttimer.R;
import com.timer.workouttimer.databinding.FragmentInfoBinding;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.gms.tasks.Task;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(InfoViewModel.class);

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView tv = root.findViewById(R.id.text_info);
        tv.setText(R.string.info);

        Button rateButton = root.findViewById(R.id.rate_button);
        rateButton.setOnClickListener(v -> showRateDialog(1));

        Button donateButton = root.findViewById(R.id.donate_button);
        donateButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/donate/?hosted_button_id=PA6MECK4HZXYJ"));
            startActivity(browserIntent);
        });

        Button feedback_button = root.findViewById(R.id.feedback_button);
        feedback_button.setOnClickListener(v -> {
            String recipient = "axbot2@gmail.com";
            String subject = "Workout Timer Feedback";

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

            try {
                startActivity(Intent.createChooser(emailIntent, "Choose an Email client:"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "No email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    public void showRateDialog(int type) {

        if (type == 1) {
            Activity activity = getActivity();
            if (activity != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName())));
                }
            }
        } else {
            ReviewManager reviewManager = ReviewManagerFactory.create(requireContext());

            Task<ReviewInfo> request = reviewManager.requestReviewFlow();

            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo);
                    flow.addOnCompleteListener(flowTask -> Log.d("RateDialog", "Rate dialog completed."));
                } else {
                    Log.e("RateDialog", "Error: " + task.getException());
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}