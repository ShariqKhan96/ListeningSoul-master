package com.webxert.listeningsouls.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.webxert.listeningsouls.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoriesFragment extends Fragment {


    public static StoriesFragment mInstance = null;

    public StoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stories, container, false);
        return view;

    }

    public static StoriesFragment getmInstance() {
        if (mInstance == null)
            return mInstance = new StoriesFragment();
        else return mInstance;

    }
}
