package com.example.heeder.AccountManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heeder.Contract.Contract;
import com.example.heeder.DatabasManager.DatabaseOperations;
import com.example.heeder.FileManager.StorageClass;
import com.example.heeder.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Result;

public class FacultyControlActivity extends AppCompatActivity {


    private static final String LOG_TAG = FacultyActivity.class.getSimpleName();

    private Button control_button;
    private Button open_sheet_button;


    private TextView attendance;
    private ProgressBar progressBar;

    private ValueEventListener valueEventListener;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private String classID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_control);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();

        attendance = findViewById(R.id.attendance_view);

        control_button= findViewById(R.id.control_button);
        open_sheet_button = findViewById(R.id.open_sheet);

        progressBar = findViewById(R.id.progress_bar_view);

        classID=String.valueOf(getIntent().getIntExtra(Contract.CLASS_ID,-1));
        EXCEL_FILE_NAME = classID;

        control_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (control_button.getText().equals(getString(R.string.start_class))) {

                    control_button.setVisibility(View.GONE);
                    open_sheet_button.setVisibility(View.GONE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseOperations.clearAttendanceLog(Integer.valueOf(classID));
                            Map<String,Object> map=new HashMap<>();
                            map.put(Contract.STATUS,Contract.ACTIVE_STATUS);
                            map.put(Contract.TIME,Calendar.getInstance().getTimeInMillis());

                            Contract.firebaseDatabase.getReference().child(classID).updateChildren(map);
                        }
                    }).start();



                } else {
                    control_button.setVisibility(View.GONE);
                    attendance.setText("");
                    open_sheet_button.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    Contract.firebaseDatabase.getReference().child(classID).child(Contract.TIME).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Long startTime = Long.valueOf(snapshot.getValue().toString());
                            Long  endTime = Calendar.getInstance().getTimeInMillis();
                            long num_milli_secs= num_milli_secs(startTime,endTime);
                            int orgCount=(int)(num_milli_secs/(Contract.VERIFICATOIN_TIMER_PERIOD));


                            Log.d(LOG_TAG,startTime+" "+endTime+" "+num_milli_secs+" "+orgCount);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    createExcelWorkbook(DatabaseOperations.getAttendanceLog(Integer.valueOf(classID)),orgCount);
                                    Contract.firebaseDatabase.getReference().child(classID).child(Contract.STATUS).setValue(Contract.INACTIVE_STATUS);
                                }
                            }).start();



                            Contract.firebaseDatabase.getReference().child(Contract.STATUS).setValue(Contract.INACTIVE_STATUS);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }


            }
        });

        open_sheet_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File f = new File(getApplicationContext().getExternalFilesDir(null), EXCEL_FILE_NAME);

                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", f);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/vnd.ms-excel");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(LOG_TAG, "XXXXXXXXXXXXXXXXXXXXXXXXXX " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Cannot open the file", Toast.LENGTH_SHORT).show();
                }

            }
        });

        workbook = new HSSFWorkbook();

        cell = null;

        cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.AQUA.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

        sheet = null;



    }


    public static long num_milli_secs(long incTime, long myTime) {

//        final long secondsInMilli = 1000;
//        final long minutesInMilli = secondsInMilli * 60;
//        final long hoursInMilli = minutesInMilli * 60;
//        final long daysInMilli = hoursInMilli * 24;
//
//        long different = myTime - incTime;
//
//        long elapsedDays = different / daysInMilli;
//        different = different % daysInMilli;
//
//        long elapsedHours = different / hoursInMilli;
//        different = different % hoursInMilli;
//
//        long elapsedMinutes = different / minutesInMilli;
//        different = different % minutesInMilli;
//
//        long elapsedSeconds = different / secondsInMilli;
//        different = different % secondsInMilli;
//
//
//        return elapsedSeconds;

        return myTime-incTime;


    }


    private Cell cell;
    private Sheet sheet;



    private int current_row = 0;

    Workbook workbook;
    CellStyle cellStyle;
    Row row;

    String EXCEL_FILE_NAME = null;

    public void createExcelWorkbook(ResultSet rs,int orgCount) {
        if(rs==null){
            return;
        }

        try{
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSSS");
            String EXCEL_SHEET_NAME = sdf.format(new Date());
            sheet = workbook.createSheet(EXCEL_SHEET_NAME);

            while(rs.next()){

                String reg = rs.getString(1);
                int sCount = rs.getInt(2);
                double percent;
                if(orgCount==0){
                    percent=0;
                }
                else{
                    percent=(sCount/(orgCount*1.0))*100;
                    if(percent>100){
                        percent=100;
                    }
                }

                final double p=percent;


                int att=percent>=Contract.MIN_ATTENDANCE_PERCENT?1:0;

                row = sheet.createRow(current_row++);

                cell = row.createCell(0);
                cell.setCellValue(reg);
                cell.setCellStyle(cellStyle);

                cell = row.createCell(1);
                cell.setCellValue(percent);
                cell.setCellStyle(cellStyle);

                cell = row.createCell(2);
                cell.setCellValue(att);
                cell.setCellStyle(cellStyle);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        attendance.append("\n" + "ID: " + reg + "  " + "Attendance percentage: " + p+" Attendance :"+att);
                    }
                });

            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                        DatabaseOperations.clearAttendanceLog(Integer.valueOf(classID));
                }
            }).start();

            current_row = 0;


            Boolean s = StorageClass.exportDataIntoWorkbook(getApplicationContext(), EXCEL_FILE_NAME, workbook);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (s) {
                        Toast.makeText(getApplicationContext(), "Excel stored", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        open_sheet_button.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getApplicationContext(), "Excel not stored", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }




    }

    @Override
    protected void onResume() {
        super.onResume();

        valueEventListener = Contract.firebaseDatabase.getReference().child(classID).child(Contract.STATUS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot == null || snapshot.getValue() == null || snapshot.getValue().toString().equals(String.valueOf(Contract.INACTIVE_STATUS))) {
                    control_button.setText(getString(R.string.start_class));
                    control_button.setVisibility(View.VISIBLE);
                } else {
                    control_button.setText(getString(R.string.end_class));
                    control_button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    protected void onPause() {
        if (valueEventListener != null) {
            Contract.firebaseDatabase.getReference().child(classID).child(Contract.STATUS).removeEventListener(valueEventListener);
        }

        super.onPause();
    }

}