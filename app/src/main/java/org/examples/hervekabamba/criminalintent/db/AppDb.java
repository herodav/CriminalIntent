package org.examples.hervekabamba.criminalintent.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities = {Crime.class}, version = 1, exportSchema = false)
public abstract class AppDb extends RoomDatabase {

public abstract CrimeDao crimeDao();
private static AppDb INSTANCE;

static AppDb getInstance(final Context context){
    if (INSTANCE == null) {
        synchronized (AppDb.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        AppDb.class, "app_database")
                                      //TODO: Migration
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
    }
    return INSTANCE;
}
}
