package org.examples.hervekabamba.criminalintent.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.io.File;
import java.util.List;

public class CrimeViewModel extends AndroidViewModel {

    private CrimeRepository mRepository;
    private LiveData<List<Crime>> mAllCrimes;
    private Context mContext;

    public CrimeViewModel (Application application) {
        super(application);
        mContext = application;
        mRepository = new CrimeRepository(application);
        mAllCrimes = mRepository.getAllCrimes();
    }

    public LiveData<List<Crime>> getAllCrimes() { return mAllCrimes; }

    public void insert(Crime crime) { mRepository.insert(crime); }

    public void delete(Crime crime) { mRepository.delete(crime);}

    public File getPhotoFile(Crime crime) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }

}
