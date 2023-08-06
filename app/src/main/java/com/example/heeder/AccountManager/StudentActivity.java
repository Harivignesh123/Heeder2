package com.example.heeder.AccountManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.heeder.DatabasManager.DBcontract;
import com.example.heeder.DatabasManager.DatabaseOperations;
import com.example.heeder.R;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentActivity extends AppCompatActivity {


        private final String LOG_TAG=StudentActivity.class.getSimpleName();

        private ListView listView;
        private StudentArrayAdapter studentAdapter;
        private ProgressBar progressBar;
        private SearchView searchView;
        private ImageView emptyView;

        @Override
        protected void onCreate(Bundle savedInstanceState){
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_student);

                progressBar=findViewById(R.id.progress_bar);
                emptyView=findViewById(R.id.list_empty_view);
                searchView=findViewById(R.id.search_view);
                listView=findViewById(R.id.student_list);

                studentAdapter=new StudentArrayAdapter(StudentActivity.this,0,searchView,emptyView);

                listView.setAdapter(studentAdapter);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                                return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                                studentAdapter.FilterQuery(newText);
                                return false;
                        }
                });

        }

        private void GetAndDisplayAllClasses(){
                isActivityAlive=true;
                ClassHolder.clearAll();

                new Thread(new Runnable() {
                        @Override
                        public void run() {
                                final ResultSet rs= DatabaseOperations.getAllSubjects(getApplicationContext(),DBcontract.STUDENT_ROLE);
                                if(rs==null){
                                        return;
                                }
                                runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                                if(isActivityAlive){
                                                        try{
                                                                if(!rs.next()){
                                                                        //No members found
                                                                        findViewById(R.id.list_empty_view).setVisibility(View.VISIBLE);
                                                                        progressBar.setVisibility(View.GONE);
                                                                }
                                                                else {
                                                                        findViewById(R.id.list_empty_view).setVisibility(View.GONE);
                                                                        listView.setVisibility(View.VISIBLE);
                                                                        progressBar.setVisibility(View.GONE);

                                                                        do {
                                                                                ClassHolder.classHolderArrayList.add(new ClassHolder(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
                                                                        } while (rs.next());

                                                                        studentAdapter.FilterQuery(searchView.getQuery().toString());
                                                                }
                                                        } catch (SQLException throwables) {
                                                                throwables.printStackTrace();
                                                        }
                                                        finally {
                                                                if (rs != null) {
                                                                        try {
                                                                                rs.close();
                                                                        } catch (SQLException sqlEx) {

                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                });
                        }
                }).start();




        }



        private void LogOut(){
                FirebaseAuth.getInstance().signOut();

                SharedPreferences sharedPreferences=getSharedPreferences(getString(R.string.shared_preferences),MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent=new Intent(getApplicationContext(),AccountPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.account_main_menu,menu);
                return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                        case R.id.log_out_button:
                                LogOut();
                                break;
                        default:
                                break;
                }
                return super.onOptionsItemSelected(item);
        }

        private void ShowToast(String message){
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                        }
                });

        }

        private boolean isActivityAlive=false;

        @Override
        protected void onResume() {
                super.onResume();
                GetAndDisplayAllClasses();
        }

        @Override
        protected void onPause() {
                isActivityAlive=false;
                MemberHolder.clearAll();
                progressBar.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
                super.onPause();
        }


}