package com.example.enrico.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by enrico on 25/12/17.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter{

    public SectionsPagerAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                ContactFragment contactFragment = new ContactFragment();
                return contactFragment;

            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

            case 2:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 3:
                ProfileFragment profileFragment = new ProfileFragment();
                return profileFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    public CharSequence getPageTitle(int position){
        switch(position){
            case 0:
                return "Friends";
            case 1:
                return "Chats";
            case 2:
                return "Requests";
            case 3:
                return "Profile";
            default:
                return null;
        }
    }
}
