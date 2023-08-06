package com.example.heeder.FaceProcessing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.heeder.R;
import com.google.mlkit.vision.face.Face;


public class FaceOverlay extends View {

    private Paint borderPaint;
    private Face face;


    public FaceOverlay(Context context) {
        super(context);
        init();

    }

    public FaceOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FaceOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        borderPaint=new Paint();
        borderPaint.setColor(getResources().getColor(R.color.orange));
        borderPaint.setStrokeWidth(10f);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    public void setFaceBox(Face face){
        this.face=face;
        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(this.face!=null){
            canvas.drawRect(face.getBoundingBox(),borderPaint);
        }
    }
}
