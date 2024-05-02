package com.google.ar.core.codelab.objectdetection;

import static android.media.FaceDetector.Face.CONFIDENCE_THRESHOLD;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TFLiteClassifier {
    private Interpreter tflite;

    private static final int BATCH_SIZE = 1;
    private static final int inputSize = 640; // Model input size
    private static final int PIXEL_SIZE = 3;
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;


    public TFLiteClassifier(Context context) {
        try {
            tflite = new Interpreter(loadModelFile(context, "models/best-fp16.tflite"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
        int[] intValues = new int[inputSize * inputSize];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
        resizedBitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            byteBuffer.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            byteBuffer.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            byteBuffer.putFloat(((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        }
        return byteBuffer;
    }


    public float[][][] runInference(Bitmap bitmap) {
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(bitmap);

        // Adjust the output array size based on the model's output tensor
        // Your model's output tensor is [1, 25200, 6]
        float[][][] output = new float[1][25200][6];

        tflite.run(inputBuffer, output);

        return output;
    }

//    public List<Recognition> processOutput(float[][][] modelOutput) {
//        List<Recognition> recognitions = new ArrayList<>();
//
//        // Loop through each detection
//        for (int i = 0; i < modelOutput[0].length; i++) {
//            float centerY = modelOutput[0][i][0];
//            float centerX = modelOutput[0][i][1];
//            float height = modelOutput[0][i][2];
//            float width = modelOutput[0][i][3];
//            float confidence = modelOutput[0][i][4];
//            int classLabel = (int) modelOutput[0][i][5];
//
//            // Assuming the model outputs the center coordinates of the box along with height and width
//            float top = centerY - height / 2.0f;
//            float left = centerX - width / 2.0f;
//            float bottom = centerY + height / 2.0f;
//            float right = centerX + width / 2.0f;
//
//            // Filter out detections with low confidence
//            if (confidence > CONFIDENCE_THRESHOLD) {
//                recognitions.add(new Recognition(top, left, bottom, right, confidence, classLabel));
//            }
//        }
//
//        return recognitions;
//    }






}
