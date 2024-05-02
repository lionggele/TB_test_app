package com.google.ar.core.codelab.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.function.SharedViewModel;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    private ImageView imageView;
    private ProgressBar progressBar;
    // PyTorch model
    Module module = null;
    Bitmap bitmap;
    public String downloadUrl;
    public String measurementText;
    public Button measurementButton;
    public Tensor outputTensor = null;
    public HashMap<Integer, Integer> checkedItems;

    public String interpretation;
    public double realWorldDiameter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diameter_measurement);
        Button doneButton = findViewById(R.id.doneButton);
        imageView = findViewById(R.id.resultImage);
        //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        progressBar = findViewById(R.id.progressBar);
        measurementButton = findViewById(R.id.measurementText);
        measurementButton.setText(measurementText);

        downloadUrl = getIntent().getStringExtra("ImageDownloadUrl");
        checkedItems = (HashMap<Integer, Integer>) getIntent().getSerializableExtra("checkedItems");
        if (downloadUrl != null && checkedItems != null && !checkedItems.isEmpty()) {
            loadBitmapFromUrl(downloadUrl, checkedItems);
        } else {
            Toast.makeText(this, "Image URL not found or no conditions selected", Toast.LENGTH_SHORT).show();
        }

//        try {
//            bitmap = BitmapFactory.decodeStream(getAssets().open( "409.jpg"));//water_body_1382.jpg
//            bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
////            InputStream is = getAssets().open("40.jpg"); // Use your image filename here
////            BitmapFactory.Options options = new BitmapFactory.Options();
////            options.inPreferredConfig = Bitmap.Config.ARGB_8888; // Use ARGB_8888 here
////            bitmap = BitmapFactory.decodeStream(is, null, options);
////            is.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        doneButton.setOnClickListener(v -> { finishActivity();
        });

    }

    private void finishActivity(){
        Intent intent = new Intent(ResultActivity.this, HomeScreenActivity.class);
        intent.putExtra("OpenFragment", "TuberFragment");
        intent.putExtra("downloadUrl", downloadUrl);
        intent.putExtra("realWorldDiameter", realWorldDiameter);
        intent.putExtra("interpretation", interpretation);
        startActivity(intent);
    }


    private void loadBitmapFromUrl(String url, HashMap<Integer, Integer> checkedStates) {
        Glide.with(this).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                bitmap = resource;
                imageView.setImageBitmap(bitmap);
                processImage(checkedStates);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
    }

    private void interpretResults(HashMap<Integer, Integer> checkedStates, double realWorldDiameter) {
        interpretation = "Negative";

        // Determine the threshold based on the checked states
        int threshold = determineThreshold(checkedStates);

        if (realWorldDiameter >= threshold-0.1) {
            interpretation = "Positive";
        }
        if(realWorldDiameter >= 30 || threshold == 0){
            interpretation = "Invalid";
        }

        // Log the result or update the UI
        Log.d("TST Interpretation", "Result is " + interpretation);

        SharedViewModel model = new ViewModelProvider(this).get(SharedViewModel.class);
        model.setRealWorldDiameter(realWorldDiameter);
        model.setInterpretation(interpretation);
        model.setImageUrl(downloadUrl);

        runOnUiThread(() -> {
            Button resultButton = findViewById(R.id.buttonPositive);  // Make sure to have a button with this ID in your layout
            resultButton.setText(interpretation);
        });
    }

    private int determineThreshold(HashMap<Integer, Integer> checkedStates) {
        // Default threshold
        int threshold = 15;

        // Determine the threshold based on the checked states
        for (int category : checkedStates.values()) {
            if (category == QuestionaireActivity.CATEGORY_5MM || category == QuestionaireActivity.CATEGORY_VACCINE || category == QuestionaireActivity.CATEGORY_CONTACT) {
                threshold = 5; // If any 5mm condition is met, return 5
            } else if (category == QuestionaireActivity.CATEGORY_10MM|| category == QuestionaireActivity.CATEGORY_VACCINE || category == QuestionaireActivity.CATEGORY_CONTACT) {
                threshold = 10; // If any 10mm condition is met, set threshold to 10
            }
            // If CATEGORY_15MM, keep the default threshold
        }

        return threshold;
    }




    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
    private void processImage(HashMap<Integer, Integer> checkedStates) {
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
        new Thread(() -> {
            // Load the image from the assets
            //bitmap = BitmapFactory.decodeStream(getAssets().open( "test.jpg"));
            try {
                module = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "b_model.ptl")); //deeplabv3_scripted.ptl
                System.out.println("Abc");
            } catch (IOException e) {
                Log.e("ImageSegmentation", "Error loading model!", e);
                finish();
            }
            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                    new float[]{0.485f, 0.456f, 0.406f},  // mean RGB
                    new float[]{0.229f, 0.224f, 0.225f}); // std RGB
            // TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
             //       TensorImageUtils.TORCHVISION_NORM_STD_RGB);

            Map<String, IValue> outTensors =
                    module.forward(IValue.from(inputTensor)).toDictStringKey();
            outputTensor = outTensors.get("out").toTensor();
            //final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
            final Bitmap segmentationMap = createSegmentationMap(outputTensor);
            final Bitmap finalImage = overlaySegmentationMap(bitmap, segmentationMap);
            final Bitmap finalImage255 = overlaySegmentationMap255(bitmap, segmentationMap);
            calculateAndDrawLargestDiameter(finalImage255);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                imageView.setImageBitmap(finalImage);//measuredImage
                measurementButton.setText(measurementText);
                interpretResults(checkedStates, realWorldDiameter);
            });
        }).start();
    }

    private static final int[][] LABEL_COLORS = new int[][] {
            {0, 0, 0},      // Background
            //{0, 255, 0},    // Lime Green
            {0, 255, 255},  // Cyan
            // {0, 0, 255},    // Bright Blue
    };


    private Bitmap createSegmentationMap(Tensor outputTensor) {
        // Assuming the output tensor has shape [1, NumClasses, H, W]
        long[] shape = outputTensor.shape();
        if (shape.length != 4 || shape[0] != 1) {
            throw new IllegalArgumentException("Unexpected shape of outputTensor. Expecting [1, NumClasses, H, W]");
        }

        int numClasses = (int) shape[1];
        int height = (int) shape[2];
        int width = (int) shape[3];

        Bitmap segmentationMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float[] scores = outputTensor.getDataAsFloatArray(); // The tensor data
        //int[] colors = new int[width * height];
        // Process each pixel and assign a color based on the highest score
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int maxClassId = 0;
                float maxScore = 0;

                for (int c = 0; c < numClasses; c++) {
                    // Calculate the index for the current class score i*(width*height) + j*width + k
                    int scoreIndex = c * width * height + y * width + x;
                    if (scoreIndex >= scores.length) {
                        throw new ArrayIndexOutOfBoundsException("Score index exceeds scores array length.");
                    }

                    float score = scores[scoreIndex];
                    if (score > maxScore) {
                        maxScore = score;
                        maxClassId = c;
                    }
                }

                // Assign color
                int color = (maxClassId < LABEL_COLORS.length) ? Color.rgb(
                        LABEL_COLORS[maxClassId][0],
                        LABEL_COLORS[maxClassId][1],
                        LABEL_COLORS[maxClassId][2]
                ) : Color.BLACK;
                segmentationMap.setPixel(x, y, color);
                //colors[y * width + x] = color;
            }
        }

        //segmentationMap.setPixels(colors, 0, width, 0, 0, width, height);
        return segmentationMap;
    }




    private Bitmap overlaySegmentationMap(Bitmap originalImage, Bitmap segmentationMap) {
        Bitmap result = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        canvas.drawBitmap(originalImage, 0, 0, paint);

        paint.setAlpha(50); // Semi-transparent
        canvas.drawBitmap(segmentationMap, 0, 0, paint);

        return result;
    }

    private Bitmap overlaySegmentationMap255(Bitmap originalImage, Bitmap segmentationMap) {
        Bitmap result = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        canvas.drawBitmap(originalImage, 0, 0, paint);

        paint.setAlpha(255); // Semi-transparent
        canvas.drawBitmap(segmentationMap, 0, 0, paint);

        return result;
    }


    private Bitmap calculateAndDrawLargestDiameter(Bitmap segmentedBitmap) {
        int width = segmentedBitmap.getWidth();
        int height = segmentedBitmap.getHeight();
        int cyanColor = Color.rgb(0, 255, 255);

        List<Point> edgePoints = findEdgePoints(segmentedBitmap, cyanColor); // Assuming red is the segment color

        // Initialize variables to keep track of the maximum diameter
        double maxDiameter = 0;
        Point maxPointA = null;
        Point maxPointB = null;

        // Find the two points that are farthest apart to calculate the maximum diameter
        for (int i = 0; i < edgePoints.size(); i++) {
            for (int j = i + 1; j < edgePoints.size(); j++) {
                Point p1 = edgePoints.get(i);
                Point p2 = edgePoints.get(j);
                double distance = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
                if (distance > maxDiameter) {
                    maxDiameter = distance;
                    maxPointA = p1;
                    maxPointB = p2;
                }
            }
        }

        // Conversion factor from pixels to real-world units (e.g., millimeters)
        double PIXEL_TO_MM_CONVERSION_FACTOR = getConversionFactor(maxDiameter);

        // Convert pixel distance to real-world units
        realWorldDiameter = maxDiameter * PIXEL_TO_MM_CONVERSION_FACTOR;
        measurementText = String.format(Locale.getDefault(), "%.2f mm", realWorldDiameter);

        // Create a new bitmap to avoid altering the original bitmap which may be used elsewhere
        Bitmap measuredBitmap = segmentedBitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(measuredBitmap);
//        Paint paint = new Paint();
//        paint.setColor(Color.YELLOW); // Line color
//        paint.setStrokeWidth(10); // Line thickness
//
//        if (maxPointA != null && maxPointB != null) {
//            // Draw the line representing the diameter
//            canvas.drawLine(maxPointA.x, maxPointA.y, maxPointB.x, maxPointB.y, paint);
//
//            // Optionally, draw the measurement text
//            paint.setColor(Color.WHITE); // Text color
//            paint.setTextSize(40); // Text size
//            float textX = (maxPointA.x + maxPointB.x) / 2;
//            float textY = (maxPointA.y + maxPointB.y) / 2 - 20; // Offset above the line
//            canvas.drawText(measurementText, textX, textY, paint);
//        }

        return measuredBitmap; // Return the bitmap with the diameter line drawn
    }

    private boolean isEdgePixel(Bitmap bitmap, int x, int y, int segmentColor) {
        // Check 8-neighbors to see if any is not of segmentColor
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip the center pixel
                int neighborColor = bitmap.getPixel(x + dx, y + dy);
                if (neighborColor != segmentColor) {
                    return true; // Found an edge pixel
                }
            }
        }
        return false; // No edge found
    }

    private List<Point> findEdgePoints(Bitmap bitmap, int segmentColor) {
        List<Point> edgePoints = new ArrayList<>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int pixelColor = bitmap.getPixel(x, y);

                // Check if the current pixel color matches the segment color
                if (pixelColor == segmentColor) {
                    // Check the neighboring pixels to determine if it's an edge pixel
                    if (isEdgePixel(bitmap, x, y, segmentColor)) {
                        edgePoints.add(new Point(x, y));
                    }
                }
            }
        }
        return edgePoints;
    }

    private double getConversionFactor(double maxDiameter) {
        // Define the conversion factor from pixel to real-world units, e.g., millimeters.
        // Adjust the conversion factor based on the maxDiameter value.
        if (maxDiameter < 50) {
            return 0.1197; //0.1213; // Conversion factor for maxDiameter < 40
        } else if (maxDiameter >= 50 && maxDiameter < 80) {
            return 0.1523; //0.1493;  // Conversion factor for 50 < maxDiameter < 60
        } else if (maxDiameter >= 80 && maxDiameter < 200) {
            return 0.1499; //0.1234;
        } else{
            return 0.1332;
        }

//        } else{
//            // Define a default conversion factor if needed or throw an exception
//            // return defaultConversionFactor;
//            throw new IllegalArgumentException("Conversion factor not defined for this diameter range.");
//        }
    }

    // 0.2645833333

    // 7mm
    // 0.1145833333 pixel -> 215-220 height
    // 0.11 pixel -> 218-220 height
    // 0.1129 pixel -> 219-220 height -> 7mm

    // 5mm
    // 0.1129 pixel -> 6.21mm  -> 219-220 height
    // 0.0909 pixel (0.22 pixel  -> 5.00mm)
    //  1 attempt -> 4.84mm
    //  2 attempt -> 0.1129 pixel ( 4.88mm) -> 0.115 pixel -> 4.98mm  -> 0.1155 pixel -> 5mm
    //  3 attempt -> 0.1155 pixel  (4.10mm) -> 0.1407 pixel -> 5.00mm
    //  4 attempt -> 0.1407 pixel -> 5.89mm -> 0.1195 pixel -> 5.00mm
    //  5 attempt -> 0.1195 pixel -> 4.18mm -> 0.1195 pixel -> 5.00mm
    //  5 attempt -> 0.1195 pixel -> 4.18mm
    //  6 attempt -> 0.1195 pixel -> 5.51mm -> 0.1085 pixel -> 5.00mm
    //  7 attempt -> 0.1195 pixel; -> 5.43mm -> 0.11 pixel -> 5.00mm
    //  8 attempt -> 0.1195 pixel; -> 4.76mm -> 0.1256 pixel -> 5.00mm
    //  9 attempt -> 0.1195 pixel; -> 5.11mm -> 0.1168 pixel -> 5.00mm
    //  10 attempt -> 0.1195 pixel; -> 4.68mm -> 0.1275 pixel -> 5.00mm

    // 0.12031 ->
    //  conversion_factors = [
    //    0.1155,  # Attempt 2
    //    0.1407,  # Attempt 3
    //    0.1195,  # Attempt 4
    //    0.1195,  # Attempt 5 (first)
    //    0.1195,  # Attempt 5 (second)
    //    0.1085,  # Attempt 6
    //    0.11,    # Attempt 7
    //    0.1256,  # Attempt 8
    //    0.1168,  # Attempt 9
    //    0.1275   # Attempt 10
    //
    //
    //# Calculate the average conversion factor
    //average_conversion_factor = sum(conversion_factors) / len(conversion_factors)
    //average_conversion_factor


    // 10 mm
    // 1 attempt -> 0.1195 pixel -> 7.89 mm -> 0.1617 pixel -> 10mm
    // 2 attempt -> 0.1617 pixel -> 9.19 mm -> 0.1759 pixel -> 10mm
    // 3 attempt -> 0.1759 pixel -> 12.29mm -> 0.1431 pixel -> 10mm
    // 4 attempt -> 0.1431 pixel -> 10.73mm -> 0.1335 pixel -> 10mm
    // 5 attempt -> 0.1335 pixel -> 8.74mm -> 0.1529 pixel -> 10mm
    // 6 attempt -> 0.1529 pixel -> 9.10mm -> 0.1693 pixel -> 10mm
    // 7 attempt -> 0.1693 pixel -> 10.34mm -> 0.1637 pixel -> 10mm
    // 8 attempt -> 0.1693 pixel -> 10.21mm -> 0.1658 pixel -> 10mm
    // 9 attempt -> 0.1693 pixel -> 10.07mm -> 0.1682 pixel -> 10mm
    // 10 attempt -> 0.1693 pixel -> 10.47mm -> 0.1617 pixel -> 10mm

    // 0.15469  == (0.1617 + 0.1759 + 0.1431 + 0.1335 + 0.1529 + 0.1693 + 0.1637 + 0.1658 + 0.1682 + 0.1617) / 10

    // 15mm
    // 1 attempt -> 0.1783 pixel -> 16.84 mm -> 0.164 pixel -> 15mm
    // 2 attempt -> 0.1783 pixel -> 18.19 mm -> 0.1471 pixel -> 15mm
    // 3 attempt -> 0.1783 pixel -> 16.30 mm -> 0.164 pixel -> 15mm
    // 4 attempt -> 0.1783 pixel -> 15.32 mm -> 0.176 pixel -> 15mm
    // 5 attempt -> 0.1783 pixel -> 18.42 mm -> 0.1452 pixel -> 15mm
    // 6 attempt -> 0.1783 pixel -> 17.89 mm -> 0.1752 pixel -> 15mm
    // 7 attempt -> 0.1783 pixel -> 19.18 mm -> 0.1395 pixel -> 15mm
    // 8 attempt -> 0.1783 pixel -> 19.18 mm -> 0.1395 pixel -> 15mm
    // 9 attempt -> 0.1783 pixel -> 14.06 mm -> 0.183 pixel -> 15mm
    // 10 attempt -> 0.1783 pixel -> 17.97 mm -> 0.1488 pixel -> 15mm
    // 1=0.1783mm -> 32.40mm

    // 0.15823

    // test 1 : 5mm -> 4.04 mm
    // test 2 : 5mm -> 4.84 mm
    // test 3 : 5mm -> 5.51 mm
    // test 4 : 5mm -> 5.19 MM
    // test 5 : 5mm -> 5.44 mm


    // test 4 : 5mm -> 6.45mm


    // test 1 : 10mm -> 9.35mm
    // test 2 : 10mm -> 9.52mm
    // test 3 : 10mm -> 8.10mm
    // test 4 : 10mm -> 8.34mm  -> 0.1893 -> 10.20mm
    // test 5 : 10mm -> 8.26mm
    // 0.15469 (x)

    // 0.1693
    // test 1 : 10mm -> 10.82mm
    // test 2 : 10mm -> 10.34mm
    // test 3 : 10mm -> 10.43mm



    // 0.15823
    // test 1 : 15mm -> 22.52 mm
    // test 2 : 15mm -> 25.67 mm
    // test 3 : 15mm -> 21.51 mm


    // test 1 : 15mm -> 20.75mm -> 0.15823 -> 18.33mm -> 0.1289 -> 15mm
    // test 3 : 15mm ->  14.73mm -> 0.1297  -> 0.1309 -> 15.01mm
    // test 2: 15mm  -> 14.10mm  -> 0.1309  -> 0.1405 -> 15.04mm -> 0.1583 -> 16.06mm


    // 0.1405 -> 0.1555
    // test 1 : 15mm -> 15.57 mm
    // test 2 : 15mm -> 14.56 mm
    // test 3 : 15mm -> 14.10 mm


