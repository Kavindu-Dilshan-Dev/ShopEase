package com.kavindu.shopeaseapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.kavindu.shopeaseapp.databinding.ActivitySensorsBinding;
import com.kavindu.shopeaseapp.utils.SensorHelper;
import com.kavindu.shopeaseapp.utils.TelephonyHelper;

import java.util.List;

public class SensorsActivity extends AppCompatActivity implements SensorHelper.SensorCallback {

    private ActivitySensorsBinding binding;
    private SensorHelper sensorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySensorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sensorHelper = new SensorHelper(this, this);

        // List all available sensors
        List<String> sensors = sensorHelper.getAvailableSensors();
        StringBuilder sb = new StringBuilder();
        for (String s : sensors) sb.append("• ").append(s).append("\n");
        binding.tvSensorList.setText(sb.toString());

        // Telephony
        binding.tvNetworkOperator.setText("Network: " +
                TelephonyHelper.getNetworkOperator(this));
        binding.btnCallSupport.setOnClickListener(v -> TelephonyHelper.dialSupport(this));
        binding.btnSmsSupport.setOnClickListener(v ->
                TelephonyHelper.sendSms(this, "Hello, I need help with my order."));

        // Advanced: Chip group filter
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Handle chip selection for product filtering
            Toast.makeText(this, checkedIds.size() + " filter(s) selected",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override protected void onResume()  { super.onResume();  sensorHelper.register();   }
    @Override protected void onPause()   { super.onPause();   sensorHelper.unregister(); }

    @Override
    public void onShakeDetected() {
        runOnUiThread(() -> {
            binding.tvSensorStatus.setText("📳 SHAKE DETECTED!");
            Toast.makeText(this, "Shake detected! Refreshing...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStepDetected(int steps) {
        runOnUiThread(() -> binding.tvSteps.setText("👟 Steps: " + steps));
    }

    @Override
    public void onLightChanged(float lux) {
        runOnUiThread(() -> binding.tvLight.setText(
                String.format("💡 Light: %.1f lux", lux)));
    }

    @Override
    public void onOrientationChanged(float x, float y, float z) {
        runOnUiThread(() -> binding.tvAccel.setText(
                String.format("📐 X:%.2f  Y:%.2f  Z:%.2f", x, y, z)));
    }
}