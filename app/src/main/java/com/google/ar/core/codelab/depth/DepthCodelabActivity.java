/*
 * Copyright 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.codelab.depth;


import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.codelab.common.helpers.CameraPermissionHelper;
import com.google.ar.core.codelab.common.helpers.DisplayRotationHelper;
import com.google.ar.core.codelab.common.helpers.FullScreenHelper;
import com.google.ar.core.codelab.common.helpers.SnackbarHelper;
import com.google.ar.core.codelab.common.helpers.TapHelper;
import com.google.ar.core.codelab.common.helpers.TrackingStateHelper;
import com.google.ar.core.codelab.common.rendering.BackgroundRenderer;
import com.google.ar.core.codelab.common.rendering.CenterOrientationRenderer;
import com.google.ar.core.codelab.common.rendering.CircleOrientationRenderer;
import com.google.ar.core.codelab.common.rendering.ObjectRenderer;
import com.google.ar.core.codelab.common.rendering.OcclusionObjectRenderer;
import com.google.ar.core.codelab.orientation.OrientationHandler;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
public class DepthCodelabActivity extends AppCompatActivity implements GLSurfaceView.Renderer , SensorEventListener{
  private static final String TAG = DepthCodelabActivity.class.getSimpleName();

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;

  private boolean installRequested;
  private boolean isDepthSupported;

  private Session session;
  private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  private DisplayRotationHelper displayRotationHelper;
  private OrientationHandler orientationHandler;
  private CircleOrientationRenderer circleOrientationRenderer;
  private CenterOrientationRenderer centerOrientationRenderer;
  private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);
  private TapHelper tapHelper;

  private final DepthTextureHandler depthTexture = new DepthTextureHandler();
  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final ObjectRenderer virtualObject = new ObjectRenderer();
  private final OcclusionObjectRenderer occludedVirtualObject = new OcclusionObjectRenderer();

  // Temporary matrix allocated here to reduce number of allocations for each frame.
  private final float[] anchorMatrix = new float[16];

  private static final String SEARCHING_PLANE_MESSAGE = "Please move around slowly... Finding the Plane";
  private static final String PLANES_FOUND_MESSAGE = "Plane is found";
  private static final String DEPTH_NOT_AVAILABLE_MESSAGE = "[Depth not supported on this device]";

  // Anchors created from taps used for object placing with a given color.
  private static final float[] OBJECT_COLOR = new float[] {139.0f, 195.0f, 74.0f, 255.0f};
  private final ArrayList<Anchor> anchors = new ArrayList<>();

  private boolean showDepthMap = false;
  private boolean calculateUVTransform = true;

  private int mWidth;
  private int mHeight;

  private  boolean capturePicture = false;

  private TextView distance_TextView;

  private TextView orientation_TextView;

  private TextView orientation2_TextView;

  private Button mButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contour_tracking);
    surfaceView = findViewById(R.id.surfaceview);
    // display the value of between the depth image and camera
    distance_TextView = findViewById(R.id.distance_TextView);
    distance_TextView.setText("0 mm"); // Empty text initially

    // display the angle value of orientation of a phone with roll side
    orientation_TextView = findViewById(R.id.orientation_TextView);
    orientation_TextView.setText("0 '"); // Empty text initially

    // display the angle value of orientation of a phone with pitch side
    orientation2_TextView = findViewById(R.id.orientation2_TextView);
    orientation2_TextView.setText("0 '"); // Empty text initially

    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);
    orientationHandler = new OrientationHandler(/*context=*/this);
    circleOrientationRenderer = new CircleOrientationRenderer(/*context=*/this);
    centerOrientationRenderer = new CenterOrientationRenderer(/*context=*/this, orientationHandler);
    orientationHandler.setOrientationRenderer(centerOrientationRenderer);

    // Set up tap listener.
    tapHelper = new TapHelper(/*context=*/ this);
    surfaceView.setOnTouchListener(tapHelper);

    // Set up renderer.
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    // Set up a Cirlce renderer overlays the surface view (for orientation)
    addContentView(circleOrientationRenderer, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));
    addContentView(centerOrientationRenderer, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));


    installRequested = false;

    final Button toggleDepthButton = (Button) findViewById(R.id.toggle_depth_button);
    toggleDepthButton.setOnClickListener(
        view -> {
          if (isDepthSupported) {
            showDepthMap = !showDepthMap;
            toggleDepthButton.setText(showDepthMap ? R.string.hide_depth : R.string.show_depth);
          } else {
            showDepthMap = false;
            toggleDepthButton.setText(R.string.depth_not_available);
          }
        });


