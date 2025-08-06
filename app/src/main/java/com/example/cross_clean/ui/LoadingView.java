package com.example.cross_clean.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cross_clean.R;

public class LoadingView extends FrameLayout {

    private Drawable drawable;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadingView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.loading_fragment, this, true);
        ImageView loadingImage = findViewById(R.id.loading_ani);
        drawable = loadingImage.getDrawable();

        start();
    }

    public void start() {
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) drawable;
            animation.start();

            post(() -> {
                setVisibility(View.VISIBLE);
            });
        }
    }

    public void stop() {
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) drawable;
            animation.stop();
            post(() -> {
                setVisibility(View.GONE);
            });
        }
    }
}
