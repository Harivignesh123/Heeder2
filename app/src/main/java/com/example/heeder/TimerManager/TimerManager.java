package com.example.heeder.TimerManager;

import java.util.Timer;
import java.util.TimerTask;

public class TimerManager {

    private static final long delay=0;
    private static final long period=1000;

    //----------------------------------------------------------------------------------

    private static long sleepCounter=0;
    private static Timer sleepTimer;

    private static TimerTask sleepTimerTask=null;

    public static void startSleepTimer(){
        if(sleepTimer==null){
            sleepTimer=new Timer();
            initializeSleepTimerTask();
            sleepTimer.schedule(sleepTimerTask,delay,period);
        }

    }

    private static void initializeSleepTimerTask(){
        sleepTimerTask=new TimerTask() {
            @Override
            public void run() {
                sleepCounter++;
            }
        };
    }

    public static long getSleepCounter(){
        return sleepCounter;
    }

    public static void resetSleepTimer(){
        if(sleepTimer!=null){
            sleepTimer.cancel();
            sleepTimer=null;
        }
        if(sleepTimerTask!=null){
            sleepTimerTask.cancel();
            sleepTimerTask=null;
        }
        sleepCounter=0;
    }

    //----------------------------------------------------------------------------------

    private static long frameCounter=0;
    private static Timer frameTimer;

    private static  TimerTask frameTimerTask=null;

    public static void startFrameTimer(){
        if(frameTimer==null){
            frameTimer=new Timer();
            initializeFrameTimerTask();
            frameTimer.schedule(frameTimerTask,delay,period);
        }
    }

    private static void initializeFrameTimerTask(){
        frameTimerTask=new TimerTask() {
            @Override
            public void run() {
                frameCounter++;
            }
        };
    }

    public static long getFrameCounter(){
        return frameCounter;
    }

    public static void resetFrameTimer(){
        if(frameTimer!=null){
            frameTimer.cancel();
            frameTimer=null;
        }
        if(frameTimerTask!=null){
            frameTimerTask.cancel();
            frameTimerTask=null;
        }
        frameCounter=0;
    }
//-------------------------------------------------------------------------------------





}
