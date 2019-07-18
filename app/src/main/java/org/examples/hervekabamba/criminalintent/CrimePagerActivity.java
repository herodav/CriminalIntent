package org.examples.hervekabamba.criminalintent;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.examples.hervekabamba.criminalintent.db.Crime;
import org.examples.hervekabamba.criminalintent.db.CrimeViewModel;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {
    private static final String TAG = "CrimePagerActivity";

    private static final String EXTRA_CRIME_ID =
            "org.examples.hervekabamba.crime_id";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    public static Intent newIntent(Context packageContext, String crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        mViewPager = (ViewPager) findViewById(R.id.crime_view_pager);

        final String crimeId = getIntent().getStringExtra(EXTRA_CRIME_ID);

        CrimeViewModel crimeViewModel = ViewModelProviders.of(CrimePagerActivity.this).get(CrimeViewModel.class);
        crimeViewModel.getAllCrimes().observe(CrimePagerActivity.this, new Observer<List<Crime>>() {
            @Override
            public void onChanged(@Nullable List<Crime> crimes) {

                mCrimes = crimes;
                updateUI(crimeId);
            }
        });
    }

    private void updateUI(String crimeId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        //Setting item to display
        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        Intent intent = new Intent(CrimePagerActivity.this, CrimeListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        CrimePagerActivity.this.finish();
    }

}
