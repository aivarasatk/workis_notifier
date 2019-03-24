package com.workis.pranesejas;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.workis.pranesejas.fragments.AvailableJobsFragment;
import com.workis.pranesejas.fragments.NewestJobsFragment;
import com.workis.pranesejas.fragments.TodaysJobUpdates;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:{
                return new NewestJobsFragment();
            }
            case 1:{
                return new TodaysJobUpdates();
            }
            case 2:{
                return new AvailableJobsFragment();
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}
