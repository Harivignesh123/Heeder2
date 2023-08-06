package com.example.heeder.AccountManager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.heeder.R;

public class AccountFragmentPagerAdapter extends FragmentPagerAdapter {
    private Context context;

    public AccountFragmentPagerAdapter(Context context, @NonNull FragmentManager fm) {
        super(fm);
        this.context=context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new LoginFragment(context);
            case 1:
                return new CreateAccountFragment(context);
            default:
                return new LoginFragment(context);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return context.getString(R.string.login_text);
            case 1:
                return context.getString(R.string.create_account_text);
            default:
                return context.getString(R.string.login_text);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
