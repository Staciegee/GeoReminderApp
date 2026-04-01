package com.example.georeminderapp;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.Query;

    @Database(entities = {LocationEntity.class}, version = 1, exportSchema = true)
    public abstract class AppDatabase extends RoomDatabase {
        public abstract LocationDao locationDao();
    }

