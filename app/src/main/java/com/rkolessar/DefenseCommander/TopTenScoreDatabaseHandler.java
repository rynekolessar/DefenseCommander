package com.rkolessar.DefenseCommander;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class TopTenScoreDatabaseHandler implements Runnable {
    private final MainActivity mainActivity;
    private Integer score = null;

    public TopTenScoreDatabaseHandler(MainActivity mainActivity, Integer score) {
        this.mainActivity = mainActivity;
        this.score = score;
    }

    public TopTenScoreDatabaseHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        try {
            Class.forName(mainActivity.getString(R.string.jdbc_driver));
            Connection connection = DriverManager.getConnection(
                    mainActivity.getString(R.string.db_url),
                    mainActivity.getString(R.string.db_username),
                    mainActivity.getString(R.string.db_password));

            String query = "SELECT * FROM AppScores ORDER BY Score DESC LIMIT 10";
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);
            ArrayList<ScoreEntry> scoreEntries = new ArrayList<>();
            while (resultSet.next()) {
                long dateTimeMillis = resultSet.getLong("DateTime");
                String initials = resultSet.getString("Initials").toUpperCase();
                int score = resultSet.getInt("Score");
                int level = resultSet.getInt("Level");
                scoreEntries.add(new ScoreEntry(dateTimeMillis, initials, score, level));
            }

            if (scoreEntries.size() > 8 && score != null && score > scoreEntries.get(9).getScore()) { // New score in top 10
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.openTopScoreDialogue();
                    }
                });
            } else { // score not high enough to be in top 10
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.startScoreActivity(scoreEntries);
                    }
                });
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
