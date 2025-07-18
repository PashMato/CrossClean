package com.example.mygame.froger;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygame.R;
import com.example.mygame.game_engine.CameraOH;
import com.example.mygame.game_engine.GameObject;
import com.example.mygame.game_engine.Math.Vectors;
import com.example.mygame.game_engine.Model3D;

public class MainActivity extends AppCompatActivity{
    GLSurfaceView glView;
    Scene scene;

    Compass compass;

    Button Step;
    TextView ScoreText;

    private int score;
    boolean isDead;

    Player robot;

    float a = 6;
    float angle = 0;

    @SuppressLint("SetTextI18n")
    private void setScore(int scoreValue) {
        this.score = scoreValue;
        ScoreText.setText(getString(R.string.score) + " " + scoreValue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ScoreText = findViewById(R.id.score_text);
        Step = findViewById(R.id.step);
        Step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (robot.requestMove()) {
                    setScore(score + 1);
                    robot.scale[1] = 1;
                }
            }
        });

        // Reset the game start settings
        setScore(score);
        isDead = false;

        CameraOH cam = new CameraOH(new float[] {0, 1.5f, 5.5f}, new float[] {0f, 0f, 0f}, this, 200);
        cam.addListener(this::onCameraUpdate);
        cam.rotation[0] = -40;

        // Handle OpenGL stuff
        glView = findViewById(R.id.glView);
        glView.setEGLContextClientVersion(2); // OpenGL ES 2.0
        glView.setRenderer(cam);

        // World generation
        scene = new Scene(this);
        cam.addListener(scene::UpdateRoad);

        robot = new Player(new float[] {0f, 0f, -1f},
                Model3D.loadModelById(this, R.raw.i_robot, R.raw.i_robot_texture),
                scene.laneWidth);

        compass = new Compass(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.start();
    }

    private void onCameraUpdate(GameObject g) {
        angle += a * GameObject.getDt();
        Vectors.lookAt(g.position, g.rotation, robot.position, angle, 1.5f);

        if (robot.position[2] < 0 && scene.startPos >= 0) {
            robot.position[2] = scene.startPos;
        }
    }
}
