package com.google.ar.core.codelab.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.function.InfoAdapter;
import com.google.ar.core.codelab.function.InfoModel;

import java.util.ArrayList;
import java.util.List;

public class InformationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private InfoAdapter adapter;
    private List<InfoModel> infoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout); // Ensure this layout file correctly references your RecyclerView

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerView);

        // Initialize your data list
        infoList = new ArrayList<>();
        // Fill infoList with actual data
        for (int i = 0; i < 10; i++) {
            infoList.add(new InfoModel("Information " + i));
        }

        // Set up the adapter and attach it to the RecyclerView
        adapter = new InfoAdapter(infoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
