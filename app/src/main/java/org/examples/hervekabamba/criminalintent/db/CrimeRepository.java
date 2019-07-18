package org.examples.hervekabamba.criminalintent.db;


import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class CrimeRepository {
    private CrimeDao mCrimeDao;
    private LiveData<List<Crime>> mAllCrimes;

    CrimeRepository(Application application) {
        AppDb db = AppDb.getInstance(application);
        mCrimeDao = db.crimeDao();
        mAllCrimes = mCrimeDao.getAllCrimes();
    }

    LiveData<List<Crime>> getAllCrimes() {
        return mAllCrimes;
    }


    public void insert(Crime Crime) {
        new insertAsyncTask(mCrimeDao).execute(Crime);
    }

    public void delete(Crime crime) {
        new deleteCrimeAsyncTask(mCrimeDao).execute(crime);
    }

    private static class insertAsyncTask extends AsyncTask<Crime, Void, Void> {

        private CrimeDao mAsyncTaskDao;

        insertAsyncTask(CrimeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Crime... params) {
            mAsyncTaskDao.insertCrime(params[0]);
            return null;
        }
    }

    private static class deleteCrimeAsyncTask extends AsyncTask<Crime, Void, Void> {
        private CrimeDao mAsyncTaskDao;

        deleteCrimeAsyncTask(CrimeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Crime... params) {
            mAsyncTaskDao.deleteCrime(params[0]);
            return null;
        }
    }
}
