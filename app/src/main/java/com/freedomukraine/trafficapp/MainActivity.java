package com.freedomukraine.trafficapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        Button speedUp = new Button(this);
        speedUp.setText("Speed up");

        Button stop = new Button(this);
        stop.setText("Stop");

        Button turnLeft = new Button(this);
        turnLeft.setText("Turn left");

        TextView result = new TextView(this);
        result.setTextSize(22);
        result.setText("Waiting for answer...");

        speedUp.setOnClickListener(v -> result.setText("Wrong: Speed up"));
        stop.setOnClickListener(v -> result.setText("Correct: Stop"));
        turnLeft.setOnClickListener(v -> result.setText("Wrong: Turn left"));

        layout.addView(question);
        layout.addView(speedUp);
        layout.addView(stop);
        layout.addView(turnLeft);
        layout.addView(result);

        setContentView(layout);
    }
}