package com.example.cross_clean.ui;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cross_clean.R;
import com.example.cross_clean.cross_clean.Scene;
import com.example.cross_clean.game_engine.CameraOH;
import com.example.cross_clean.game_engine.GameObject;
import com.example.cross_clean.game_engine.Math.Vectors;
import com.example.cross_clean.ui.records_table.RecordsTable;

public class MainManu extends AppCompatActivity {
    Button playBN;
    Button recordsBN;
    Button howToBN;
    ImageView settings;
    GameSettings gameSettings;


    ///  OpenGL background ///
    GLSurfaceView glView;
    CameraOH cam;
    Scene scene;

    float[] lookAtPos = new float[3];
    float a = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_manu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playBN = findViewById(R.id.play_bn);
        recordsBN = findViewById(R.id.records_bn);
        howToBN = findViewById(R.id.how_to_bn);

        settings = findViewById(R.id.setting_button);
        gameSettings = findViewById(R.id.setting);

        playBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent game = new Intent(MainManu.this, LoadingActivity.class);
                startActivity(game);
                overridePendingTransition(0, 0); // disables transition animation
                finish();
            }
        });

        recordsBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recordsTable = new Intent(MainManu.this, RecordsTable.class);
                startActivity(recordsTable);
            }
        });

        howToBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: show how to play the game
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameSettings.show();
            }
        });

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
        scene.settings.baseCarSpawnInterval = 6;
    }

    private void onCameraUpdate(GameObject g) {
        Vectors.lookAt(g.position, g.rotation, lookAtPos, 90 - CameraOH.cameraRot[1] + a * CameraOH.getDt(), 1.5f);
    }
}
