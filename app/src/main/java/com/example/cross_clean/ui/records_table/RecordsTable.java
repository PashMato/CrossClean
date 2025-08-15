package com.example.cross_clean.ui.records_table;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cross_clean.R;
import com.example.cross_clean.cross_clean.records.AppDatabase;


public class RecordsTable extends FrameLayout {
    RecordsAdapter adapter;

    ImageView backButton;
    public ConstraintLayout mainManuParent;

    public RecordsTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecordsTable(Context context) {
        super(context);
        init(context);
    }


    /**
     *  this method is called from the constructor to set the base parameters
     * @param context the app's context
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.records_table_fragment, this, true);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        adapter = new RecordsAdapter(AppDatabase.getInstance(context).recordsDao().getAllRecordsByScore(), context);
        recyclerView.setAdapter(adapter);

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> hide());
    }

    public void show() {
        post(() -> {
            mainManuParent.setVisibility(View.GONE);
            setVisibility(View.VISIBLE);
        });
    }

    private void hide() {
        post(() -> {
            mainManuParent.setVisibility(View.VISIBLE);
            setVisibility(View.GONE);
        });
    }
}
