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
public class StarredFragment extends Fragment {


   public static StarredFragment mInstance = null;

    public static StarredFragment getmInstance() {
        if(mInstance == null)
            return mInstance = new StarredFragment();
        else return mInstance;
    }

    public StarredFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;

        view =inflater.inflate(R.layout.fragment_starred, container, false);
        return view;

    }

}
