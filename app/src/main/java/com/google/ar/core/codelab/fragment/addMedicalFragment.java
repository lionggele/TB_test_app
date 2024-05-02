package com.google.ar.core.codelab.fragment;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.ar.core.codelab.depth.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link addMedicalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class addMedicalFragment extends Fragment {

    private ProfileSetupListener listener;

    public addMedicalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment addBioFragment.
     */

    public static addMedicalFragment newInstance() {
        addMedicalFragment fragment = new addMedicalFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onAttach(@NonNull Context ctx){
//        super.onAttach(ctx);
//        try {
//            listener = (ProfileSetupListener) ctx;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(ctx.toString() + " must implement ProfileSetupListener");
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addpatientrecord, container, false);

        TextInputLayout bioInput = view.findViewById(R.id.textField);
        TextInputEditText bioEt = view.findViewById(R.id.textInputEditText);
        bioEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    listener.AddmedicalRecord(editable.toString());
                }
            }
        });
        return view;
    }
}