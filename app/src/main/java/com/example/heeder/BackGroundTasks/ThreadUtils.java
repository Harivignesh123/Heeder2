package com.example.heeder.BackGroundTasks;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;


public class ThreadUtils {
    private static final String LOG_TAG= ThreadUtils.class.getSimpleName();

    private static HandlerThread cameraHandlerThread;
    private static Handler cameraHandler;

    public static void StartCameraThread() {
        if (cameraHandlerThread == null || !cameraHandlerThread.isAlive()) {
            try {
                cameraHandlerThread = new HandlerThread("CameraVideoThread");
                cameraHandlerThread.start();
                cameraHandler = new Handler(cameraHandlerThread.getLooper());
                Log.d(LOG_TAG,"Background Thread Started");
            } catch (Exception e) {

            }
        }
    }

    public static void StopCameraThread() {
        if (cameraHandlerThread != null && cameraHandlerThread.isAlive()) {
            try {
                Log.d(LOG_TAG,"Background Thread Stopped");
                cameraHandlerThread.quitSafely();
                cameraHandlerThread=null;
            } catch (Exception e) {
                Log.d(LOG_TAG, "StopBackgroundThread: " + e.getMessage());
            }
        }
    }

    public static Handler GetCameraHandler(){
        return cameraHandler;
    }

    public static void PostCameraTask(Runnable runnable){
        cameraHandler.post(runnable);
    }


    private static ArrayList<Runnable> runnableQueue=new ArrayList<Runnable>();



}
