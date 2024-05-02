package com.google.ar.core.codelab.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.ar.core.codelab.depth.DepthCodelabActivity;
import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.fragment.ProfileSetupListener;
import com.google.ar.core.codelab.fragment.addMedicalFragment;

public class MedicalProfileSetupActivity extends AppCompatActivity implements ProfileSetupListener {
    private static final String TAG = "ProfileSetupActivity";
    private FragmentManager fragmentManager;
    private int currentFragmentIndex = 0;

    private Button proceedBtn;
    private String currentBio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);


        ImageButton backbutton = findViewById(R.id.backbutton);
        backbutton.setOnClickListener(v -> {
            onBackPressed();

        });

        fragmentManager = getSupportFragmentManager();


        addMedicalFragment Mrecordfragment = addMedicalFragment.newInstance();


        // Replace the initial fragment with addProfilePictureFragment
        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Mrecordfragment)
                .commitNow();

        proceedBtn = findViewById(R.id.proceedbutton);
        proceedBtn.setOnClickListener(v -> {
           openHomeScreen();
        });


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    public void openHomeScreen() {
        Intent i = new Intent(this, HomeScreenActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }


    @Override
    public void AddmedicalRecord(String mrecrod) {
        currentBio = mrecrod;
        proceedBtn.setEnabled(true);
    }
}