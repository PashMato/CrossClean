package com.example.cross_clean.cross_clean;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cross_clean.R;
import com.example.cross_clean.cross_clean.records.AppDatabase;
import com.example.cross_clean.cross_clean.records.Record;
import com.example.cross_clean.game_engine.CameraOH;
import com.example.cross_clean.game_engine.GameObject;
import com.example.cross_clean.game_engine.Math.Vectors;
import com.example.cross_clean.game_engine.Model3D;
import com.example.cross_clean.ui.GameOverView;
import com.example.cross_clean.ui.LoadingActivity;
import com.example.cross_clean.ui.LoadingView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CrossCleanGame extends AppCompatActivity {
    GLSurfaceView glView;
    CameraOH cam;
    Scene scene;

    Compass compass;

    Button Step;
    TextView ScoreText;

    private int score;
    boolean isDead;

    Player robot;

    float a = 10;

    @SuppressLint("SetTextI18n")
    private void setScore(int scoreValue) {
        this.score = scoreValue;
        ScoreText.setText(getString(R.string.score) + " " + scoreValue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cross_clean_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ScoreText = findViewById(R.id.score_text);
        Step = findViewById(R.id.step);
        Step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (robot.requestMove()) {
                    setScore(score + 1);
                }
            }
        });

        LoadingView loadingView = findViewById(R.id.loading_view);
        loadingView.start();  // Show loading

        // Reset the game start settings
        setScore(score);
        isDead = false;

        cam = new CameraOH(new float[3], new float[3], this);

        // Handle OpenGL stuff
        glView = findViewById(R.id.glView);
        glView.setEGLContextClientVersion(3); // OpenGL ES 3.0
        glView.setRenderer(cam);
        cam.position[1] = 1.5f;
        cam.rotation[0] = -40;

        cam.addListener(this::onCameraUpdate);

        // World generation
        scene = new Scene(this);
        cam.addListener(scene::UpdateRoad); // This is here because scene isn't a GameObject

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() { // Activate the camera
                loadingView.stop();
            }
        }, 3000);

        robot = new Player(new float[] {0f, 0f, -1f},
                Model3D.loadModelById(this, R.raw.i_robot, R.raw.i_robot_texture),
                1f); // TODO: set this to real laneWidth
        robot.onCollisionFunction = this::onPlayerCollision;

        compass = new Compass(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
        compass.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();
        compass.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(() -> {
            AppDatabase.getInstance(CrossCleanGame.this)
                    .recordsDao()
                    .insert(new Record("You", -1, new Date()));
        }).start();
    }

    private void onCameraUpdate(GameObject g) {
        if (isDead) {
            Vectors.lookAt(g.position, g.rotation, robot.position, 90 - CameraOH.cameraRot[1] + a * CameraOH.getDt(), 1.5f);
        } else {
            // TODO: implement the compass thing
            Vectors.lookAt(g.position, g.rotation, robot.position, 90 - CameraOH.cameraRot[1] + a * CameraOH.getDt(), 1.5f);
        }

        if (robot.position[2] < 0 && scene.startPos >= 0) {
            robot.position[2] = scene.startPos;
        }
    }

    private void onDelete() {
        GameObject.Delete(robot);
        GameObject.Delete(cam);
    }

    private void onPlayerCollision(GameObject g) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() { // Activate the camera
                GameOverView gameOverView = findViewById(R.id.game_over_layout);
                gameOverView.bestRecord = AppDatabase.getInstance(CrossCleanGame.this).recordsDao().getRecordWithMaxScore();

                gameOverView.onDeleteFunction = CrossCleanGame.this::onDelete;

                gameOverView.show(score);
            }
        }, 1000);
    }
}