//    public static Bitmap overlayImages(Bitmap originalImage, Bitmap segmentationMap) {
//        // Overlay the segmentation map onto the original image
//        Bitmap overlayedImage = Bitmap.createBitmap(
//                originalImage.getWidth(), originalImage.getHeight(), originalImage.getConfig());
//
//        for (int y = 0; y < originalImage.getHeight(); y++) {
//            for (int x = 0; x < originalImage.getWidth(); x++) {
//                int originalColor = originalImage.getPixel(x, y);
//                int segmentationColor = segmentationMap.getPixel(x, y);
//
//                // Apply alpha blending
//                int blendedColor = blendColors(originalColor, segmentationColor, 0.5f); // 50% transparency
//                overlayedImage.setPixel(x, y, blendedColor);
//            }
//        }
//        return overlayedImage;
//    }
//
//
//    private static int blendColors(int color1, int color2, float ratio) {
//        // Blend two colors with the given ratio
//        int alpha1 = Color.alpha(color1);
//        int red1 = Color.red(color1);
//        int green1 = Color.green(color1);
//        int blue1 = Color.blue(color1);
//
//        int alpha2 = Color.alpha(color2);
//        int red2 = Color.red(color2);
//        int green2 = Color.green(color2);
//        int blue2 = Color.blue(color2);
//
//        int blendedRed = (int) (red1 * (1 - ratio) + red2 * ratio);
//        int blendedGreen = (int) (green1 * (1 - ratio) + green2 * ratio);
//        int blendedBlue = (int) (blue1 * (1 - ratio) + blue2 * ratio);
//        int blendedAlpha = (int) (alpha1 * (1 - ratio) + alpha2 * ratio);
//
//        return Color.argb(blendedAlpha, blendedRed, blendedGreen, blendedBlue);
//    }



}
