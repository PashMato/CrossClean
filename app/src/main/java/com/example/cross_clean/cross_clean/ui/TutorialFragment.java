package com.example.cross_clean.cross_clean.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.cross_clean.R;

/**
 * this shows the tutorial automatically
 */
public class TutorialFragment extends FrameLayout {
    Context context;

    public TutorialFragment(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    public TutorialFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    /**
     *  this method is called from the constructor to set the base parameters
     * @param context the app's context
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.tutorial_fragment, this, true);

        showTutorial();
    }

    private void showTutorial() {
        // Load al of the necessary views
        View overlay = findViewById(R.id.tutorial_overlay);
        View container = findViewById(R.id.tutorial_text_container);
        TextView text = findViewById(R.id.tutorial_text);
        Button next = findViewById(R.id.next_button);

        // hides the tutorial if needs to
        if (!context.getSharedPreferences("my_prefs", MODE_PRIVATE).getBoolean("show_tutorials", true)) {
            post(() -> {
                overlay.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
            });
            return;
        }

        post(() -> {
            overlay.setVisibility(View.VISIBLE);
            container.setVisibility(View.VISIBLE);
        });

        // Read the tutorial's steps according to accessibility mode
        String[] steps = getResources().getStringArray(
                context.getSharedPreferences("my_prefs", MODE_PRIVATE).
                getBoolean("accessibility_mode", false)
                ? R.array.tutorial_steps_acc : R.array.tutorial_steps);


        final int[] currentStep = {0};
        text.setText(steps[currentStep[0]]);

        next.setOnClickListener(v -> {
            currentStep[0]++;
            if (currentStep[0] < steps.length) {
                text.setText(steps[currentStep[0]]);
            } else {
                post(() -> {
                    overlay.setVisibility(View.GONE);
                    container.setVisibility(View.GONE);
                });

                // Mark tutorial as completed
                SharedPreferences.Editor editor = context.getSharedPreferences("my_prefs", MODE_PRIVATE).edit();
                editor.putBoolean("show_tutorials", false);
                editor.apply();
            }
        });
    }
}
