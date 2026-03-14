package com.kavindu.shopeaseapp.utils;

import android.content.Context;
import android.hardware.*;
import java.util.ArrayList;
import java.util.List;

public class SensorHelper implements SensorEventListener {

    public interface SensorCallback {
        void onShakeDetected();
        void onStepDetected(int steps);
        void onLightChanged(float lux);
        void onOrientationChanged(float azimuth, float pitch, float roll);
    }

    private final SensorManager sensorManager;
    private final SensorCallback callback;
    private Sensor accelerometer, stepCounter, lightSensor, gyroscope;

    private float lastX, lastY, lastZ;
    private long lastShakeTime;
    private static final float SHAKE_THRESHOLD = 12f;
    private static final int SHAKE_SLOP = 500;
    private int totalSteps = 0;

    public SensorHelper(Context context, SensorCallback callback) {
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepCounter   = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        lightSensor   = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        gyroscope     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void register() {
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (stepCounter != null)
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
        if (lightSensor != null)
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
        if (gyroscope != null)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    public List<String> getAvailableSensors() {
        List<String> list = new ArrayList<>();
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensors) list.add(s.getName() + " [" + s.getVendor() + "]");
        return list;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0], y = event.values[1], z = event.values[2];
                float delta = Math.abs(x - lastX) + Math.abs(y - lastY) + Math.abs(z - lastZ);
                if (delta > SHAKE_THRESHOLD) {
                    long now = System.currentTimeMillis();
                    if (now - lastShakeTime > SHAKE_SLOP) {
                        lastShakeTime = now;
                        if (callback != null) callback.onShakeDetected();
                    }
                }
                lastX = x; lastY = y; lastZ = z;
                if (callback != null) callback.onOrientationChanged(x, y, z);
                break;

            case Sensor.TYPE_STEP_COUNTER:
                totalSteps = (int) event.values[0];
                if (callback != null) callback.onStepDetected(totalSteps);
                break;

            case Sensor.TYPE_LIGHT:
                if (callback != null) callback.onLightChanged(event.values[0]);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}