package com.google.ar.core.codelab.objectdetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Recognition {
    private final float top;
    private final float left;
    private final float bottom;
    private final float right;
    private final float confidence;
    private final int classLabel;

    public Recognition(float top, float left, float bottom, float right, float confidence, int classLabel) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.confidence = confidence;
        this.classLabel = classLabel;
    }

//    public float getTop() {
//        return top;
//    }
//
//    public float getLeft() {
//        return left;
//    }
//
//    public float getBottom() {
//        return bottom;
//    }
//
//    public float getRight() {
//        return right;
//    }
//
//    public float getConfidence() {
//        return confidence;
//    }
//
//    public int getClassLabel() {
//        return classLabel;
//    }

    // Draw this bounding box on the provided Canvas
    public void draw(Canvas canvas) {
        // Customize paint settings if necessary
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        canvas.drawRect(this.left, this.top, this.right, this.bottom, paint);
    }
}
