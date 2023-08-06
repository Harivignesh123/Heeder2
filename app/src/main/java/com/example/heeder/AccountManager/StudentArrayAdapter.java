package com.example.heeder.AccountManager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.example.heeder.CameraActivity;
import com.example.heeder.Contract.Contract;
import com.example.heeder.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class StudentArrayAdapter extends ArrayAdapter {
    private SearchView searchView;
    private ImageView emptyView;

    public StudentArrayAdapter(@NonNull Context context, int resource,SearchView searchView,ImageView emptyView) {
        super(context, resource);
        this.searchView=searchView;
        this.emptyView=emptyView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null){
            convertView=LayoutInflater.from(getContext()).inflate(R.layout.student_list_item,parent,false);
        }
        ((TextView)convertView.findViewById(R.id.title_text_view)).setText(ClassHolder.getFilteredTitle(position));
        ((TextView)convertView.findViewById(R.id.faculty_name_text_view)).setText(ClassHolder.getFilteredfName(position));
//
//        ((TextView)convertView.findViewById(R.id.reg_status_text_view)).setText((MemberHolder.getFilteredMemberPhoto(position)==null)?"Not Registered":"Registered");
//


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contract.firebaseDatabase.getReference().child(String.valueOf(ClassHolder.getFilteredClassID(position))).child(Contract.STATUS).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot==null||snapshot.getValue()==null||snapshot.getValue().toString().equals(String.valueOf(Contract.INACTIVE_STATUS))){
                            Toast.makeText(getContext(),"Class is Inactive",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Intent intent=new Intent(getContext(),CameraActivity.class);
                            intent.putExtra(Contract.CLASS_ID,ClassHolder.getFilteredClassID(position));
                            intent.putExtra(Contract.CAMERA_MODE,Contract.VERIFICATON_MODE);
                            getContext().startActivity(intent);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return ClassHolder.classHolderFilteredArrayList.size();
    }

    public void FilterQuery(String newText){
        if(newText!=null){
            ClassHolder.classHolderFilteredArrayList.clear();
            if(newText.length()==0){
                ClassHolder.classHolderFilteredArrayList.addAll(ClassHolder.classHolderArrayList);
            }
            else{
                for(ClassHolder c:ClassHolder.classHolderArrayList){
                    if(c.getTitle().toLowerCase().contains(newText.toLowerCase())||c.getfName().toLowerCase().contains(newText.toLowerCase())){
                        ClassHolder.classHolderFilteredArrayList.add(c);
                    }
                }
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(ClassHolder.classHolderFilteredArrayList.size()>0){
                        emptyView.setVisibility(View.GONE);
                    }
                    else {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                    notifyDataSetChanged();
                }
            });

        }
    }
}
