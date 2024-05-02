package com.google.ar.core.codelab.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.function.QuestionaireAdapter;
import com.google.ar.core.codelab.function.Questionnaire;
import com.google.ar.core.codelab.function.SharedViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestionaireActivity extends AppCompatActivity implements QuestionaireAdapter.OnCategoryChangeListener {

    public static final int CATEGORY_5MM = 5;
    public static final int CATEGORY_10MM = 10;
    public static final int CATEGORY_15MM = 15;
    public static final int CATEGORY_VACCINE = 16;
    public static final int CATEGORY_CONTACT = 17;
    public static final int NO_ANSWER =-1;
    private RecyclerView recyclerView;
    private QuestionaireAdapter adapter;
    public String downloadUrl;
    private List<Questionnaire> questionnaires;
    private HashMap<Integer, Integer> checkedItems;

    private HashMap<Integer, Integer> categoryValues = new HashMap<>();
    private SharedViewModel sharedViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionaire_row);

        questionnaires = new ArrayList<>();
        initializeQuestionnaires();
        checkedItems = new HashMap<>();
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        recyclerView = findViewById(R.id.recycler_view_questionaire);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionaireAdapter(questionnaires, this);
        recyclerView.setAdapter(adapter);

        downloadUrl = getIntent().getStringExtra("ImageDownloadUrl");

        Button nextButton = findViewById(R.id.next);
        nextButton.setOnClickListener(v -> navigateToNextActivity());
    }

    private void initializeQuestionnaires() {
        // need to edit the text and image
        questionnaires.add(new Questionnaire("Have you ever had a positive TB Skin test of TB blood test?", "", false, R.drawable.hiv, 0));
        questionnaires.add(new Questionnaire("Have you had a severe reaction to a TB skin test", "",false, R.drawable.hiv, 0));
        questionnaires.add(new Questionnaire("Have you ever taken medication for tuberculosis?", "Details text 2", false, R.drawable.hiv, 0));

        // > 5mm
        questionnaires.add(new Questionnaire("Are you born in or lived in a high risk country ?", "India, Indonesia, China, the Philippines, Pakistan, Nigeria, Bangladesh and Democratic Republic of the Congo", false, R.drawable.hiv, CATEGORY_5MM));
        questionnaires.add(new Questionnaire("Have you done any organ transplant", "",false, R.drawable.hiv, CATEGORY_5MM));
        questionnaires.add(new Questionnaire("Have you ever used injection drugs", "",false, R.drawable.hiv, CATEGORY_5MM));
        questionnaires.add(new Questionnaire("Do you have HIV/AIDS", " with Tuberculosis people",false, R.drawable.hiv, CATEGORY_5MM));

        // > 10mm
        questionnaires.add(new Questionnaire("Do you have any diseases that could affect your immune system such as cancer, leukemia or other?", "",false, R.drawable.hiv, CATEGORY_10MM));
        questionnaires.add(new Questionnaire("Do you have diabetes?", "", false, R.drawable.hiv, CATEGORY_10MM));
        questionnaires.add(new Questionnaire("Do you have severe kidney disease?", " with Tuberculosis people",false, R.drawable.hiv, CATEGORY_10MM));
        questionnaires.add(new Questionnaire("Are you underweight or do you have any disease which affects how you absorb food an nutrients?", "",false, R.drawable.hiv, CATEGORY_10MM));
        questionnaires.add(new Questionnaire("Have you had an intestinal bypass or gastrectomy?", "Details text 2", false, R.drawable.hiv, CATEGORY_10MM));
        questionnaires.add(new Questionnaire("Do you take any prescription medications? ", "", false, R.drawable.hiv, CATEGORY_10MM));
        questionnaires.add(new Questionnaire("Are you live/work in high risk congregate settings? ", "correctional facilities, nursing homes, homeless, shelters, hospitals & other healthcare facilities", false, R.drawable.hiv, CATEGORY_5MM));

        // 15 mm
        questionnaires.add(new Questionnaire("Are you lived in low incidence TB", "", false, R.drawable.hiv, CATEGORY_15MM));

        // close contact
        questionnaires.add(new Questionnaire("Have you had the BCG vaccine?", " with Tuberculosis people",false, R.drawable.hiv, CATEGORY_VACCINE));

        // close contact
        questionnaires.add(new Questionnaire("Have you been in contact with someone who has TB disease?", "",false, R.drawable.hiv, CATEGORY_CONTACT));
    }

    @Override
    public void onCategoryChanged(HashMap<Integer, Integer> updatedCheckedItems) {
        checkedItems = updatedCheckedItems;
    }

    // In your navigateToNextActivity method of QuestionaireActivity
    private void navigateToNextActivity() {
        // Save the questionnaire responses
        saveResponseData();

        // Pass the vaccination and close contact status to the sharedViewModel
        sharedViewModel.setVaccinationStatus(isVaccinationDone());
        sharedViewModel.setCloseContactStatus(isCloseContact());

        // Navigate to the next activity, in this case, ImageCheckingActivity
        Intent intent = new Intent(QuestionaireActivity.this, ImageCheckingActivity.class);
        intent.putExtra("checkedItems", checkedItems);
        if (downloadUrl != null) {
            intent.putExtra("ImageDownloadUrl", downloadUrl);
        }
        startActivity(intent);
    }

    private void saveResponseData() {
        // Save responses to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("TB_Response_Prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("VaccinationDone", isVaccinationDone());
        editor.putBoolean("CloseContact", isCloseContact());
        editor.apply();
    }

    private boolean isVaccinationDone() {
        // Assuming 1 for true and 0 for false, as stored in checkedItems
        return checkedItems.getOrDefault(CATEGORY_VACCINE, 0) == 1;
    }

    private boolean isCloseContact() {
        // Assuming 1 for true and 0 for false, as stored in checkedItems
        return checkedItems.getOrDefault(CATEGORY_CONTACT, 0) == 1;
    }


//    private boolean isVaccinationDone() {
//        // Check the vaccination status based on the user's response
//        return checkedItems.getOrDefault(CATEGORY_VACCINE, NO_ANSWER) == R.id.questionaire_checkbox;
//    }
//
//    private boolean isCloseContact() {
//        // Check the close contact status based on the user's response
//        return checkedItems.getOrDefault(CATEGORY_CONTACT, NO_ANSWER) == R.id.questionaire_checkbox;
//    }


}
