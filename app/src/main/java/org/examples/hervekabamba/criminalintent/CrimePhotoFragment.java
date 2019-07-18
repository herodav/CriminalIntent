package org.examples.hervekabamba.criminalintent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.examples.hervekabamba.criminalintent.utilities.PictureUtils;

public class CrimePhotoFragment extends AppCompatDialogFragment {

    public static final String FILE_PATH = "file path";
    public static final String CRIME_TITLE = "crime title";

    private String mFilePath, mCrimeTitle;
    private TextView mTitleTv;
    private ImageView mImage;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCrimeTitle = getArguments().getString(CRIME_TITLE);
        mFilePath = getArguments().getString(FILE_PATH);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.crime_photo, container, false);

        mImage = v.findViewById(R.id.photo_detail_img);
        if (mFilePath != null) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mFilePath, getActivity());
            mImage.setImageBitmap(bitmap);
        }

        mTitleTv = v.findViewById(R.id.photo_title_tv);
        if (mCrimeTitle != null) {
            mTitleTv.setText(mCrimeTitle);
        }

        return v;
    }

    public static CrimePhotoFragment newInstance(String filePath, String crimeTitle) {

        Bundle args = new Bundle();
        args.putString(FILE_PATH, filePath);
        args.putString(CRIME_TITLE, crimeTitle);
        CrimePhotoFragment fragment = new CrimePhotoFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
