package com.example.heeder.SoundManager;

import android.content.Context;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.speech.tts.Voice;
import android.util.Log;

import com.example.heeder.R;

public class VoiceManager {
    private static final String LOG_TAG= VoiceManager.class.getSimpleName();

    private static MediaPlayer sleepWarningTone;
    private static MediaPlayer outOfFrameWarningTone;

    public static void StartSleepWarningSound(Context context){
        if(!isPlaying()){
            sleepWarningTone=MediaPlayer.create(context, R.raw.dont_sleep);
            sleepWarningTone.start();
        }
    }

    public static void StartOutOfFrameWarningSound(Context context){
        if(!isPlaying()){
            outOfFrameWarningTone=MediaPlayer.create(context, R.raw.please_be_in_the_frame);
            outOfFrameWarningTone.setVolume(10,10);
            outOfFrameWarningTone.start();
        }
    }

    private static boolean isPlaying(){
        boolean result=false;
        if(sleepWarningTone!=null){
            result=sleepWarningTone.isPlaying();
        }
        if(outOfFrameWarningTone!=null){
            result=result||outOfFrameWarningTone.isPlaying();
        }
        if(result){
            Log.d(LOG_TAG,"Sound is already playing");
        }
        return result;
    }
}
