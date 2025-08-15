package com.example.cross_clean.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.cross_clean.R;
import com.example.cross_clean.cross_clean.OnDeleteFunction;
import com.example.cross_clean.cross_clean.records.AppDatabase;
import com.example.cross_clean.cross_clean.records.Record;

import java.util.Date;

public class GameOverView extends FrameLayout {

    public Record bestRecord = null;
    private Context context = null;
    private TextView score;
    private TextView bestScore;

    public OnDeleteFunction onDeleteFunction;

    public GameOverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameOverView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.game_over_view, this, true);

        score = findViewById(R.id.score_text);
        bestScore = findViewById(R.id.best_score_text);

        Button restart = findViewById(R.id.restart_button);
        Button mainManu = findViewById(R.id.main_manu_button);

        restart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, LoadingActivity.class));

                if (context instanceof Activity) {
                    onDeleteFunction.OnDelete();
                    ((Activity) context).finish();
                }
            }
        });

        mainManu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MainManu.class));

                if (context instanceof Activity) {
                    onDeleteFunction.OnDelete();
                    ((Activity) context).finish();
                }
            }
        });

        this.context = context;

        post(() -> setVisibility(View.GONE));
    }

    @SuppressLint("SetTextI18n")
    public void show(int _score) {
        post(() -> {
            setVisibility(View.VISIBLE);

            if (score != null && bestScore != null) {
                bestScore.setText(context.getString(R.string.best_score) + " " + (bestRecord == null ? 0 : bestRecord.score));
                score.setText(context.getString(R.string.score) + " " + _score);

                SharedPreferences prefs = context.getSharedPreferences("my_prefs",
                        Context.MODE_PRIVATE);

                if (_score == 0) {
                    AppDatabase.getInstance(context).recordsDao()
                            .insert(new Record(prefs.getString("owner_name", "You"), _score, new Date()));
                }
            }
        });
    }
}
