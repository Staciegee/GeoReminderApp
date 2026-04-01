package com.example.georeminderapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.addLocationBtn).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        findViewById(R.id.viewLocationsBtn).setOnClickListener(v ->
                startActivity(new Intent(this, SavedLocations.class)));
    }
}
