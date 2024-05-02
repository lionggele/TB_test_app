package com.google.ar.core.codelab.objectdetection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.ar.core.codelab.depth.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TFLiteObjectDetectionHelper extends AppCompatActivity {
    private static final int NUM_DETECTIONS = 25200;
//    private static final int INPUT_SIZE = 640;
//    private static final String MODEL_NAME = "models/best-fp16.tflite";
//    private Interpreter tflite;
    private Bitmap processedImage;
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private TextView resultsTextView;
    private ImageView imageView;
    private Button detectButton;
    private TFLiteClassifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objdetector);

        imageView = findViewById(R.id.captured_image);
        resultsTextView = findViewById(R.id.tv_results);
        detectButton = findViewById(R.id.btn_detect1);

        // Load the image from the URI sent by the previous Activity
        String imageUriString = getIntent().getStringExtra("SavedImageUri");//"SavedImageUri"
        //Uri imageUri = Uri.parse(String.valueOf(R.drawable.skinimage));
        Uri imageUri = Uri.parse(imageUriString);

        if (imageUri != null) {

            //imageView.setImageResource(R.drawable.skinimage);
            try {
                processedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(processedImage);
            } catch (IOException e) {
                Log.e("ObjDetector", "Error loading image", e);
            }
        } else {
            // Handle the case where the imageUri was not found in the intent or is null
            Log.e("ObjDetector", "Image Uri is null");
            resultsTextView.setText("Image Uri is null");
        }
        detectButton.setOnClickListener(v -> {

        });

    }



}
