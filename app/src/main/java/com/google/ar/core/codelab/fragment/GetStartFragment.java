package com.google.ar.core.codelab.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.ar.core.codelab.depth.R;

public class GetStartFragment extends Fragment {

    private static final String ARG_TEXT = "arg_text";

    public static GetStartFragment newInstance(String text) {
        GetStartFragment fragment = new GetStartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_getstartedpage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textView = view.findViewById(R.id.textView);
        if (getArguments() != null) {
            String text = getArguments().getString(ARG_TEXT);
            textView.setText(text);
        }
    }
}

