package com.google.ar.core.codelab.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.ar.core.codelab.Activities.InformationActivity;
import com.google.ar.core.codelab.depth.R;

public class InfoFragment extends Fragment {

    public InfoFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up click listeners for each CardView
        setupCardClickListener(view, R.id.firstCardView, 1);
        setupCardClickListener(view, R.id.secondCardView, 2);
        setupCardClickListener(view, R.id.thirdcardview, 3);
        setupCardClickListener(view, R.id.forthCardView, 4); // Use the correct ID, might be fourthCardView
    }

    private void setupCardClickListener(View view, int cardViewId, int infoNumber) {
        CardView cardView = view.findViewById(cardViewId);
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InformationActivity.class);
            intent.putExtra("INFO_NUMBER", infoNumber); // Optional: Pass information to the activity
            startActivity(intent);
        });
    }
}
