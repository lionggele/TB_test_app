package com.google.ar.core.codelab.Activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.ar.core.codelab.depth.DepthCodelabActivity;
import com.google.ar.core.codelab.depth.R;

import java.util.HashMap;

public class ImageCheckingActivity extends AppCompatActivity {
    private ImageView imageView;
    public String downloadUrl;
    private HashMap<Integer, Integer> checkedItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photohchecking);
        imageView = findViewById(R.id.resultImage);
        Button nextButton = findViewById(R.id.buttonAnalysis);
        Button buttonRetake = findViewById(R.id.buttonRetake);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            checkedItems = (HashMap<Integer, Integer>) extras.getSerializable("checkedItems");
            // Now you can use the checkedStates to interpret the TB skin test results
        }
        downloadUrl = getIntent().getStringExtra("ImageDownloadUrl");
        if (downloadUrl != null) {
            Glide.with(this).load(downloadUrl).into(imageView);
            System.out.println(downloadUrl);
        } else {
            Toast.makeText(this, "Image URL not found", Toast.LENGTH_SHORT).show();
        }

        nextButton.setOnClickListener(v -> Imageanalysis());

        buttonRetake.setOnClickListener(v -> retakeImage());
    }

    public void Imageanalysis(){
        //savedImageUriString = getIntent().getStringExtra("SavedImageUri");
        Intent intent = new Intent(ImageCheckingActivity.this, ResultActivity.class);
        //intent.putExtra("ImageDownloadUrl", downloadUrl);
        intent.putExtra("ImageDownloadUrl", downloadUrl);
        if (checkedItems != null) {
            intent.putExtra("checkedItems", checkedItems);
        }
        startActivity(intent);
    }

    private void retakeImage() {
        Intent intent = new Intent(ImageCheckingActivity.this, DepthCodelabActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void showHelpDialog(View view) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialoghelp, null);

        // Find the "Got it!" button in the custom layout and set a click listener
        Button buttonGotIt = dialogView.findViewById(R.id.dialogButtonGotIt);

        // Create the AlertDialog and set the custom view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set the background of the dialog window to transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Set the click listener for the "Got it!" button to dismiss the dialog when clicked
        buttonGotIt.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }


}
