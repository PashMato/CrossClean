package com.example.cross_clean.cross_clean;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Compass implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor rotationSensor;

    public float azimuth = 0f; // Current smoothed azimuth
    private float lastAzimuth = 0f;
    private static final float ALPHA = 0.15f; // Smoothing factor (lower = smoother)

    public Compass(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void start() {
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    protected void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            float rawAzimuth = (float) Math.toDegrees(orientation[0]);
            if (rawAzimuth < 0) rawAzimuth += 360;

            // Apply low-pass filter
            azimuth = lowPassFilter(rawAzimuth, azimuth);

            if (Math.abs(azimuth - lastAzimuth) > 1.0f) {
                lastAzimuth = azimuth;
                Log.d("Compass", "Azimuth: " + azimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private float lowPassFilter(float newValue, float oldValue) {
        float delta = ((newValue - oldValue + 540) % 360) - 180;
        return (oldValue + ALPHA * delta + 360) % 360;
    }
}
