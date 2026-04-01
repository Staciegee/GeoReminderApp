package com.example.georeminderapp;


import androidx.room.Entity;
import androidx.room.PrimaryKey;


        @Entity(tableName = "locations") // ✅ THIS FIXES YOUR ERROR
        public class LocationEntity {

            @PrimaryKey(autoGenerate = true)
            public int id;

            public double latitude;
            public double longitude;
            public String name;
            public float radius;

            // ✅ MAIN CONSTRUCTOR (USE THIS ONE)
            public LocationEntity(double latitude, double longitude, String name, float radius) {
                this.latitude = latitude;
                this.longitude = longitude;
                this.name = name;
                this.radius = radius;
            }
    }
