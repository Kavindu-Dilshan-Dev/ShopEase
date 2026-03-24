package com.kavindu.shopeaseapp.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.kavindu.shopeaseapp.databinding.ActivitySplashBinding;
import com.kavindu.shopeaseapp.utils.PrefsManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startAnimations();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PrefsManager prefs = PrefsManager.getInstance(this);
            Intent intent = prefs.isLoggedIn()
                    ? new Intent(this, MainActivity.class)
                    : new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }

    private void startAnimations() {

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(
                binding.ivLogo, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(
                binding.ivLogo, "scaleY", 0f, 1f);
        scaleX.setDuration(900);
        scaleY.setDuration(900);
        scaleX.setInterpolator(new BounceInterpolator());
        scaleY.setInterpolator(new BounceInterpolator());


        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(
                binding.tvAppName, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setStartDelay(600);


        ObjectAnimator slideUp = ObjectAnimator.ofFloat(
                binding.tvTagline, "translationY", 80f, 0f);
        ObjectAnimator tagFade = ObjectAnimator.ofFloat(
                binding.tvTagline, "alpha", 0f, 1f);
        slideUp.setDuration(800);
        tagFade.setDuration(800);
        slideUp.setStartDelay(800);
        tagFade.setStartDelay(800);
        slideUp.setInterpolator(new OvershootInterpolator());


        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, fadeIn, slideUp, tagFade);
        set.start();
    }
}