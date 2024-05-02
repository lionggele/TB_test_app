package com.google.ar.core.codelab.Activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.codelab.depth.R;

public class LoginpageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ImageButton backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> {
            // End the current activity which will navigate back to the previous activity
            onBackPressed();
        });

    }
    @Override
    public void onBackPressed(){
        finish();
    }

}
