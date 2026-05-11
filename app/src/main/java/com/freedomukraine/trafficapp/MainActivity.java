package com.freedomukraine.trafficapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 80, 40, 40);

        TextView question = new TextView(this);
        question.setText("What should a driver do at a red light?");
        question.setTextSize(22);

        Button a = new Button(this);
        a.setText("Speed up");

        Button b = new Button(this);
        b.setText("Stop");

        Button c = new Button(this);
        c.setText("Turn left");

        layout.addView(question);
        layout.addView(a);
        layout.addView(b);
        layout.addView(c);

        setContentView(layout);
    }
}