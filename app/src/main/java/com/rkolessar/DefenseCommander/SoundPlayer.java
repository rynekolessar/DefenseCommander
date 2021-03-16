package com.rkolessar.DefenseCommander;

import android.content.Context;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.HashSet;

public class SoundPlayer {

    private static SoundPlayer instance;
    private final SoundPool soundPool;
    private static final int MAX_STREAMS = 10;
    static int doneCount = 0;
    static int loadCount = 0;

    private final HashSet<Integer> loaded = new HashSet<>();
    private final HashSet<String> soundsToLoop = new HashSet<>();

    private final HashMap<String, Integer> soundNameToSoundId = new HashMap<>();
    private final HashMap<String, Integer> soundNameToStreamId = new HashMap<>();

    private SoundPlayer() {
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(MAX_STREAMS);

        soundPool = builder.build();
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            loaded.add(sampleId);
            doneCount++;
        });
    }

    static SoundPlayer getInstance() {
        if (instance == null) {
            instance = new SoundPlayer();
        }
        return instance;
    }

    void setupSound(Context context, String soundName, int resource) {
        setupSound(context, soundName, resource, false);
    }

    void setupLoopingSound(Context context, String soundName, int resource) {
        setupSound(context, soundName, resource, true);
    }

    private void setupSound(Context context, String soundName, int resource, boolean loop) {
        int soundId = soundPool.load(context, resource, 1);
        soundNameToSoundId.put(soundName, soundId);
        if (loop) {
            soundsToLoop.add(soundName);
        }
        loadCount++;
    }

    void startSound(String soundName) {
        Integer soundId = soundNameToSoundId.get(soundName);
        if (soundId == null || !loaded.contains(soundId)) {
            return;
        }

        int streamId = soundPool.play(soundId, 1f, 1f, 1, soundsToLoop.contains(soundName) ? -1 : 0, 1f);
        soundNameToStreamId.put(soundName, streamId);
    }

    void stopSound(String soundName) {
        Integer streamId = soundNameToStreamId.get(soundName);
        if (streamId != null) {
            soundPool.stop(streamId);
        }
    }

    void stopAllSound() {
        for (Integer soundId : loaded) {
            soundPool.stop(soundId);
        }
    }
}
