package com.rkolessar.DefenseCommander;

import java.io.Serializable;

public class ScoreEntry implements Serializable {

    private final long time;
    private final String initials;
    private final int score;
    private final int level;

    public ScoreEntry(long time, String initials, int score, int level) {
        this.time = time;
        this.initials = initials;
        this.score = score;
        this.level = level;
    }

    public long getTime() {
        return time;
    }

    public String getInitials() {
        return initials;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }
}
