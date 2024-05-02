package com.google.ar.core.codelab.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.codelab.depth.DepthCodelabActivity;
import com.google.ar.core.codelab.depth.R;

public class RegisterpageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ImageButton backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> {
            // End the current activity which will navigate back to the previous activity
            onBackPressed();
        });

        Button button2 = findViewById(R.id.registerbutton);
        button2.setOnClickListener(v -> openRegisterPage());


    }
    @Override
    public void onBackPressed(){
        finish();
    }

    public void openRegisterPage() {
        Intent intent2 = new Intent(RegisterpageActivity.this, MedicalProfileSetupActivity.class);
        startActivity(intent2);
    }

}
