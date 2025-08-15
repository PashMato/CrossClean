package com.example.cross_clean.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.cross_clean.R;


public class GameSettings extends FrameLayout {
    Context context = null;
    Button applyBt;
    Button cancelBt;
    EditText ownerEt;
    CheckBox showTutCb;
    CheckBox accModCb; // accessibility mode

    public GameSettings(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameSettings(Context context) {
        super(context);
        init(context);
    }


    /**
     * this method is called from the constructor and it sets the base parameters
     * @param context the app's context
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.setting_frame, this, true);
        this.context = context;

        applyBt = findViewById(R.id.apply_button);
        cancelBt = findViewById(R.id.cancel_button);

        ownerEt = findViewById(R.id.owner_edit_text);
        showTutCb = findViewById(R.id.show_tutorial);
        accModCb = findViewById(R.id.accessibility_mode);


        applyBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("my_prefs",
                        MODE_PRIVATE).edit();

                // turn on the tutorial if the player change from/to accessibility mode
                boolean isAccChanged = accModCb.isChecked() != context.getSharedPreferences("my_prefs",
                        MODE_PRIVATE).getBoolean("accessibility_mode", false);

                editor.putString("owner_name", ownerEt.getText().toString());
                editor.putBoolean("show_tutorials", showTutCb.isChecked() || isAccChanged);
                editor.putBoolean("accessibility_mode", accModCb.isChecked());
                editor.apply();

                post(() -> setVisibility(View.GONE));
            }
        });

        cancelBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                post(() -> setVisibility(View.GONE));
            }
        });

        post(() -> setVisibility(View.GONE));
    }

    public void show() {
        if (context == null) {
            return;
        }

        post(() -> {
            setVisibility(View.VISIBLE);
        });

        SharedPreferences prefs = context.getSharedPreferences("my_prefs", MODE_PRIVATE);

        ownerEt.setText(prefs.getString("owner_name", "You"));
        showTutCb.setChecked(prefs.getBoolean("show_tutorials", true));
        accModCb.setChecked(prefs.getBoolean("accessibility_mode", false));
    }
}
