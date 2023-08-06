package com.example.heeder.AccountManager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.example.heeder.CameraActivity;
import com.example.heeder.Contract.Contract;
import com.example.heeder.R;

public class FacultyArrayAdapter extends ArrayAdapter {

    private final String LOG_TAG=FacultyArrayAdapter.class.getSimpleName();
    private SearchView searchView;
    private ImageView emptyView;

    public FacultyArrayAdapter(@NonNull Context context, int resource,SearchView searchView,ImageView emptyView) {
        super(context, resource);
        this.searchView=searchView;
        this.emptyView=emptyView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null){
            convertView=LayoutInflater.from(getContext()).inflate(R.layout.faculty_list_item,parent,false);
        }

        ((TextView)convertView.findViewById(R.id.title_text_view)).setText(ClassHolder.getFilteredTitle(position));
        //((TextView)convertView.findViewById(R.id.faculty_name_text_view)).setText(ClassHolder.getFilteredfName(position));

//        ((TextView)convertView.findViewById(R.id.id_text_view)).setText(MemberHolder.getFilteredUniqueID(position));
//        ((TextView)convertView.findViewById(R.id.name_text_view)).setText(MemberHolder.getFilteredName(position));
//
//        ((TextView)convertView.findViewById(R.id.reg_status_text_view)).setText((MemberHolder.getFilteredMemberPhoto(position)==null)?"Not Registered":"Registered");
//
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(),FacultyControlActivity.class);
                intent.putExtra(Contract.CLASS_ID,ClassHolder.getFilteredClassID(position));
                getContext().startActivity(intent);
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
                    if(c.getTitle().toLowerCase().contains(newText.toLowerCase())){
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

