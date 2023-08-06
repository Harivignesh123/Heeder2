package com.example.heeder.AccountManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.heeder.CameraActivity;
import com.example.heeder.CameraManager.CameraTools;
import com.example.heeder.Contract.Contract;
import com.example.heeder.DatabasManager.DBcontract;
import com.example.heeder.DatabasManager.DatabaseOperations;
import com.example.heeder.FaceVerification.RecognitionUtils;
import com.example.heeder.Permissions.PermissionManager;
import com.example.heeder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.mlkit.vision.common.InputImage;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;


public class LoginFragment extends Fragment {

    private final String LOG_TAG=LoginFragment.class.getSimpleName();

    private Context context;
    private ProgressBar progressBar;
    private Button loginButton;
    private RadioGroup radioGroup;

    private int globalUserRole=-1;

    public LoginFragment(Context context) {
        this.context=context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView= inflater.inflate(R.layout.fragment_login, container, false);

        radioGroup=rootView.findViewById(R.id.radio_group);
        progressBar=rootView.findViewById(R.id.progress_bar);
        loginButton=((Button)rootView.findViewById(R.id.login_button));
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PermissionManager.checkInternetConnection(context)) {
                    Toast.makeText(context,getString(R.string.no_internet_warning_message_text),Toast.LENGTH_SHORT).show();
                    return;
                }
                ToggleProgressBar();

                final String uniqueID=((EditText)rootView.findViewById(R.id.id_edit_text)).getText().toString().trim();
                final String password=((EditText)rootView.findViewById(R.id.password_edit_text)).getText().toString().trim();

                if(uniqueID.isEmpty()||password.isEmpty()){
                    ShowToast(context.getResources().getString(R.string.fill_all_the_fields_text));
                    ToggleProgressBar();
                    return;
                }

                int userRole;
                if(radioGroup.getCheckedRadioButtonId()==R.id.student_radio_button){
                    userRole= DBcontract.STUDENT_ROLE;
                }
                else if(radioGroup.getCheckedRadioButtonId()==R.id.faculty_radio_button){
                    userRole= DBcontract.FACULTY_ROLE;
                }
                else if(radioGroup.getCheckedRadioButtonId()==R.id.admin_radio_button){
                    userRole= DBcontract.ADMIN_ROLE;
                }else{
                    ShowToast(context.getResources().getString(R.string.choose_user_type_text));
                    ToggleProgressBar();
                    return;
                }

                globalUserRole=userRole;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ResultSet rs= DatabaseOperations.getMemberDetailsWithUniqueID(uniqueID,userRole);

