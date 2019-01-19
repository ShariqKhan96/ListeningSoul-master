package com.webxert.listeningsouls.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.webxert.listeningsouls.fragments.ChatFragment;
import com.webxert.listeningsouls.fragments.StarredFragment;
import com.webxert.listeningsouls.fragments.StoriesFragment;
import com.webxert.listeningsouls.fragments.UserChatFragment;

/**
 * Created by hp on 12/9/2018.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return UserChatFragment.getInstance();
            case 1:
                return StarredFragment.getmInstance();

        }

        return ChatFragment.getmInstance();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "CustomerChats";
            case 1:
                return "AdminChats";
            default:
                return "CustomerChats";
        }
    }
}