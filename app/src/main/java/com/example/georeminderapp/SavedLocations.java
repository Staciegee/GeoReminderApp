package com.example.georeminderapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

public class SavedLocations extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LocationAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitysavedlocations);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "locations_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        List<LocationEntity> locations = db.locationDao().getAllLocations();

        adapter = new LocationAdapter(locations, db);
        recyclerView.setAdapter(adapter);
    }
}