package com.example.mygame;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygame.game_engine.CameraOH;
import com.example.mygame.game_engine.GameObject;
import com.example.mygame.game_engine.Math.Vectors;
import com.example.mygame.game_engine.Model3D;
import com.example.mygame.game_engine.ObjectTypes;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private GLSurfaceView glView;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] rotation = new float[] {0, 0, 0};
    private GameObject robot;

    private Scene scene;
    private float a = 0;

    private float[] gravity;
    private float[] geomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CameraOH cam = new CameraOH(new float[] {0, 2f, 0}, new float[] {0f, 0f, 5f}, this);
        cam.addListener(this::onCameraUpdate);
        cam.rotation[0] = -15;


        glView = new GLSurfaceView(this);
        glView.setEGLContextClientVersion(2); // OpenGL ES 2.0
        glView.setRenderer(cam);
        setContentView(glView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);

        scene = new Scene(this, cam);
        cam.addListener(scene::UpdateRoad);

//        robot = new GameObject(new float[] {0f, 0f, 0f}, new float[] {0f, 0f}, scene.laneModel, ObjectTypes.Background);
//        robot.rotation[0] = -90;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;

        if (gravity != null && geomagnetic != null) {

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                rotation[1] = (float) Math.toDegrees(orientation[0]); // angle around z-axis
                rotation[0] = (float) Math.toDegrees(orientation[1]); // angle around z-axis
                rotation[2] = (float) Math.toDegrees(orientation[2]); // angle around z-axis
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used here
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void onCameraUpdate(GameObject g) {
//        Vectors.lookAt(g.position, g.rotation, robot.position, 90, 4);
//        g.rotation[0] = (rotation[0] + 90);
    }
}