//    mButton = findViewById(R.id.next);
//    mButton.setOnClickListener(view -> {
//      Intent secondActivityIntent = new Intent(DepthCodelabActivity.this, ImageCaptureActivity.class);
//      startActivity(secondActivityIntent);
//    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
          case INSTALL_REQUESTED:
            installRequested = true;
            return;
          case INSTALLED:
            break;
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
          CameraPermissionHelper.requestCameraPermission(this);
          return;
        }

        // Creates the ARCore session.
        session = new Session(/* context= */ this);
        Config config = session.getConfig();
        isDepthSupported = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC);
        if (isDepthSupported) {
          config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {
          config.setDepthMode(Config.DepthMode.DISABLED);
        }
        session.configure(config);


      } catch (UnavailableArcoreNotInstalledException
          | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (UnavailableDeviceNotCompatibleException e) {
        message = "This device does not support AR";
        exception = e;
      } catch (Exception e) {
        message = "Failed to create AR session";
        exception = e;
      }

      if (message != null) {
        messageSnackbarHelper.showError(this, message);
        Log.e(TAG, "Exception creating session", exception);
        return;
      }
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    try {
      session.resume();
    } catch (CameraNotAvailableException e) {
      messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      session = null;
      return;
    }

    surfaceView.onResume();
    displayRotationHelper.onResume();
    orientationHandler.onResume();

  }

  @Override
  public void onPause() {
    super.onPause();
    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      orientationHandler.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

  }
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    super.onRequestPermissionsResult(requestCode, permissions, results);
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      Toast.makeText(this, "Camera permission is needed to run this application",
              Toast.LENGTH_LONG).show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // The depth texture is used for object occlusion and rendering.
      depthTexture.createOnGlThread();

      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(/*context=*/ this);
      backgroundRenderer.createDepthShaders(/*context=*/ this, depthTexture.getDepthTexture());

      virtualObject.createOnGlThread(/*context=*/ this, "models/box.obj", "models/box_texture.png");
      virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);

      if (isDepthSupported) {
        occludedVirtualObject.createOnGlThread(/*context=*/ this, "models/box.obj", "models/box_texture.png");
        occludedVirtualObject.setDepthTexture(
            depthTexture.getDepthTexture(),
            depthTexture.getDepthWidth(),
            depthTexture.getDepthHeight());
        occludedVirtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);
      }
    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
    mWidth = width;
    mHeight = height;
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
  }



  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {

  }



  @Override
  public void onDrawFrame(GL10 gl) {
    // Clear screen to notify driver it should not load any pixels from previous frame.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }
    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    try {
      session.setCameraTextureName(backgroundRenderer.getTextureId());

      // Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      Frame frame = session.update();
      Camera camera = frame.getCamera();


      if (frame.hasDisplayGeometryChanged() || calculateUVTransform) {
        calculateUVTransform = false;
        float[] transform = getTextureTransformMatrix(frame);
        occludedVirtualObject.setUvTransformMatrix(transform);
      }


      // Retrieves the latest depth image for this frame.
      if (isDepthSupported) {
        depthTexture.update(frame);
      }


      // Handle one tap per frame.
      handleTap(frame, camera);

      // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
      trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

      // If frame is ready, render camera preview image to the GL surface.
      backgroundRenderer.draw(frame);

      if (showDepthMap) {
        backgroundRenderer.drawDepth(frame);
      }
      if (capturePicture ) {
        capturePicture = false;
        SavePicture();
      }

      // If not tracking, don't draw 3D objects, show tracking failure reason instead.
      if (camera.getTrackingState() == TrackingState.PAUSED) {
        messageSnackbarHelper.showMessage(
            this, TrackingStateHelper.getTrackingFailureReasonString(camera));
        return;
      }

      // Get projection matrix.
      float[] projmtx = new float[16];
      camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

      // Get camera matrix and draw.
      float[] viewmtx = new float[16];
      camera.getViewMatrix(viewmtx, 0);

      // Compute lighting from average intensity of the image.
      // The first three components are color scaling factors.
      // The last one is the average pixel intensity in gamma space.
      final float[] colorCorrectionRgba = new float[4];
      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

      // No tracking error at this point. Inform user of what to do based on if planes are found.
      String messageToShow = "";
      if (hasTrackingPlane()) {
        messageToShow = PLANES_FOUND_MESSAGE;
      } else {
        messageToShow = SEARCHING_PLANE_MESSAGE;
      }
      if (!isDepthSupported) {
        messageToShow += "\n" +  DEPTH_NOT_AVAILABLE_MESSAGE;
      }
      messageSnackbarHelper.showMessage(this, messageToShow);


      // Visualize anchors created by touch.
      float scaleFactor = 1.0f;
      for (Anchor anchor : anchors) {
        // checking if ARCore is actively tracking the anchor and has reliable position and orientation
        if (anchor.getTrackingState() != TrackingState.TRACKING) {
          continue;
        }
        // Get the current pose of an Anchor in world space. The Anchor pose is updated
        // during calls to session.update() as ARCore refines its estimate of the world.
        anchor.getPose().toMatrix(anchorMatrix, 0);

        // Update and draw the model and its shadow.
        if (isDepthSupported) {
          occludedVirtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
          occludedVirtualObject.draw(viewmtx, projmtx, colorCorrectionRgba, OBJECT_COLOR);
        } else {
          virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
        }

        /*
        // Calculate the distance between the camera and the plane.
        float distanceToPlane = calculateDistanceToPlane(anchor.getPose(), camera.getPose());
        String distanceText = String.format(Locale.getDefault(), "%.2f", distanceToPlane);

        // Update the TextView with the distance text.
        //distance_TextView.setText(distanceText);

         */

      }

      if (depthTexture.getDepthValue() >= 401) {
        // above 701, ratio will increase by 1 every 74 mm
        int r = Math.min(15, Math.max(10, Math.round(depthTexture.getDepthValue() / 74)));//74.2
        centerOrientationRenderer.updateCircleSize(r);
      }
      else{
        // depthImage Value is smaller than 701, ratio will be 9 for the center circle
        int r = 9;
        centerOrientationRenderer.updateCircleSize(r);
      }


      // Format the depth value as a string.
      String distanceText = String.format("%d", depthTexture.getDepthValue()-50);

      // Display the distance text in a TextView.
      distance_TextView.setText(distanceText);

      // Format the orientation value as a string.
      String orientation = String.format("%.2f '", orientationHandler.getdegree());

      // Display the orientation text in a TextView.
      orientation_TextView.setText(orientation);


      String orientation2 = String.format("%.2f '", orientationHandler.getDegree2());

      // Display the orientation text in a TextView.
      orientation2_TextView.setText(orientation2);
    //&& depthTexture.getDepthValue() >= 215
      if (depthTexture.getDepthValue() <= 271 && depthTexture.getDepthValue() >= 269  && orientationHandler.getdegree() == 0f &&  orientationHandler.getDegree2() == 0f){
        try {
          Intent secondActivityIntent = new Intent(DepthCodelabActivity.this, ImageCaptureActivity.class);
          startActivity(secondActivityIntent);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.S
      Log.e(TAG, "Exception on the OpenGL thread", t);
    }
  }


    private void handleTap(Frame frame, Camera camera) {
    MotionEvent tap = tapHelper.poll();
    if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
      for (HitResult hit : frame.hitTest(tap)) {
        Trackable trackable = hit.getTrackable();
        if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
          // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
          // Cap the number of objects created. This avoids overloading both the rendering system and ARCore.
          if (anchors.size() >= 30) { // original is 20
            anchors.get(0).detach();
            anchors.remove(0);
          }

          // Adding an Anchor tells ARCore that it should track this position in
          // space. This anchor is created on the Plane to place the 3D model
          // in the correct position relative both to the world and to the plane.
          anchors.add(hit.createAnchor());
          break;
        }
      }
    }
  }

  // Checks if we detected at least one plane.
  private boolean hasTrackingPlane() {
    for (Plane plane : session.getAllTrackables(Plane.class)) {
      if (plane.getTrackingState() == TrackingState.TRACKING) {
        return true;
      }
    }
    return false;
  }


  /**
   * Call from the GLThread to save a picture of the current frame.
   */
  public void SavePicture() throws IOException {
    int pixelData[] = new int[mWidth * mHeight];

    // Read the pixels from the current GL frame.
    IntBuffer buf = IntBuffer.wrap(pixelData);
    buf.position(0);
    GLES20.glReadPixels(0, 0, mWidth, mHeight,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);

    // Create a file in the external storage in the device /depthcodelab
    final File out = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + "/depthImages", "IMG" +
            Long.toHexString(System.currentTimeMillis()) + ".png");

    // Make sure the directory exists
    if (!out.getParentFile().exists()) {
      out.getParentFile().mkdirs();
    }

    // Convert the pixel data from RGBA to what Android wants, ARGB.
    int bitmapData[] = new int[pixelData.length];
    for (int i = 0; i < mHeight; i++) {
      for (int j = 0; j < mWidth; j++) {
        int p = pixelData[i * mWidth + j];
        int b = (p & 0x00ff0000) >> 16;
        int r = (p & 0x000000ff) << 16;
        int ga = p & 0xff00ff00;
        bitmapData[(mHeight - i - 1) * mWidth + j] = ga | r | b;
      }
    }
    // Create a bitmap.
    Bitmap bmp = Bitmap.createBitmap(bitmapData,
            mWidth, mHeight, Bitmap.Config.ARGB_8888);

    // Write it to disk.
    FileOutputStream fos = new FileOutputStream(out);
    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
    fos.flush();
    fos.close();

  }

  /**
   * This method returns a transformation matrix that when applied to screen space uvs makes them
   * match correctly with the quad texture coords used to render the camera feed. It takes into
   * account device orientation.
   */
  private static float[] getTextureTransformMatrix(Frame frame) {
    float[] frameTransform = new float[6];
    float[] uvTransform = new float[9];
    // XY pairs of coordinates in NDC space that constitute the origin and points along the two
    // principal axes.
    float[] ndcBasis = {0, 0, 1, 0, 0, 1};

    // Temporarily store the transformed points into outputTransform.
    frame.transformCoordinates2d(
        Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
        ndcBasis,
        Coordinates2d.TEXTURE_NORMALIZED,
        frameTransform);

    // Convert the transformed points into an affine transform and transpose it.
    float ndcOriginX = frameTransform[0];
    float ndcOriginY = frameTransform[1];
    uvTransform[0] = frameTransform[2] - ndcOriginX;
    uvTransform[1] = frameTransform[3] - ndcOriginY;
    uvTransform[2] = 0;
    uvTransform[3] = frameTransform[4] - ndcOriginX;
    uvTransform[4] = frameTransform[5] - ndcOriginY;
    uvTransform[5] = 0;
    uvTransform[6] = ndcOriginX;
    uvTransform[7] = ndcOriginY;
    uvTransform[8] = 1;

    return uvTransform;
  }
}




  /*

  // Calculate the normal distance to plane from cameraPose, the given planePose should have y axis
  // parallel to plane's normal, for example plane's center pose or hit test pose.
  private static float calculateDistanceToPlane(Pose planePose, Pose cameraPose) {
    float[] normal = new float[3];
    float cameraX = cameraPose.tx();
    float cameraY = cameraPose.ty();
    float cameraZ = cameraPose.tz();
    // Get transformed Y axis of plane's coordinate system.
    planePose.getTransformedAxis(1, 1.0f, normal, 0);
    // Compute dot product of plane's normal with vector from camera to plane center.
    return (cameraX - planePose.tx()) * normal[0]
            + (cameraY - planePose.ty()) * normal[1]
            + (cameraZ - planePose.tz()) * normal[2];
  }


  // Calculate the distance between the camera and the object
  private float calculateDistanceToObj(Pose objectPose, Pose cameraPose) {
    float dx = objectPose.tx() - cameraPose.tx();
    float dy = objectPose.ty() - cameraPose.ty();
    float dz = objectPose.tz() - cameraPose.tz();
    return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
  }
  */
