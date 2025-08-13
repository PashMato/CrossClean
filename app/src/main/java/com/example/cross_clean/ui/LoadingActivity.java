package com.example.cross_clean.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cross_clean.R;
import com.example.cross_clean.cross_clean.CrossCleanGame;

public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // Optional: Set a "loading..." layout with a progress bar or just a black screen
        setContentView(R.layout.activity_loading);

        LoadingView loadingView = findViewById(R.id.loading_view);
        loadingView.start();  // Show loading

        new Thread(() -> {
            // Simulate loading (e.g., textures, assets, etc.)
            preloadGameResources();

            // Now go to the actual game on the UI thread
            new Handler(Looper.getMainLooper()).post(() -> {
                Intent intent = new Intent(LoadingActivity.this, CrossCleanGame.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // optional: remove transition animation
                finish();
            });
        }).start();
    }

    private void preloadGameResources() {
        // If you have textures, files, etc. to prepare — do it here
        // You can also call static methods in CrossCleanGame to prepare shared stuff
    }
}
