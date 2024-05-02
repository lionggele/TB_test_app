package com.google.ar.core.codelab.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.fragment.HomeFragment;
import com.google.ar.core.codelab.fragment.InfoFragment;
import com.google.ar.core.codelab.fragment.TuberFragment;
import com.google.ar.core.codelab.function.SharedViewModel;

public class HomeScreenActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    private SharedViewModel sharedViewModel;

    public boolean vaccinationDone;

    public boolean closeContact;
    HomeFragment HomeFragment;
    TuberFragment TuberFragment;
    InfoFragment InfoFragment;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);
        HomeFragment = new HomeFragment();
        TuberFragment = new TuberFragment();
        InfoFragment = new InfoFragment();

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.camera);

        // Load default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_Fragment, HomeFragment)
                .commit();

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        String openFragment = getIntent().getStringExtra("OpenFragment");
        if ("TuberFragment".equals(openFragment)) {
            navigateToTuberFragment();
        } else if ("InfoFragment".equals(openFragment)){
            bottomNavigationView.setSelectedItemId(R.id.information);
        }
        else {
            // Default to HomeFragment if no specific instruction is found
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_Fragment, HomeFragment)
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.camera);  // Set Home as default
        }


        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            // Create an intent to start the SettingsActivity
            Intent intent = new Intent(HomeScreenActivity.this, SettingsActivity.class);
            startActivity(intent);
        });


//        NavigationView navigationView=findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()){
//                case R.id.logout:
//                    // logout();
//                    return true;
//            }
//            return false;
//        });
    }



    private void navigateToTuberFragment() {
        String downloadUrl = getIntent().getStringExtra("downloadUrl");
        double realWorldDiameter = getIntent().getDoubleExtra("realWorldDiameter", 0); // Default to 0 if not found
        String interpretation = getIntent().getStringExtra("interpretation");
//        boolean vaccinationStatus = getIntent().getBooleanExtra("vaccinationStatus", false);
//        boolean closeContactStatus = getIntent().getBooleanExtra("closecontact", false);
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        if (downloadUrl != null) {
            sharedViewModel.setImageUrl(downloadUrl);
        }
        sharedViewModel.setRealWorldDiameter(realWorldDiameter);
        sharedViewModel.setInterpretation(interpretation);

//        sharedViewModel.setVaccinationStatus(vaccinationStatus);
//        sharedViewModel.setCloseContactStatus(closeContactStatus);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_Fragment, TuberFragment)
                .commit();
        bottomNavigationView.setSelectedItemId(R.id.tuberculosis);
    }

        @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_Fragment, HomeFragment)
                        .commit();
                return true;

            case R.id.tuberculosis:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_Fragment, TuberFragment)
                        .commit();
                return true;

            case R.id.information:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_Fragment, InfoFragment)
                        .commit();
                return true;
        }
        return false;
    }

}


