package com.rkolessar.DefenseCommander;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ScoreDatabaseHandler implements Runnable {

    private final MainActivity mainActivity;
    private final ScoreEntry scoreEntry;

    public ScoreDatabaseHandler(MainActivity mainActivity, ScoreEntry scoreEntry) {
        this.mainActivity = mainActivity;
        this.scoreEntry = scoreEntry;
    }

    @Override
    public void run() {
        try {
            Class.forName(mainActivity.getString(R.string.jdbc_driver));
            Connection connection = DriverManager.getConnection(
                    mainActivity.getString(R.string.db_url),
                    mainActivity.getString(R.string.db_username),
                    mainActivity.getString(R.string.db_password));

            String query = "INSERT INTO AppScores VALUES ("
                    + scoreEntry.getTime()
                    + ",'" + scoreEntry.getInitials() + "',"
                    + scoreEntry.getScore() + "," + scoreEntry.getLevel() + ")";

            Statement statement = connection.createStatement();
            statement.executeUpdate(query);

            mainActivity.runOnUiThread(() -> new Thread(new TopTenScoreDatabaseHandler(mainActivity)).start());

        } catch (ClassNotFoundException | SQLException ignored) {
        }

    }
}
