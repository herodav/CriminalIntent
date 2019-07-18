package org.examples.hervekabamba.criminalintent;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.examples.hervekabamba.criminalintent.db.Crime;
import org.examples.hervekabamba.criminalintent.db.CrimeViewModel;
import org.examples.hervekabamba.criminalintent.utilities.DateUtils;
import org.examples.hervekabamba.criminalintent.utilities.PictureUtils;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    public static final String CRIME_PHOTO = "CrimePhoto";

    public static final int REQUEST_DATE = 0;
    public static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;

    private EditText mTitleField;
    private Button mDateBtn, mTimeBtn, mSuspectBtn, mReportBtn, mCallSuspectBtn;
    private CheckBox mSolvedCheckBox;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private CrimeViewModel mCrimeViewModel;

    private Crime mCrime;
    private File mPhotoFile;
    private String mCrimeId, mPhoneNumber;
    private String mTitle;

    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
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

        mCrimeId = getArguments().getString(ARG_CRIME_ID);
        mCrimeViewModel = ViewModelProviders.of(CrimeFragment.this).get(CrimeViewModel.class);
        mCrimeViewModel.getAllCrimes().observe(this, new Observer<List<Crime>>() {
            @Override
            public void onChanged(@Nullable List<Crime> crimes) {
                if (crimes != null && crimes.size() > 0) {
                    for (Crime c : crimes) {
                        if (c.getId().equals(mCrimeId)) {
                            mCrime = c;
                            mPhotoFile = mCrimeViewModel.getPhotoFile(mCrime);
                            mTitle = mCrime.getTitle();
                            updateUI();
                        }
                    }
                }
            }
        });
    }

    private void updateUI() {
        updateDate();
        updateTime();
        mTitleField.setText(mCrime.getTitle());
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        if (!mCrime.getSuspect().isEmpty()) {
            mSuspectBtn.setText(mCrime.getSuspect());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        Log.d(TAG, "onCreateView: started");
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: Crime = " + mCrime);
                if (mCrime != null) {
                    mCrime.setTitle(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This one too
            }
        });

        mDateBtn = (Button) v.findViewById(R.id.crime_date);
        mDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                try {
                    Date date = DateUtils.parseDate(mCrime.getDate());
                    DatePickerFragment dialog = DatePickerFragment.newInstance(date);
                    dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    dialog.show(manager, DIALOG_DATE);
                } catch (ParseException e) {
                    Log.e(TAG, "onClick: " + e.toString());
                }
            }
        });

        mTimeBtn = (Button) v.findViewById(R.id.crime_time);
        mTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();

                try {
                    Date date = DateUtils.parseDate(mCrime.getDate());
                    TimePickerFragment fragment = TimePickerFragment.newInstance(date);
                    fragment.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                    fragment.show(manager, DIALOG_TIME);
                } catch (ParseException e) {
                    Log.e(TAG, "onClick: " + e.toString());
                }
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (mCrime != null){
                    mCrime.setSolved(isChecked);
                }
            }
        });

        mReportBtn = (Button) v.findViewById(R.id.crime_report);
        mReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setChooserTitle(getString(R.string.send_report))
                        .setText(getCrimeReport())
                        .startChooser();

       /*         Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);*/
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        mSuspectBtn = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectBtn.setEnabled(false);
        }

        mCallSuspectBtn = (Button) v.findViewById(R.id.call_suspect);
        mCallSuspectBtn.setEnabled(false);
        mCallSuspectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mPhoneNumber));
                startActivity(intent);
            }
        });

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "org.examples.hervekabamba.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoView.getDrawable() != null && mPhotoFile != null) {
                    FragmentManager fm = getFragmentManager();
                    CrimePhotoFragment cf = CrimePhotoFragment.newInstance(mPhotoFile.getPath(), mTitle);
                    cf.show(fm, CRIME_PHOTO);
                } else {
                    Toast.makeText(getActivity(), "No picture available", Toast.LENGTH_LONG).show();
                }

            }
        });

        ViewTreeObserver viewTreeObserver = mPhotoView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhotoView();
            }
        });

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_crime:
                updateCrime();
                Toast.makeText(getActivity(), getString(R.string.crime_saved), Toast.LENGTH_LONG).show();
//                startCrimeListFragment();
                return true;
            case R.id.delete_crime:
                mCrimeViewModel.delete(mCrime);
                Toast.makeText(getActivity(), getString(R.string.crime_deleted), Toast.LENGTH_LONG).show();
//                startCrimeListFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Date date;
        switch (requestCode) {
            case REQUEST_DATE:
                date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mCrime.setDate(date.toString());
                updateDate();
                break;
            case REQUEST_TIME:
                date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                mCrime.setDate(date.toString());
                updateDate();
                updateTime();
                break;
            case REQUEST_CONTACT:
                if (data != null) {
                    Uri contactUri = data.getData();

                    //  Find contact based on ID.

                    ContentResolver cr = getActivity().getContentResolver();
                    String[] queryFields = new String[]{ContactsContract.Contacts._ID};
                    Cursor cursor = cr.query(
                            contactUri,
                            queryFields,
                            null,
                            null,
                            null);

                    try {
                        if (cursor.getCount() == 0) {
                            return;
                        }

                        while (cursor.moveToNext()) {
                            String contactId =
                                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            Log.d(TAG, "onActivityResult: ID is: " + contactId);
                            //  Get phone number & name
                            String[] queryField2 = new String[]{
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                            Cursor details = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    queryField2,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                                    null,
                                    null);
                            try {

                                if (details.getCount() == 0) {
                                    Toast.makeText(getActivity(),
                                            getResources().getString(R.string.missing_phone_number), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                while (details.moveToNext()) {
                                    String id = details.getString(details.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                                    String name = details.getString(details.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                    mPhoneNumber = details.getString(details.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.d(TAG, "onActivityResult: " + id + " -- phone:" + mPhoneNumber + "name: " + name);
                                    mCrime.setSuspect(name);
                                    mSuspectBtn.setText(name);
//                                    mCallSuspectBtn.setText(mPhoneNumber);
                                }

                            } finally {
                                details.close();
                            }
                        }

                    } finally {
                        cursor.close();

                    }
                }
            default:
                break;
            case REQUEST_PHOTO:
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "org.examples.hervekabamba.criminalintent.fileprovider",
                        mPhotoFile);
                getActivity().revokeUriPermission(uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                updatePhotoView();
        }
    }

    private void updateCrime() {
        mCrimeViewModel.insert(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    @SuppressLint("StringFormatInvalid")
    private void updateDate() {
        String date = DateUtils.formatDate(mCrime.getDate());
        mDateBtn.setText(getString(R.string.date_format, date));
    }

    @SuppressLint("StringFormatInvalid")
    private void updateTime() {
        String time = DateUtils.formatTime(mCrime.getDate());
        mTimeBtn.setText(getString(R.string.time_format, time));
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = DateUtils.formatDate(mCrime.getDate());
        String suspect = mCrime.getSuspect();
        if (suspect.isEmpty()) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {

            mPhotoView.setImageDrawable(null);
        } else {
            try {
                Bitmap bitmap = PictureUtils.getScaledBitmap(
                        mPhotoFile.getPath(), getActivity());
                mPhotoView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        }
    }


    //Call this method in Host Activity to open this Fragment
    public static CrimeFragment newInstance(String crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
