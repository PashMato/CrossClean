package com.example.cross_clean.ui;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cross_clean.R;
import com.example.cross_clean.ui.records_table.RecordsTable;

public class MainManu extends AppCompatActivity {
    Button playBN;
    Button recordsBN;
    Button howToBN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_manu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playBN = findViewById(R.id.play_bn);
        recordsBN = findViewById(R.id.records_bn);
        howToBN = findViewById(R.id.how_to_bn);

        playBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent game = new Intent(MainManu.this, LoadingActivity.class);
                startActivity(game);
                overridePendingTransition(0, 0); // disables transition animation
                finish();
            }
        });

        recordsBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recordsTable = new Intent(MainManu.this, RecordsTable.class);
                startActivity(recordsTable);
            }
        });

        howToBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: show how to play the game
            }
        });
    }
}
