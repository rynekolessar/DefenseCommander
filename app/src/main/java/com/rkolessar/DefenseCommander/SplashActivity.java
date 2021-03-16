package com.rkolessar.DefenseCommander;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 6000;
    ImageView titleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToFullscreen();
        setContentView(R.layout.activity_splash_screen);

        SoundPlayer.getInstance().setupLoopingSound(this, "background", R.raw.background);
        SoundPlayer.getInstance().setupSound(this, "interceptor_blast", R.raw.interceptor_blast);
        SoundPlayer.getInstance().setupSound(this, "interceptor_hit_missile", R.raw.interceptor_hit_missile);
        SoundPlayer.getInstance().setupSound(this, "launch_interceptor", R.raw.launch_interceptor);
        SoundPlayer.getInstance().setupSound(this, "launch_missile", R.raw.launch_missile);
        SoundPlayer.getInstance().setupSound(this, "missile_miss", R.raw.missile_miss);
        SoundPlayer.getInstance().setupSound(this, "base_blast", R.raw.base_blast);

        titleImageView = findViewById(R.id.splashTitle);
        fadeInTitle();

        new Handler().postDelayed(() -> SoundPlayer.getInstance().startSound("background"), 2000);

        new Handler().postDelayed(this::openMainActivity, SPLASH_TIME_OUT);
    }

    private void setToFullscreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void fadeInTitle() {
        ObjectAnimator aAnim = ObjectAnimator.ofFloat(titleImageView, "alpha", 0, 1);
        aAnim.setDuration(5500);
        aAnim.start();
    }

    private void openMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}