package com.rkolessar.DefenseCommander;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MISSILE_BLAST_RANGE = 250;
    private static final int INTERCEPTOR_BLAST_RANGE = 120;

    static int interceptorsInFlight = 0;

    private int screenWidth;
    private int screenHeight;
    private int score = 0;
    private int level = 1;

    private CloudScroller cloudScroller;

    private final List<Base> baseList = new ArrayList<>();

    private ViewGroup layout;

    private MissileMaker missileMaker;

    private TextView scoreTextView;
    private TextView levelTextView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToFullscreen();
        setContentView(R.layout.activity_main);
        getScreenDimensions();

        scoreTextView = findViewById(R.id.scoreTextView);
        levelTextView = findViewById(R.id.levelTextView);
        levelTextView.setText(getString(R.string.level, level));

        layout = findViewById(R.id.constraintLayout);
        layout.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                handleTouch(motionEvent.getX(), motionEvent.getY());
            }
            return false;
        });

        cloudScroller = new CloudScroller(this, layout, 60000, screenWidth, screenHeight);

        createBases();

        missileMaker = new MissileMaker(this);
        new Thread(missileMaker).start();

        setUpGameLoop();
    }

    /**
     * Starts the game loop
     */
    private void setUpGameLoop() {
        new Thread(() -> {
            while (!baseList.isEmpty()) {
                continue;
            }
            MainActivity.this.runOnUiThread(this::endGame);
        }).start();
    }

    /**
     * Sets the level
     *
     * @param level level number
     */
    public void setLevel(int level) {
        this.level = level;
        levelTextView.setText(getString(R.string.level, level));
    }

    /**
     * Gets the Distance
     *
     * @param startX X coordinate to start
     * @param startY Y coordinate to start
     * @param endX   X coordinate to end
     * @param endY   Y coordinate to end
     * @return distance
     */
    public double getDistance(float startX, float startY, float endX, float endY) {
        return Math.sqrt(Math.pow((endX - startX), 2) + Math.pow((endY - startY), 2));
    }

    /**
     * Applies an interceptor blast at (x,y)
     *
     * @param interceptorX x coordinate for interceptor
     * @param interceptorY y coordinate for interceptor
     */
    public void applyInterceptorBlast(float interceptorX, float interceptorY) {
        List<Missile> hitMissiles = new ArrayList<>();
        for (Missile missile : missileMaker.getActiveMissiles()) {
            double distance = getDistance(missile.getX(), missile.getY(), interceptorX, interceptorY);
            if (distance < INTERCEPTOR_BLAST_RANGE) {
                hitMissiles.add(missile);
                score++;
                scoreTextView.setText(String.valueOf(score));
            }
        }
        Base hitBase = null;
        // Extra Credit 4: Check if interceptors explode near a base, if so the base is destroyed
        for (Base base : baseList) {
            double distance = getDistance(interceptorX, interceptorY, base.getX(), base.getY());
            if (distance < INTERCEPTOR_BLAST_RANGE) {
                base.destruct();
                hitBase = base;
            }
        }
        if (hitBase != null) {
            baseList.remove(hitBase);
        }
        hitMissiles.forEach(missile -> {
            missile.setHit(true);
            missile.interceptorHitMissile();
        });
    }

    /**
     * removes the missile
     *
     * @param missile missile object to remove
     */
    public void removeMissile(Missile missile) {
        layout.removeView(missile.getMissileImageView());
        missileMaker.removeMissile(missile);
    }

    /**
     * Applies a missile blast from the missile object
     *
     * @param missile object to blast
     */
    public void applyMissileBlast(Missile missile) {
        float missileX = missile.getX();
        float missileY = missile.getY();

        Base hitBase = null;
        for (Base base : baseList) {
            double distance = getDistance(missileX, missileY, base.getX(), base.getY());
            if (distance < MISSILE_BLAST_RANGE) {
                base.destruct();
                hitBase = base;
            } else {
                SoundPlayer.getInstance().startSound("missile_miss");
            }
            missile.missileMiss();
            missileMaker.removeMissile(missile);
        }
        if (hitBase != null) {
            baseList.remove(hitBase);
        }
    }

    /**
     * End of the game
     */
    private void endGame() {
        missileMaker.stop();
        for (Missile activeMissile : missileMaker.getActiveMissiles()) {
            layout.removeView(activeMissile.getMissileImageView());
        }
        missileMaker.removeAllMissiles();
        interceptorsInFlight = 0;

        ImageView endGameImageView = findViewById(R.id.gameOver);
        ObjectAnimator aAnim = ObjectAnimator.ofFloat(endGameImageView, "alpha", 0, 1);
        aAnim.setDuration(5500);
        aAnim.start();

        new Handler().postDelayed(this::handleScores, 8500);
    }

    /**
     * Starts a new thread with the top ten score database handler
     */
    private void handleScores() {
        new Thread(new TopTenScoreDatabaseHandler(this, score)).start();
    }

    /**
     * Opens the top score dialog
     */
    public void openTopScoreDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText userInitialsEditText = buildEnterUserInitialsEditText();
        builder.setView(userInitialsEditText);
        builder.setTitle("You are a Top-Player!");
        builder.setMessage("Please enter your initials (up to 3 characters):");
        builder.setPositiveButton("OK", (dialog, which) -> enterScoreInDatabase(userInitialsEditText.getText().toString()));
        builder.setNegativeButton("CANCEL", (dialog, which) -> new Thread(new TopTenScoreDatabaseHandler(this)).start());
        builder.create().show();
    }

    /**
     * adds the users score to the database in a new thread
     *
     * @param userInitials users initials
     */
    private void enterScoreInDatabase(String userInitials) {
        new Thread(new ScoreDatabaseHandler(this,
                new ScoreEntry(System.currentTimeMillis(), userInitials, score, level))).start();
    }

    /**
     * Begins the ScoreActivity
     *
     * @param scoreEntries list of scores
     */
    public void startScoreActivity(ArrayList<ScoreEntry> scoreEntries) {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("scoreEntries", scoreEntries);
        startActivity(intent);
        finish();
    }

    /**
     * @return users initials
     */
    public EditText buildEnterUserInitialsEditText() {
        EditText userInitials = new EditText(this);
        userInitials.setInputType(InputType.TYPE_CLASS_TEXT);
        userInitials.setGravity(Gravity.CENTER_HORIZONTAL);
        userInitials.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(3)
        });

        return userInitials;
    }

    /**
     * @return layout
     */
    public ViewGroup getLayout() {
        return layout;
    }

    /**
     * @return screen width in pixels
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * @return screen height in pixels
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * creates the starting bases
     */
    private void createBases() {
        baseList.add(new Base(findViewById(R.id.leftBaseImageView), this));
        baseList.add(new Base(findViewById(R.id.centerBaseImageView), this));
        baseList.add(new Base(findViewById(R.id.rightBaseImageView), this));
    }

    /**
     * handles touch events
     *
     * @param x x coordinate for touch point
     * @param y y coordinate for touch point
     */
    private void handleTouch(float x, float y) {
        if (baseList.isEmpty()) {
            return;
        }
        Base closestBase = findNearestBaseToTouch(x, y);
        launchInterceptor(x, y, closestBase);
    }

    /**
     * Launches an interceptor towards from the nearest base
     *
     * @param x           x coordinate for interceptor
     * @param y           y coordinate for interceptor
     * @param closestBase closest base to fire from
     */
    private void launchInterceptor(float x, float y, Base closestBase) {
        if (interceptorsInFlight < 3) {
            Interceptor interceptor = new Interceptor(this, closestBase.getX(), closestBase.getY(), x, y);
            interceptor.launch();
        }
    }

    /**
     * Finds the nearest base to the given coordinates
     *
     * @param touchX X coordinate of touch point
     * @param touchY Y coordinate of touch point
     * @return the nearest base
     */
    private Base findNearestBaseToTouch(float touchX, float touchY) {
        Base nearestBase = null;
        double nearestBaseDistance = Double.MAX_VALUE;
        for (Base base : baseList) {
            double distance = getDistance(touchX, touchY, base.getX(), base.getY());
            if (distance < nearestBaseDistance) {
                nearestBaseDistance = distance;
                nearestBase = base;
            }
        }
        return nearestBase;
    }

    /**
     * gets and sets the screen dimensions
     */
    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }

    /**
     * Sets the activity to Fullscreen/ Immersive mode
     */
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

    @Override
    protected void onStop() {
        super.onStop();
        SoundPlayer.getInstance().stopAllSound();
        cloudScroller.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundPlayer.getInstance().stopSound(getString(R.string.background_sound));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SoundPlayer.getInstance().startSound(getString(R.string.background_sound));
    }
}