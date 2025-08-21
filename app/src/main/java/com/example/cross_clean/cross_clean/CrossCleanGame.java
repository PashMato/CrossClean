package com.example.cross_clean.cross_clean;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cross_clean.R;
import com.example.cross_clean.cross_clean.records.AppDatabase;
import com.example.cross_clean.game_engine.CameraOH;
import com.example.cross_clean.game_engine.GameObject;
import com.example.cross_clean.game_engine.Math.Vectors;
import com.example.cross_clean.game_engine.Model3D;
import com.example.cross_clean.ui.GameOverView;
import com.example.cross_clean.ui.LoadingView;

import java.util.Timer;
import java.util.TimerTask;

public class CrossCleanGame extends AppCompatActivity {
    GLSurfaceView glView;
    CameraOH cam;
    Scene scene;

    Compass compass;

    TextView ScoreText;
    ImageView ManuButton;

    private int score;
    boolean isDead;

    Player robot;

    float a = 10;

    boolean accessibilityMode;

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
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility( // Make the screen full screen
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // load views
        ScoreText = findViewById(R.id.score_text);
        ManuButton = findViewById(R.id.manu_button);

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


        robot = new Player(new float[] {0f, 0f, -1f},
                Model3D.loadModelById(this, R.raw.i_robot, R.raw.i_robot_texture),
                scene.settings.laneWidth);
        robot.onCollisionFunction = this::onPlayerCollision;
        robot.isActive = false;

        String name = getSharedPreferences("my_prefs", MODE_PRIVATE).getString("owner_name", "You");
        robot.easterEgg = name.equals("Pashmato");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() { // Activate the camera & give time for loading
                loadingView.stop();
                robot.isActive = true;
            }
        }, 3000);

        compass = new Compass(this);
        compass.start();

        accessibilityMode = getSharedPreferences("my_prefs", MODE_PRIVATE).
                getBoolean("accessibility_mode", false);


        ManuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robot.onOpenManu(false);
            }
        });
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
    public boolean onTouchEvent(MotionEvent event) { // for accessibility mode
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (accessibilityMode && robot.requestMove()) {
                setScore(score + 1);
            }
        }
        return super.onTouchEvent(event);
    }

    private void onCameraUpdate(GameObject g) {
        if (!accessibilityMode && compass.isJumping && robot.requestMove()) { // if Jumping
            setScore(score + 1);
        }
        if (robot.isDead) { // Camera rotation
            Vectors.lookAt(g.position, g.rotation, robot.position, 90 - CameraOH.cameraRot[1] + a * CameraOH.getDt(), 1.5f);
        } else {
            Vectors.lookAt(g.position, g.rotation, robot.position, compass.orientation, 1.5f);
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
            GameOverView gameOverView = findViewById(R.id.game_over_layout);
            gameOverView.bestRecord = AppDatabase.getInstance(CrossCleanGame.this).recordsDao().getRecordWithMaxScore();

            gameOverView.onDeleteFunction = CrossCleanGame.this::onDelete;

            gameOverView.show(score);
    }
}
