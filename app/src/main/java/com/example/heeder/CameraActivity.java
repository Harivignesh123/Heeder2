package com.example.heeder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heeder.BackGroundTasks.ThreadUtils;
import com.example.heeder.CameraManager.CameraTools;
import com.example.heeder.Contract.Contract;
import com.example.heeder.DatabasManager.DatabaseOperations;
import com.example.heeder.FaceProcessing.FaceOverlay;
import com.example.heeder.FaceVerification.RecognitionUtils;
import com.example.heeder.SoundManager.VoiceManager;
import com.example.heeder.TimerManager.TimerManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class CameraActivity extends AppCompatActivity {

    private final String LOG_TAG= CameraActivity.class.getSimpleName();

    private TextureView textureView;
    private CameraManager cameraManager;

    private String cameraID;
    private Size mPreviewSize;
    private Surface previewSurface;

    private int totalRotation;

    private FaceDetector faceDetector;
    private boolean isFrameProcessing=false;

    private FaceOverlay faceOverlay;

    private RelativeLayout takePictureLayout;
    private ImageView takePictureButton;
    private ProgressBar takePictureProgressBar;

    private boolean isVerificationMode=false;

    private RecognitionUtils recognitionUtils;
    private int classID;


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

            if(!isFrameProcessing&&isVerificationMode){
                processFrame(bitmap);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        textureView=findViewById(R.id.texture_view);
        faceOverlay=((FaceOverlay)findViewById(R.id.face_overlay_view));
        takePictureLayout=findViewById(R.id.take_picture_layout);
        takePictureButton=findViewById(R.id.take_picture_button);
        takePictureProgressBar=findViewById(R.id.take_picture_progress_bar);

        FaceDetectorOptions faceDetectorOptionsTracker = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        faceDetector = FaceDetection.getClient(faceDetectorOptionsTracker);

        if(getIntent().getIntExtra(Contract.CAMERA_MODE,Contract.VERIFICATON_MODE)==Contract.VERIFICATON_MODE){
            takePictureButton.setVisibility(View.GONE);
            isVerificationMode=true;

            takePictureButton.setOnClickListener(null);

            classID=getIntent().getIntExtra(Contract.CLASS_ID,0);

        }
        else{
            takePictureLayout.setVisibility(View.VISIBLE);
            ShowTakePictureButton();

            isVerificationMode=false;

            final int id=getIntent().getIntExtra(Contract.ID,-1);

            takePictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HideTakePictureButton();
                    CapturePicture(id);
                }
            });

        }

        recognitionUtils=new RecognitionUtils(getApplicationContext());
        recognitionUtils.LoadModel(CameraActivity.this);

    }

    private void ShowTakePictureButton(){
        takePictureButton.setVisibility(View.VISIBLE);
        takePictureProgressBar.setVisibility(View.GONE);
    }

    private void HideTakePictureButton(){
        takePictureButton.setVisibility(View.GONE);
        takePictureProgressBar.setVisibility(View.VISIBLE);
    }


    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(LOG_TAG, "Camera2: camera device opened");
            mCameraDevice = cameraDevice;
            StartPreview();
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


                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                totalRotation = CameraTools.sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if (swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mPreviewSize = CameraTools.ChooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);

                Log.d(LOG_TAG, "size: " + mPreviewSize.getHeight() + " x " + mPreviewSize.getWidth());
                cameraID = id;
                return;
            }

        } catch (Exception e) {

        }
    }

    private void StartPreview() {
        if (mCameraDevice == null) {
            return;
        }

        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        previewSurface = new Surface(surfaceTexture);

        try {
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL);

            ThreadUtils.StartCameraThread();

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null,ThreadUtils.GetCameraHandler());
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

    private void processFrame(Bitmap bitmap1){
        isFrameProcessing=true;
        final InputImage inputImage=InputImage.fromBitmap(bitmap1,0);
        new Thread(new Runnable(){
            @Override
            public void run() {
                Bitmap bitmap=bitmap1;

                faceDetector.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        faceDetector.process(inputImage)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {

                                                if(faces.size()>0){
                                                    Face face=faces.get(0);
                                                    faceOverlay.setFaceBox(face);

                                                    Bitmap frame_bmp = bitmap;

                                                    //Adjust orientation of Face
                                                    Bitmap frame_bmp1 = recognitionUtils.rotateBitmap(frame_bmp,0, false, false);


                                                    //Get bounding box of face
                                                    RectF boundingBox = new RectF(face.getBoundingBox());

                                                    //Crop out bounding box from whole Bitmap(image)
                                                    Bitmap cropped_face = recognitionUtils.getCropBitmapByCPU(frame_bmp1, boundingBox);

                                                    Boolean flipX=true;
                                                    if(flipX)
                                                        cropped_face = recognitionUtils.rotateBitmap(cropped_face, 0, flipX, false);

                                                    Bitmap scaled = recognitionUtils.getResizedBitmap(cropped_face, 112, 112);

                                                    timerTempBitmap=scaled;
                                                    timerTempFace=face;

                                                    processFace(face);

                                                }
                                                else{
                                                    faceOverlay.setFaceBox(null);
                                                    Log.d(LOG_TAG, "Out of frame ");
                                                    TimerManager.startFrameTimer();
                                                    if(TimerManager.getFrameCounter()>Contract.MAX_OUT_OF_FRAME_TIME){
                                                        VoiceManager.StartOutOfFrameWarningSound(getApplicationContext());
                                                    }
                                                    TimerManager.resetSleepTimer();

                                                }
                                                isFrameProcessing=false;
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(LOG_TAG,e.getMessage());
                                                isFrameProcessing=false;
                                            }
                                        });
                    }
                });

            }

        }).start();


    }

    private void processFace(Face face){
        if((face.getLeftEyeOpenProbability()!=null&&face.getLeftEyeOpenProbability()>Contract.EYES_OPEN_CONSTRAINT)||(face.getRightEyeOpenProbability()!=null&&face.getRightEyeOpenProbability()>Contract.EYES_OPEN_CONSTRAINT)){
            Log.d(LOG_TAG, "Active ");
            TimerManager.resetSleepTimer();
        }
        else{
            Log.d(LOG_TAG, "Sleeping");
            TimerManager.startSleepTimer();
            if(TimerManager.getSleepCounter()> Contract.MAX_SLEEP_TIME){
                VoiceManager.StartSleepWarningSound(getApplicationContext());
            }
        }
        TimerManager.resetFrameTimer();
    }


    private void CapturePicture(int id) {
        Bitmap bitmap1 = textureView.getBitmap();
        new Thread(new Runnable(){
            @Override
            public void run(){
                Bitmap bitmap=bitmap1;

                //detect face using google ml face detection
                faceDetector.process(bitmap,0).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        if(faces==null||faces.size()<1){
                            ShowToast(getResources().getString(R.string.no_face_captured_text));
                            ShowTakePictureButton();
                            return;
                        }
                        Face face=faces.get(0);
                        final RectF rectF=new RectF(face.getBoundingBox());
                        new Thread(new Runnable(){
                            @Override
                            public void run(){
                                Bitmap bitmap2=bitmap;
                                Bitmap cropped_bitmap=CameraTools.getCropBitmapByCPU(bitmap2,rectF);
                                Bitmap resized_bitmap=CameraTools.getResizedBitmap(cropped_bitmap,64,64);

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                resized_bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                                final byte[] imageByteArray = bos.toByteArray();

                                if(DatabaseOperations.registerMemberFace(id,imageByteArray)){
                                    ShowToast("Face Registered");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onBackPressed();
                                        }
                                    });

                                }
                                else{
                                    ShowToast("Problem with registering face");
                                    ShowTakePictureButton();
                                }


                            }
                        }).start();


                    }
                });
            }

        }).start();

    }

    private void ShowToast(String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            }
        });

    }

    private Face timerTempFace=null;
    private Bitmap timerTempBitmap=null;

    private long verCounter=0;
    private Timer verTimer;

    private final long verDelay=Contract.VERIFICATION_TIMER_DELAY;
    private final long verPeriod=Contract.VERIFICATOIN_TIMER_PERIOD;

    private TimerTask verTimerTask=null;

    public void startVerTimer(){
        if(verTimer==null){
            verTimer=new Timer();
            initializeVerTimerTask();
            verTimer.schedule(verTimerTask,verDelay,verPeriod);
        }
    }

    private void initializeVerTimerTask(){
        verTimerTask=new TimerTask() {
            @Override
            public void run() {
                final Face timerFace=timerTempFace;
                final Bitmap timerBitmap=timerTempBitmap;

                verCounter++;

                if(timerFace!=null&&timerBitmap!=null){
                    if (recognitionUtils.recognizeImage(timerBitmap)) {
                        Log.d(LOG_TAG,"Face matched");
                        if((timerFace.getLeftEyeOpenProbability()!=null&&timerFace.getLeftEyeOpenProbability()>Contract.EYES_OPEN_CONSTRAINT)||(timerFace.getRightEyeOpenProbability()!=null&&timerFace.getRightEyeOpenProbability()>Contract.EYES_OPEN_CONSTRAINT)){
                            Log.d(LOG_TAG, "Active ");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    DatabaseOperations.insertAttendance(getApplicationContext(),classID);
                                }
                            }).start();
                        }
                        else{
                            Log.d(LOG_TAG, "Sleeping");
                        }
                    }
                    else{
                        Log.d(LOG_TAG,"Face not matched");
                    }
                }

            }
        };
    }

    public long getVerCounter(){
        return verCounter;
    }

    public void resetVerTimer(){
        if(verTimer!=null){
            verTimer.cancel();
            verTimer=null;
        }
        if(verTimerTask!=null){
            verTimerTask.cancel();
            verTimerTask=null;
        }
        verCounter=0;
    }

    @Override
    protected void onResume() {
        ThreadUtils.StartCameraThread();

        if (textureView.isAvailable()) {
            SetupCamera(textureView.getWidth(), textureView.getHeight());
            ConnectCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
        startVerTimer();
        super.onResume();
    }

    @Override
    protected void onPause() {
        CloseCamera();

        ThreadUtils.StopCameraThread();
        resetVerTimer();
        super.onPause();
    }
}