package org.examples.hervekabamba.criminalintent;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.examples.hervekabamba.criminalintent.db.Crime;
import org.examples.hervekabamba.criminalintent.db.CrimeViewModel;
import org.examples.hervekabamba.criminalintent.utilities.DateUtils;

import java.util.List;

public class CrimeListFragment extends Fragment {
    private static final String TAG = "CrimeListFragment";
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private Button addBtn;
    private TextView placeholderTv;
    private boolean mSubtitleVisible;

    private CrimeViewModel mCrimeViewModel;
    private List<Crime> mCrimeList;
    private Crime mCrime;

    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mCrimeViewModel = ViewModelProviders.of(CrimeListFragment.this).get(CrimeViewModel.class);
        mCrimeViewModel.getAllCrimes().observe(CrimeListFragment.this, new Observer<List<Crime>>() {
            @Override
            public void onChanged(@Nullable List<Crime> crimes) {
                if (crimes != null) {
                    mCrimeList = crimes;
                    updateUI();
                    updateSubtitle();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        placeholderTv = (TextView) view.findViewById(R.id.placeHolder_tv);
        addBtn = (Button) view.findViewById(R.id.add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCrime();
            }
        });

        mCrimeRecyclerView = (RecyclerView) view
                .findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSubtitle();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                addCrime();
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        int crimeSize = 0;
        if (mCrimeList != null) {
            crimeSize = mCrimeList.size();
        }
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_format, crimeSize, crimeSize);
        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);

    }


    public void updateUI() {
        if (mCrimeList == null || mCrimeList.size() == 0) {
            placeholderTv.setVisibility(View.VISIBLE);
            addBtn.setVisibility(View.VISIBLE);
        } else {
            placeholderTv.setVisibility(View.GONE);
            addBtn.setVisibility(View.GONE);

            setUpRecyclerView();
        }
    }

    public void addCrime() {
        mCrime = new Crime();
        mCrimeViewModel.insert(mCrime);
        mCallbacks.onCrimeSelected(mCrime);
        updateUI();
    }

    private void setUpRecyclerView() {
        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(mCrimeList);
        }
        mAdapter.setCrimes(mCrimeList);
        mCrimeRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));

            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved_img);

            itemView.setOnClickListener(this);//itemView is the actual View
        }

        public void bind(Crime crime) {

            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(DateUtils.formatDate(mCrime.getDate()));
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);//
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        private void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void deleteItem(int position) {
            Crime crimeToDelete = mCrimes.get(position);
//            mRecentlyDeletedItemPosition = position;
            mCrimes.remove(position);
            mCrimeViewModel.delete(crimeToDelete);
            notifyItemRemoved(position);
//            showUndoSnackbar();
            Toast.makeText(getActivity(), getString(R.string.crime_deleted), Toast.LENGTH_LONG).show();

        }

/*        private void showUndoSnackbar() {
            View view = get.findViewById(R.id.coordinator_layout);
            Snackbar snackbar = Snackbar.make(view, R.string.snack_bar_text,
                    Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.snack_bar_undo, v -> undoDelete());
            snackbar.show();
        }

        private void undoDelete() {
            mListItems.add(mRecentlyDeletedItemPosition,
                    mRecentlyDeletedItem);
            notifyItemInserted(mRecentlyDeletedItemPosition);
        }*/
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private CrimeAdapter mAdapter;

        public SwipeToDeleteCallback(CrimeAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mAdapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Log.d(TAG, "onSwiped: starting..." );
            mAdapter.deleteItem(position);
        }

    }
}
