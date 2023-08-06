package com.example.heeder.AccountManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.heeder.R;
import com.google.android.material.tabs.TabLayout;

public class AccountPage extends AppCompatActivity {

    private final String LOG_TAG=AccountPage.class.getSimpleName();

    public static Activity activity=null;

    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);
        activity=AccountPage.this;

        tabLayout=findViewById(R.id.tablayout);
        viewPager=findViewById(R.id.viewpager);

        AccountFragmentPagerAdapter simpleFragmentPagerAdapter=new AccountFragmentPagerAdapter(getApplicationContext(),getSupportFragmentManager());
        viewPager.setAdapter(simpleFragmentPagerAdapter);

        tabLayout=findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void ShowToast(String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            }
        });

    }

}