package com.bit.ui;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class MusicManager {
    private static Clip startMenuClip;
    private static Clip gameClip;
    private static boolean isGameMusic2 = false;
    private static Clip gameOverClip;

    public static void playStartMenuMusic() {
        playLoopMusic("/music/starting.wav", true);

    }

    public static void playGameMusic1() {
        isGameMusic2 = false;
        playLoopMusic("/music/beginning.wav", false);
    }

    public static void playGameMusic2() {
        if(isGameMusic2) return;
        isGameMusic2 = true;
        playLoopMusic("/music/fighting.wav", false);
    }

    public static void playGameOverMusic() {
        new Thread(() -> {
            try {
                stopAll(); // 停止所有正在播放的音乐

                InputStream is = MusicManager.class.getResourceAsStream("/music/ending.wav");
                AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                gameOverClip = AudioSystem.getClip();
                gameOverClip.open(ais);

                gameOverClip.loop(0); // 不循环，只播放一次
                gameOverClip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void playLoopMusic(String path, boolean isMenu) {
        new Thread(() -> {
            try {
                if(gameClip != null && gameClip.isRunning()) gameClip.stop();
                if(startMenuClip != null && startMenuClip.isRunning()) startMenuClip.stop();

                InputStream is = MusicManager.class.getResourceAsStream(path);
                AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);

                if(isMenu) {
                    startMenuClip = clip;
                } else {
                    gameClip = clip;
                }

                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void stopAll() {
        if(startMenuClip != null) startMenuClip.stop();
        if(gameClip != null) gameClip.stop();
        if(gameOverClip != null) gameOverClip.stop(); // 停止游戏结束音乐
    }
}