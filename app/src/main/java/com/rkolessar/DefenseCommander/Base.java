package com.rkolessar.DefenseCommander;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

public class Base {

    private final ImageView baseImageView;
    private final MainActivity mainActivity;

    public Base(ImageView baseImageView, MainActivity mainActivity) {
        this.baseImageView = baseImageView;
        this.mainActivity = mainActivity;
    }

    public float getX() {
        return baseImageView.getX() + (0.5f * baseImageView.getWidth());
    }

    public float getY() {
        return baseImageView.getY() + (0.5f * baseImageView.getHeight());
    }

    public void destruct() {
        SoundPlayer.getInstance().startSound("base_blast");

        Drawable baseBlastDrawable = ContextCompat.getDrawable(mainActivity, R.drawable.blast);

        ImageView blastImageView = new ImageView(mainActivity);
        blastImageView.setImageDrawable(baseBlastDrawable);
        assert baseBlastDrawable != null;
        blastImageView.setX(getX() - (baseBlastDrawable.getIntrinsicWidth() / 2f));
        blastImageView.setY(getY() - (baseBlastDrawable.getIntrinsicHeight() / 2f));
        blastImageView.setRotation((float) (360.0 * Math.random()));

        mainActivity.getLayout().removeView(baseImageView);
        mainActivity.getLayout().addView(blastImageView);

        ObjectAnimator blastAlphaAnimator = ObjectAnimator.ofFloat(blastImageView, "alpha", 0.0f);
        blastAlphaAnimator.setInterpolator(new LinearInterpolator());
        blastAlphaAnimator.setDuration(3000);
        blastAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(blastImageView);
            }
        });
        blastAlphaAnimator.start();
    }
}
