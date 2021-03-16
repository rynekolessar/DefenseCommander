package com.rkolessar.DefenseCommander;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CloudScroller implements Runnable {

    private final Context context;
    private final ViewGroup layout;
    private final long duration;
    private final int screenWidth;
    private final int screenHeight;
    private boolean isRunning;

    private ImageView cloudsImageViewA;
    private ImageView cloudsImageViewB;

    public CloudScroller(Context context, ViewGroup layout, long duration, int screenWidth, int screenHeight) {
        this.context = context;
        this.layout = layout;
        this.duration = duration;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        setupBackground();
    }

    private void setupBackground() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth + getBarHeight(), screenHeight);

        cloudsImageViewA = new ImageView(context);
        cloudsImageViewB = new ImageView(context);

        // Extra Credit 3: Clouds fade in and out from 0.95 to 0.25 repeatedly
        final ObjectAnimator alpha1 = ObjectAnimator.ofFloat(cloudsImageViewA, "alpha", 0.25f, 0.95f);
        alpha1.setInterpolator(new LinearInterpolator());
        alpha1.setRepeatCount(ValueAnimator.INFINITE);
        alpha1.setRepeatMode(ValueAnimator.REVERSE);
        alpha1.setDuration(4000);
        alpha1.start();

        final ObjectAnimator alpha2 = ObjectAnimator.ofFloat(cloudsImageViewB, "alpha", 0.25f, 0.95f);
        alpha2.setInterpolator(new LinearInterpolator());
        alpha2.setRepeatCount(ValueAnimator.INFINITE);
        alpha2.setRepeatMode(ValueAnimator.REVERSE);
        alpha2.setDuration(4000);
        alpha2.start();

        cloudsImageViewA.setLayoutParams(layoutParams);
        cloudsImageViewB.setLayoutParams(layoutParams);

        cloudsImageViewA.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.clouds));
        cloudsImageViewB.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.clouds));

        cloudsImageViewA.setScaleType(ImageView.ScaleType.FIT_XY);
        cloudsImageViewB.setScaleType(ImageView.ScaleType.FIT_XY);

        cloudsImageViewA.setZ(-1);
        cloudsImageViewB.setZ(-1);

        cloudsImageViewA.setAlpha(0.5f);
        cloudsImageViewB.setAlpha(0.5f);

        layout.addView(cloudsImageViewA);
        layout.addView(cloudsImageViewB);

        animate();
    }

    private void animate() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(duration);

        valueAnimator.addUpdateListener((animation) -> {
            final float progress = (float) animation.getAnimatedValue();
            float width = screenWidth + getBarHeight();

            float translationXA = width * progress;
            float translationXB = width * progress - width;

            cloudsImageViewA.setTranslationX(translationXA);
            cloudsImageViewB.setTranslationX(translationXB);
        });

        valueAnimator.start();
    }

    private int getBarHeight() {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }


    @Override
    public void run() {
        cloudsImageViewA.setX(0);
        cloudsImageViewB.setX(-(screenWidth + getBarHeight()));
        long cycleTime = 25;
        long cycles = duration / cycleTime;
        long distance = (screenWidth + getBarHeight()) / cycles;

        while (isRunning) {
            long start = System.currentTimeMillis();
            cloudsImageViewA.setX(cloudsImageViewA.getX() - distance);
            cloudsImageViewB.setX(cloudsImageViewB.getX() - distance);
            long workTime = System.currentTimeMillis() - start;

            if (cloudsImageViewA.getWidth() < -(screenWidth + getBarHeight())) {
                cloudsImageViewA.setX(screenWidth + getBarHeight());
            }
            if (cloudsImageViewB.getWidth() < -(screenWidth + getBarHeight())) {
                cloudsImageViewB.setX(screenWidth + getBarHeight());
            }

            long sleepTime = cycleTime - workTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        isRunning = false;
    }
}
