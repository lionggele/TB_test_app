package com.google.ar.core.codelab.depth;

import static androidx.camera.core.CameraXThreads.TAG;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.ar.core.codelab.Activities.QuestionaireActivity;
import com.google.ar.core.codelab.common.rendering.CircleOrientationRenderer;
import com.google.ar.core.codelab.imagecapture.UploadCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ImageCaptureActivity extends AppCompatActivity{ //implements View.OnClickListener{
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private PreviewView previewView;

    private Button bCapture;

    private ImageCapture imageCapture;

    private Uri savedImageUri;

    StorageReference storageReference;

    private CameraControl cameraControl;
    private CameraInfo cameraInfo;
    private CircleOrientationRenderer circleOrientationRenderer;
    private static final long CAPTURE_DELAY_MS = 5000; // 5 seconds delay

    //private final Handler handler = new Handler();
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagecapture);

        previewView = findViewById(R.id.previewView);

        //bCapture.setOnClickListener(this);

        storageReference = FirebaseStorage.getInstance().getReference();
        FirebaseApp.initializeApp(this);

        circleOrientationRenderer = new CircleOrientationRenderer(/*context=*/this);
        addContentView(circleOrientationRenderer, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));

        // Camera Provider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(()->{
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
                capturePhoto();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting CameraX", e);
                Thread.currentThread().interrupt();
            }
            },getExecutor());
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
            //cameraProvider.unbindAll();

        //Camera Selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        //Preview Use Case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();
        // Unbind use cases before rebinding
        cameraProvider.unbindAll();
        // Bind use cases to camera
        cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture);
        cameraControl = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture).getCameraControl();
        cameraInfo = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture).getCameraInfo();
    }

//    @Override
//    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.bCapture:
//                capturePhoto();
//                break;
//
//        }
//    }

    // Image Capture function
    private void capturePhoto() {
        ImageCapture imageCapture = this.imageCapture;
        if (imageCapture == null) {
            return;
        }

        // Create time-stamped name and MediaStore entry
        SimpleDateFormat dateFormat = new SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault());
        String name = dateFormat.format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Camera-Image");
        }

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        //cameraControl.enableTorch(true).addListener(() ->
        imageCapture.takePicture(outputOptions , ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        Toast.makeText(ImageCaptureActivity.this, "Error for saving photo" + exc.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {


                        savedImageUri = output.getSavedUri();
                        String savedImage = savedImageUri.toString();
                        if (savedImageUri != null) {

                            Bitmap originalBitmap = null;
                            Uri resizedImageUri = null;
                            try {
                                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), savedImageUri);

                                int orientation = getExifOrientation(savedImageUri); // Implement this method

                                // Resize the Bitmap
                                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 1000, 800);
                                Bitmap croppedBitmap = cropBitmapToCenter(resizedBitmap, 550, 550);

                                // Apply the original orientation to the resized bitmap
                                Bitmap rotatedBitmap = rotateBitmap(croppedBitmap, orientation);

                                // Save the resized Bitmap to a new Uri
                                resizedImageUri = saveBitmapToMediaStore(rotatedBitmap);
                                savedImage = savedImageUri.toString();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            // Resize the Bitmap
                            uploadImagetoFirebase(savedImage, resizedImageUri, new UploadCallback() {
                                @Override
                                public void onUploadSuccess(Uri uploadedImageUri) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(ImageCaptureActivity.this, "Photo has been saved and uploaded successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ImageCaptureActivity.this, QuestionaireActivity.class);
                                        String ImageDownloadUrl = uploadedImageUri.toString();
                                        intent.putExtra("ImageDownloadUrl", ImageDownloadUrl);
                                        startActivity(intent);
                                    });
                                }

                                @Override
                                public void onUploadFailure(Exception e) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(ImageCaptureActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        }

                    }
                }
                );
        //), getExecutor());
    }

    @SuppressLint("RestrictedApi")
    private void disableTorch() {
        if (cameraControl != null) {
            cameraControl.enableTorch(false).addListener(() -> {
                Log.d(TAG, "Flashlight turned off");
            }, getExecutor());
        }
    }

    public Bitmap resizeBitmap(Bitmap originalBitmap, int width, int height) {
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    @SuppressLint("RestrictedApi")
    private void uploadImagetoFirebase(String savedImage, Uri savedImageUri, UploadCallback callback) {
        if (savedImageUri == null) {
            Log.e(TAG, "uploadImagetoFirebase: The file URI is null.");
            callback.onUploadFailure(new Exception("The file URI is null."));
            return;
        }

        // Create a reference to 'images/filename.jpg'
        final StorageReference imageRef = storageReference.child("images/" + savedImage);

        // Upload file to Firebase Storage
        imageRef.putFile(savedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // After the image has been uploaded successfully,
                    // get the download URL from the result
                    taskSnapshot.getMetadata().getReference().getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                disableTorch();
                                Log.d(TAG, "on Success: Upload Image URL is :" + downloadUri.toString());
                                Toast.makeText(ImageCaptureActivity.this, "Image is Uploaded: " + downloadUri, Toast.LENGTH_SHORT).show();
                                callback.onUploadSuccess(downloadUri);
                            })
                            .addOnFailureListener(e -> {
                                disableTorch();
                                // Handle any errors in getting the download URL
                                Log.e(TAG, "Getting download URL failed", e);
                                callback.onUploadFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    disableTorch();
                    Toast.makeText(ImageCaptureActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Upload Failed", e);
                    callback.onUploadFailure(e);
                });
    }

    private Uri saveBitmapToMediaStore(Bitmap bitmap) throws IOException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "resized_image_" + System.currentTimeMillis());
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Camera-Image");
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            try (OutputStream stream = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }
        }
        return uri;
    }


    // Helper method to get the orientation of the image from its Uri
    private int getExifOrientation(Uri imageUri) throws IOException {
        ExifInterface exifInterface = new ExifInterface(getContentResolver().openInputStream(imageUri));
        return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }

    // Helper method to rotate the bitmap according to the given orientation
    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap cropBitmapToCenter(Bitmap bitmap, int width, int height) {
        if (bitmap.getWidth() >= width && bitmap.getHeight() >= height) {
            int startX = (bitmap.getWidth() - width) / 2;
            int startY = (bitmap.getHeight() - height) / 2;
            return Bitmap.createBitmap(bitmap, startX, startY, width, height);
        }
        return bitmap; // Returns the original bitmap if it's smaller than the specified dimensions
    }




}