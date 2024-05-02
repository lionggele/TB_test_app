package com.google.ar.core.codelab.imagecapture;

import android.net.Uri;

public interface UploadCallback {
    void onUploadSuccess(Uri uploadedImageUri);
    void onUploadFailure(Exception e);
}
