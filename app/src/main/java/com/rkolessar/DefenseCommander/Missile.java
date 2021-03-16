package com.rkolessar.DefenseCommander;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

public class Missile {

    private final long screenTime;
    private final int screenWidth;
    private final int screenHeight;
    private final MainActivity mainActivity;
    private final ImageView missileImageView;
    private final AnimatorSet animatorSet = new AnimatorSet();
    private boolean isHit = false;

    public Missile(long screenTime, MainActivity mainActivity) {
        this.screenTime = screenTime;
        this.screenWidth = mainActivity.getScreenWidth();
        this.screenHeight = mainActivity.getScreenHeight();
        this.mainActivity = mainActivity;

        missileImageView = new ImageView(mainActivity);
        missileImageView.setY(-500);

        mainActivity.runOnUiThread(() -> mainActivity.getLayout().addView(missileImageView));
    }

    public static float calculateAngle(double startX, double startY, double endX, double endY) {
        double angle = Math.toDegrees(Math.atan2(endX - startX, endY - startY));
        angle = angle + Math.ceil(-angle / 360) * 360;
        return (float) (190.0f - angle);
    }

    public AnimatorSet getAnimatorSet() {
        return animatorSet;
    }

    public AnimatorSet createAnimatorSet() {
        Drawable missileDrawable = ContextCompat.getDrawable(mainActivity, R.drawable.missile);
        mainActivity.runOnUiThread(() -> missileImageView.setImageDrawable(missileDrawable));

        int startX = (int) (Math.random() * screenWidth);
        int endX = (int) (Math.random() * screenWidth);
        missileImageView.setRotation(calculateAngle(startX, 0, endX, screenHeight));
        missileImageView.setZ(-5);

        assert missileDrawable != null;
        ObjectAnimator yAnimator = ObjectAnimator.ofFloat(missileImageView, "y", 0.0f, screenHeight - missileDrawable.getIntrinsicHeight());
        yAnimator.setInterpolator(new LinearInterpolator());
        yAnimator.setDuration(screenTime);
        yAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.runOnUiThread(() -> {
                    if (!isHit) {
                        mainActivity.applyMissileBlast(Missile.this);
                    }
                });
            }
        });

        ObjectAnimator xAnimator = ObjectAnimator.ofFloat(missileImageView, "x", startX, endX);
        xAnimator.setInterpolator(new LinearInterpolator());
        xAnimator.setDuration(screenTime);

        animatorSet.playTogether(yAnimator, xAnimator);

        return animatorSet;
    }

    public float getX() {
        return missileImageView.getX() + (missileImageView.getWidth() / 2f);
    }

    public float getY() {
        return missileImageView.getY() + (missileImageView.getHeight() / 2f);
    }

    public void setHit(boolean hit) {
        this.isHit = hit;
    }

    public void missileMiss() {
        animatorSet.cancel();
        ImageView blastImageView = createExplosionImageView();
        mainActivity.getLayout().removeView(missileImageView);
        mainActivity.getLayout().addView(blastImageView);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(blastImageView, "alpha", 0.0f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(blastImageView);
            }
        });
        alpha.start();
    }

    public ImageView getMissileImageView() {
        return missileImageView;
    }

    public void interceptorHitMissile() {
        mainActivity.removeMissile(this);
        animatorSet.cancel();

        SoundPlayer.getInstance().startSound("interceptor_hit_missile");

        ImageView explosionImageView = createExplosionImageView();
        mainActivity.runOnUiThread(() -> mainActivity.getLayout().addView(explosionImageView));

        ObjectAnimator alphaAnimator = createAlphaAnimator(explosionImageView);
        alphaAnimator.start();
    }

    private ObjectAnimator createAlphaAnimator(ImageView explodeImageView) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(explodeImageView, "alpha", 0.0f);
        alpha.setDuration(3000);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(explodeImageView);
            }
        });
        return alpha;
    }

    private ImageView createExplosionImageView() {
        ImageView explosionImageView = new ImageView(mainActivity);
        explosionImageView.setImageResource(R.drawable.explode);
        explosionImageView.setX(missileImageView.getX());
        explosionImageView.setY(missileImageView.getY());
        explosionImageView.setRotation((float) (360.0 * Math.random()));
        explosionImageView.setZ(-15);

        return explosionImageView;
    }

}
