package com.example.heeder.BackGroundTasks;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.heeder.CameraManager.CameraTools;
import com.example.heeder.Contract.Contract;
import com.example.heeder.R;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class BgCameraService extends Service {

    private static final String LOG_TAG= BgCameraService.class.getSimpleName();
    private static final String CHANNEL_ID = "100";
    private String NOTIFICATION_CHANNEL_ID = "com.example.heeder";
    private  String channelName = "My Camera Service";


    private TextureView textureView;
    private CameraManager cameraManager;

    private String cameraID;
    private Size mPreviewSize;
    private Surface previewSurface;

    private int totalRotation;

    public BgCameraService() {}

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand called");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startMyOwnForeground();
            }

            SetupCamera(1, 1);
            ConnectCamera();

        } catch (Exception e) {
            Log.d(" Error --->> ", e.getMessage());
        }

        return Service.START_NOT_STICKY;
    }

    private void startMyOwnForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();

            NotificationChannel chan = null;

            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(getString(R.string.notification_title))
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);

        }
    }

    private NotificationChannel createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            return channel;
        }
        return null;
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            Log.d(LOG_TAG, "Camera2: SurfaceTextureAvailable");
            SetupCamera(i, i1);
            ConnectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            if (mCameraDevice != null) {
                CloseCamera();

                mCameraDevice = null;
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
            Bitmap bitmap = Bitmap.createBitmap(textureView.getWidth(), textureView.getHeight(), Bitmap.Config.ARGB_8888);
            textureView.getBitmap(bitmap);

//            if(!isFrameProcessing){
//                processFrame(bitmap);
//            }
        }
    };

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(LOG_TAG, "Camera2: camera device opened");
            mCameraDevice = cameraDevice;
            //StartPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }


    };

    private void SetupCamera(int width, int height) {
        cameraManager = (CameraManager) getSystemService(Service.CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)== Contract.CAMERA_LENS_TYPE) {
                    continue;
                }

//                WindowManager windowManager=(WindowManager) getSystemService(Service.WINDOW_SERVICE);
//                int deviceOrientation = windowManager.getDefaultDisplay().getRotation();
//                totalRotation = CameraTools.sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
//                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
//                int rotatedWidth = width;
//                int rotatedHeight = height;
//                if (swapRotation) {
//                    rotatedWidth = height;
//                    rotatedHeight = width;
//                }
//
//                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                mPreviewSize = CameraTools.ChooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
//
//                Log.d(LOG_TAG, "size: " + mPreviewSize.getHeight() + " x " + mPreviewSize.getWidth());
                cameraID = id;
                return;
            }

        } catch (Exception e) {
            Log.d(LOG_TAG,e.getMessage());
        }
    }

    private void StartPreview() {
        if (mCameraDevice == null) {
            return;
        }

        SurfaceTexture surfaceTexture=new SurfaceTexture(0);

//        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
//        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//        previewSurface = new Surface(surfaceTexture);

        ImageReader reader=ImageReader.newInstance(1,1, ImageFormat.JPEG,1);


        try {
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(reader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL);

            ThreadUtils.StartCameraThread();

            CameraCaptureSession.CaptureCallback captureCallback=new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {

                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                    Log.d(LOG_TAG,"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    super.onCaptureProgressed(session, request, partialResult);
                }
            };

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback,ThreadUtils.GetCameraHandler());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void ConnectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Service.CAMERA_SERVICE);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                cameraManager.openCamera(cameraID, cameraStateCallback, ThreadUtils.GetCameraHandler());
                StartPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            return;
        }
    }

    private void CloseCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

//    private Timer timer=null;
//    private TimerTask timerTask=null;
//    private final long delay=1000;
//    private final long period=1000;
//    private int counter=1;
//    private final int maxCount=20;
//
//    private void StartTimer(){
//        timer=new Timer();
//        InitializeTimerTask();
//        timer.schedule(timerTask,delay,period);
//    }
//
//    private void InitializeTimerTask(){
//        timerTask=new TimerTask() {
//            @Override
//            public void run() {
//                if(counter==maxCount){
//                    StopTimer();
//                }
//                else{
//                    Log.d(LOG_TAG,"COUTER=> "+counter++);
//                }
//
//            }
//        };
//    }
//
//    private void StopTimer(){
//        if(timer!=null) {
//            timer.cancel();
//            timer = null;
//        }
//        onDestroy();
//
//    }

}