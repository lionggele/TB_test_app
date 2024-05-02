package com.google.ar.core.codelab.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.function.SharedViewModel;

import java.util.Locale;

public class TuberFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private View view;
    public TuberFragment(){
        // require a empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tuberculosis, container, false);
        ImageView imageView = view.findViewById(R.id.picturesButton);
        TextView diameterView = view.findViewById(R.id.textDiameter);
        TextView resultView = view.findViewById(R.id.textPositives);

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getImageUrl().observe(getViewLifecycleOwner(), imageUrl -> {
            if (imageUrl != null) {
                Glide.with(this).load(imageUrl).into(imageView);
            }
        });

        sharedViewModel.getRealWorldDiameter().observe(getViewLifecycleOwner(), diameter -> {
            if (diameter != null) {
                diameterView.setText(String.format(Locale.getDefault(), "%.2f mm", diameter));
            }
        });

        sharedViewModel.getInterpretation().observe(getViewLifecycleOwner(), interpretation -> {
            if (interpretation != null) {
                resultView.setText(interpretation);
            }
        });
        //setupObservers();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel.getVaccinationStatus().observe(getViewLifecycleOwner(), isVaccinated -> {
            Log.d("TuberFragment", "Vaccination status updated: " + isVaccinated);
            updateVaccinationStatus(isVaccinated);
        });
        sharedViewModel.getCloseContactStatus().observe(getViewLifecycleOwner(), isCloseContact -> {
            Log.d("TuberFragment", "Risk status updated: " + isCloseContact);
            updateRiskStatus(isCloseContact);
        });
    }


//    private void setupObservers() {
//        Log.d("TuberFragment", "Setting up observers");
//        sharedViewModel.getVaccinationStatus().observe(getViewLifecycleOwner(), this::updateVaccinationStatus);
//        sharedViewModel.getCloseContactStatus().observe(getViewLifecycleOwner(), this::updateRiskStatus);
//    }

    public void updateVaccinationStatus(boolean isDone) {
        Log.d("TuberFragment", "Setting up observers");
        CardView card = view.findViewById(R.id.vaccinationCard);
        card.setCardBackgroundColor(ContextCompat.getColor(getContext(), isDone ? R.color.green : R.color.red));
        ((TextView) view.findViewById(R.id.VaccinationText)).setText(isDone ? "Vaccination: \nDone" : "Vaccination: \nNot Done");
    }

    public void updateRiskStatus(boolean isCloseContact) {
        CardView card = view.findViewById(R.id.riskStatusCard);
        card.setCardBackgroundColor(ContextCompat.getColor(getContext(), isCloseContact ? R.color.red : R.color.green));
        ((TextView) view.findViewById(R.id.riskStatustext)).setText(isCloseContact ? "Risk Status: Close Contact" : "Risk Status: No Contact");
    }

}

