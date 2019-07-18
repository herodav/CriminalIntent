package org.examples.hervekabamba.criminalintent.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CrimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCrime(Crime c);

    @Delete
    void deleteCrime(Crime c);

    @Query("SELECT * FROM crime_table ORDER BY id")
    LiveData<List<Crime>> getAllCrimes();

    @Query("SELECT * FROM crime_table WHERE id= :id")
    List<Crime> findCrimeById(String id);


}
