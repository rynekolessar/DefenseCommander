package com.rkolessar.DefenseCommander;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ScoreActivity extends AppCompatActivity {

    TextView topTenScoresTextView;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToFullscreen();
        setContentView(R.layout.activity_score);

        ArrayList<ScoreEntry> scoreEntries;
        scoreEntries = (ArrayList<ScoreEntry>) getIntent().getExtras().getSerializable("scoreEntries");

        topTenScoresTextView = findViewById(R.id.scoreEntries);

        StringBuilder stringBuilder = new StringBuilder(getString(R.string.scorecard_title) + "\n");
        int rank = 1;
        for (ScoreEntry scoreEntry : scoreEntries) {
            if (rank < 10) {
                stringBuilder.append(" ").append(rank);
            } else {
                stringBuilder.append(rank);
            }
            stringBuilder.append("\t\t\t")
                    .append(scoreEntry.getInitials()).append("\t\t\t\t")
                    .append(scoreEntry.getLevel()).append("\t\t\t\t")
                    .append(scoreEntry.getScore()).append("\t\t\t\t")
                    .append(simpleDateFormat.format(new Date(scoreEntry.getTime())))
                    .append("\n");
            rank++;
        }

        topTenScoresTextView.setText(stringBuilder.toString());
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

    public void onExitButtonClick(View view) {
        finish();
    }
}