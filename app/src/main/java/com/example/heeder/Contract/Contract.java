package com.example.heeder.Contract;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.util.Log;

import com.example.heeder.BackGroundTasks.BgCameraService;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class Contract {

    //Firebase
    public static final FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance("https://heeder-2-default-rtdb.asia-southeast1.firebasedatabase.app/");
public  static final FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();


    public static final String STATUS="status";
    public static final String TIME="time";




    public static final int INACTIVE_STATUS=0;
    public static final int ACTIVE_STATUS=1;

    //Face Verification
    public static final String MODEL_FILE="mobile_face_net.tflite";

    public static final int SAVE_FACE_EMBEDDINGS=0;
    public static final int RECOGNIZE_FACE_EMBEDDINGS=1;


    public static final long VERIFICATION_TIMER_DELAY=5*1000;
    public static final long VERIFICATOIN_TIMER_PERIOD=5*1000;

    public static final double MIN_ATTENDANCE_PERCENT=50;


    //Bundle keys
    public static final String ID="ID";
    public static final String UNIQUE_ID="UNIQUE_ID";
    public static final String NAME="NAME";
    public static final String MAIL_ID="MAIL_ID";
    public static final String PHOTO_ARRAY="PHOTO_ARRAY";
    public static final String CLASS_ID="CLASS_ID";


    public static final String CAMERA_MODE="camera_mode";
    public static final int VERIFICATON_MODE=0;
    public static final int FACE_ADDING_MODE=1;
    public static final int MY_CAMERA_REQUEST_CODE = 100;


    private static final int BACK_CAMERA= CameraCharacteristics.LENS_FACING_FRONT;
    private static final int FRONT_CAMERA= CameraCharacteristics.LENS_FACING_BACK;

    //Change camera lens type
    public static final int CAMERA_LENS_TYPE=FRONT_CAMERA;

    //Detection settings
    public static final int MAX_SLEEP_TIME=3; //in seconds
    public static final int MAX_OUT_OF_FRAME_TIME=3;//in seconds
    public static final double EYES_OPEN_CONSTRAINT=0.5;

    public static boolean isBackgroundServiceRunning(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (BgCameraService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static void startBackgroundService(Context context) {

        try {
            if (isBackgroundServiceRunning(context)) {
                Log.d(context.getClass().getName(), "BgCamera Service is already running");
            }
            else {
                Intent intent=new Intent(context,BgCameraService.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);

                } else {
                    context.startService(intent);
                }
            }
        }
        catch (Exception e){
            Log.d(context.getClass().getSimpleName(), e.getMessage());
        }

    }

    public static void stopBackgroundService(Context context){
        try{
            Intent bgService_intent=new Intent(context,BgCameraService.class);
            context.stopService(bgService_intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
