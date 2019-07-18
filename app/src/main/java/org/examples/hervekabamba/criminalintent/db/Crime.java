package org.examples.hervekabamba.criminalintent.db;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "crime_table", primaryKeys = ("id"))
public class Crime {
    @NonNull
    private String id;
    @NonNull
    private String date;
    @NonNull
    private String title;
    @NonNull
    private boolean solved;
    @NonNull
    private String suspect;


    public Crime() {
        id = UUID.randomUUID().toString();
        date = new Date().toString();
        title = "";
        solved = false;
        suspect = "";
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    @NonNull
    public String getSuspect() {
        return suspect;
    }

    public void setSuspect(@NonNull String suspect) {
        this.suspect = suspect;
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }
}
