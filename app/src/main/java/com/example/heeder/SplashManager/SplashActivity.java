package com.example.heeder.SplashManager;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.heeder.AccountManager.AccountPage;
import com.example.heeder.AccountManager.AdminActivity;
import com.example.heeder.AccountManager.FacultyActivity;
import com.example.heeder.AccountManager.StudentActivity;
import com.example.heeder.CameraActivity;
import com.example.heeder.Contract.Contract;
import com.example.heeder.DatabasManager.DBcontract;
import com.example.heeder.DatabasManager.DatabaseOperations;
import com.example.heeder.Permissions.PermissionManager;
import com.example.heeder.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        if (!PermissionManager.checkInternetConnection(getApplicationContext())) {
//            Toast.makeText(getApplicationContext(),getString(R.string.no_internet_warning_message_text),Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                DBoperations.getInstance();
//            }
//        }).start();


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                DatabaseOperations.getMemberDetailsWithUniqueID("20BCE0032", DBcontract.STUDENT_ROLE);
//            }
//        }).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                if(FirebaseAuth.getInstance().getCurrentUser()==null){
                    i=new Intent(SplashActivity.this, AccountPage.class);
                }
                else{
                    if(getSharedPreferences(getString(R.string.shared_preferences),MODE_PRIVATE).getString(getString(R.string.id),null)==null){
                        i=new Intent(SplashActivity.this, AccountPage.class);
                    }
                    else{
                        int userRole=getSharedPreferences(getString(R.string.shared_preferences),MODE_PRIVATE).getInt(getString(R.string.user_role),-1);
                        if(userRole==DBcontract.STUDENT_ROLE){
                            i=new Intent(SplashActivity.this, StudentActivity.class);
                        }
                        else if(userRole==DBcontract.FACULTY_ROLE){
                            i=new Intent(SplashActivity.this, FacultyActivity.class);
                        }
                        else if(userRole==DBcontract.ADMIN_ROLE){
                            i=new Intent(SplashActivity.this, AdminActivity.class);
                        }
                        else{
                            i=new Intent(SplashActivity.this, AccountPage.class);
                        }

                    }

                }
                startActivity(i);
                finish();
            }
        },0);
    }
}