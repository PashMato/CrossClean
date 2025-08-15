package com.example.cross_clean.cross_clean;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Compass implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor rotation;
    private final Sensor accelometer;

    private float azimuth = 0f; // Current smoothed azimuth
    private float lastAzimuth = 0f;
    private float firstAzimuth = Float.NaN;
    public float orientation = 0f;
    private static final float ALPHA = 0.15f; // Smoothing factor (lower = smoother)

    public boolean isJumping;
    private final float accuracyError = 9;

    public Compass(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void start() {
        if (rotation != null)
            sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);

        if (accelometer != null) {
            sensorManager.registerListener(this, accelometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    protected void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            readAzimuth(event);
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            readJump(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void readJump(SensorEvent event) {
        isJumping = getLengthSquare(event.values[0], event.values[1], event.values[2]) < 9.81 * 9.81 - accuracyError * accuracyError;
    }

    private void readAzimuth(SensorEvent event) {
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

        if (Float.isNaN(firstAzimuth)) {
            firstAzimuth = azimuth;
        }

        this.orientation = azimuth - firstAzimuth;
    }
    private float lowPassFilter(float newValue, float oldValue) {
        float delta = ((newValue - oldValue + 540) % 360) - 180;
        return (oldValue + ALPHA * delta + 360) % 360;
    }
    
    private float getLengthSquare(float x, float y, float z) {
        return x * x + y * y + z * z;
    }
}
