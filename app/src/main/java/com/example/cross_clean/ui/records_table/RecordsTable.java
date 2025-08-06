package com.example.cross_clean.ui.records_table;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cross_clean.R;
import com.example.cross_clean.cross_clean.records.AppDatabase;

import java.util.ArrayList;

public class RecordsTable extends AppCompatActivity {
    RecordsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_table_layout);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordsAdapter(AppDatabase.getInstance(this).recordsDao().getAllRecordsByScore(), this);
        recyclerView.setAdapter(adapter);
    }
}
