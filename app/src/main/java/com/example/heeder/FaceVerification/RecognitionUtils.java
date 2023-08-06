package com.example.heeder.FaceVerification;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.heeder.Contract.Contract;
import com.example.heeder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.interfaces.Detector;
//
//import org.checkerframework.checker.units.qual.C;
import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecognitionUtils {
    private static final String TAG =RecognitionUtils.class.getSimpleName() ;
    Interpreter tfLite;
    FaceDetector detector;

    int[] intValues;
    int inputSize=112;  //Input size for model
    boolean isModelQuantized=false;
    float[][] embeedings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE=192; //Output size of model

    Context context;

    public RecognitionUtils(Context context){

        this.context=context;
    }



    public void LoadModel(Activity activity){
        try {
            tfLite=new Interpreter(loadModelFile(activity, Contract.MODEL_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }


        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor=null;
        if(activity!=null){
            fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        }
        else {
            fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        }

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void DetectFace(InputImage image,Bitmap bitmap,int f){
        SharedPreferences sharedPreferences=context.getSharedPreferences(context.getString(R.string.shared_preferences),context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {

                                        if(faces.size()>0) {

                                            Face face = faces.get(0); //Get first face from detected faces
//

                                            Bitmap frame_bmp = bitmap;

                                            //Adjust orientation of Face
                                            Bitmap frame_bmp1 = rotateBitmap(frame_bmp,0, false, false);


                                            //Get bounding box of face
                                            RectF boundingBox = new RectF(face.getBoundingBox());

                                            //Crop out bounding box from whole Bitmap(image)
                                            Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);

                                            Boolean flipX=true;
                                            if(flipX)
                                                cropped_face = rotateBitmap(cropped_face, 0, flipX, false);

                                            Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);

                                            if(f==0){
                                                getEmbeddings(scaled);
                                            }
                                            else {
                                                if(recognizeImage(scaled)){
//                                                    Log.d(TAG, "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR" + " Face Matched");
//                                                    if(face.getLeftEyeOpenProbability()>0.5||face.getRightEyeOpenProbability()>0.5){
//                                                        Log.d(TAG, "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR" + " Active");
////                                                        editor.putInt(String.valueOf(R.integer.attendance),sharedPreferences.getInt(String.valueOf(R.integer.attendance),0)+1);
////                                                        editor.apply();
//                                                       // Log.d(TAG, "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR COUNT:  " + sharedPreferences.getInt(String.valueOf(R.integer.attendance),0));
//                                                    }
//                                                    else{
//
//                                                        Log.d(TAG, "XRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR" + " Sleeping");
//                                                    }
                                                }
                                                else{
                                                    Log.d(TAG, "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR" + " Face not matched");
                                                }
                                            }


                                        }
                                        else{
                                            Log.d(TAG, "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR" + " No face detected");
                                        }

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }

    public void getEmbeddings(final Bitmap bitmap) {

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

            }
        }

        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();


        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable



        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        saveArrayList(embeedings[0]);

    }

    public Boolean recognizeImage(final Bitmap bitmap) {

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];

                imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            }
        }

        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();


        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        float distance_local = (float) 1.0;
        float distance = findNearest(embeedings[0]);
        if (distance_local > distance) {
            //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
            return true;
        } else {
            return false;
        }


    }
    //Compare Faces by distance between face embeddings
    public float findNearest(float[] emb) {

            final float[] knownEmb = getArrayList();

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);


            return distance;


    }

    public static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    public static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public void saveArrayList(float[] list){
        SharedPreferences sharedPreferences=context.getSharedPreferences(context.getString(R.string.shared_preferences),context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(String.valueOf(R.string.embeddings), json);
        editor.apply();

    }

    public float[] getArrayList(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(context.getString(R.string.shared_preferences),context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString(String.valueOf(R.string.embeddings), null);
        Type type = new TypeToken<float[]>() {}.getType();
        return gson.fromJson(json, type);
    }


}