                        if(rs!=null) {
                            try {
                                if (rs.next()) {

                                    if (userRole == DBcontract.STUDENT_ROLE) {
                                        //check for photo then login
                                        String sID = rs.getString(1);
                                        String sUniqueID = rs.getString(2);
                                        String name = rs.getString(3);
                                        String mailID = rs.getString(4);
                                        Blob photoBlob = rs.getBlob(5);
                                        byte[] photoArray=photoBlob.getBytes(1,(int)photoBlob.length());

                                        Bundle bundle=new Bundle();
                                        bundle.putString(Contract.ID,sID);
                                        bundle.putString(Contract.UNIQUE_ID,sUniqueID);
                                        bundle.putString(Contract.NAME,name);
                                        bundle.putString(Contract.MAIL_ID,mailID);
                                        bundle.putByteArray(Contract.PHOTO_ARRAY,photoArray);

                                        if (photoBlob == null) {
                                            ShowToast("Your face is not registered by the admin");
                                            ToggleProgressBar();
                                        } else {
                                            Login(mailID,password,bundle,userRole);
                                        }

                                    } else if(userRole == DBcontract.FACULTY_ROLE){
                                        //login

                                        String fID = rs.getString(1);
                                        String fUniqueID = rs.getString(2);
                                        String name = rs.getString(3);
                                        String mailID = rs.getString(4);

                                        Bundle bundle=new Bundle();
                                        bundle.putString(Contract.ID,fID);
                                        bundle.putString(Contract.UNIQUE_ID,fUniqueID);
                                        bundle.putString(Contract.NAME,name);
                                        bundle.putString(Contract.MAIL_ID,mailID);

                                        Login(mailID,password,bundle,userRole);

                                    }
                                    else{
                                        String aID = rs.getString(1);
                                        String aUniqueID = rs.getString(2);
                                        String name = rs.getString(3);
                                        String mailID = rs.getString(4);

                                        Bundle bundle=new Bundle();
                                        bundle.putString(Contract.ID,aID);
                                        bundle.putString(Contract.UNIQUE_ID,aUniqueID);
                                        bundle.putString(Contract.NAME,name);
                                        bundle.putString(Contract.MAIL_ID,mailID);

                                        Login(mailID,password,bundle,userRole);
                                    }
                                } else {
                                    ShowToast("User not allowed");
                                    ToggleProgressBar();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    rs.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                    }
                }).start();

            }
        });
        return rootView;
    }

    private void Login(String email, String password, Bundle bundle,int userRole){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG,"Successfullu logged in");


                    startNextActivity(bundle,userRole);
                }
                else{
                    try {
                        throw task.getException();
                    }
                    catch (FirebaseAuthInvalidUserException e){
                        ShowToast(getString(R.string.error_invalid_user_text));
                    }
                    catch(FirebaseAuthWeakPasswordException e) {
                        ShowToast(getString(R.string.error_weak_password_text));
                    }
                    catch(FirebaseAuthInvalidCredentialsException e) {
                        ShowToast(getString(R.string.error_invalid_password_text));
                    }
                    catch(FirebaseAuthUserCollisionException e) {
                        ShowToast(getString(R.string.error_user_exists_text));
                    }
                    catch(Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                        ShowToast("problem with signing in");
                    }

                }
                ToggleProgressBar();
            }
        });
    }

    private void startNextActivity(Bundle bundle,int userRole){

        SharedPreferences sharedPreferences= context.getSharedPreferences(getString(R.string.shared_preferences),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(getString(R.string.id),bundle.getString(Contract.ID));
        editor.putString(getString(R.string.unique_id),bundle.getString(Contract.UNIQUE_ID));
        editor.putString(getString(R.string.name),bundle.getString(Contract.NAME));
        editor.putString(getString(R.string.mail_id),bundle.getString(Contract.MAIL_ID));
        editor.putInt(getString(R.string.user_role),userRole);
        editor.apply();

        Intent intent;
        if(userRole==DBcontract.STUDENT_ROLE){
            intent=new Intent(context, StudentActivity.class);
            byte[] photoArray=bundle.getByteArray(Contract.PHOTO_ARRAY);
            Bitmap bitmap= CameraTools.getBitmapFromByteArray(photoArray);
            InputImage inputImage=InputImage.fromBitmap(bitmap,0);

            //Contract.firebaseStorage.getReference().child("1").putStream(CameraTools.bitmapToInputStream(bitmap));

            RecognitionUtils recognitionUtils=new RecognitionUtils(getContext());
            recognitionUtils.LoadModel(getActivity());
            recognitionUtils.DetectFace(inputImage,bitmap,Contract.SAVE_FACE_EMBEDDINGS);

        }
        else if(userRole==DBcontract.FACULTY_ROLE){
            intent=new Intent(context, FacultyActivity.class);
        }
        else{
            intent=new Intent(context, AdminActivity.class);
        }

        intent.putExtras(bundle);

        startActivity(intent);
        if(AccountPage.activity!=null){
            AccountPage.activity.finish();
        }

    }

    private void ToggleProgressBar(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(progressBar.getVisibility()==View.VISIBLE){
                    progressBar.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.GONE);
                }
            }
        });

    }

    private void ShowToast(String message){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
            }
        });

    }

}