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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.example.heeder.CameraActivity;
import com.example.heeder.Contract.Contract;
import com.example.heeder.R;

public class AdminArrayAdapter extends ArrayAdapter {
    private SearchView searchView;
    private ImageView emptyView;

    public AdminArrayAdapter(@NonNull Context context, int resource,SearchView searchView,ImageView emptyView) {
        super(context, resource);
        this.searchView=searchView;
        this.emptyView=emptyView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null){
            convertView=LayoutInflater.from(getContext()).inflate(R.layout.admin_list_item,parent,false);
        }
        ((TextView)convertView.findViewById(R.id.id_text_view)).setText(MemberHolder.getFilteredUniqueID(position));
        ((TextView)convertView.findViewById(R.id.name_text_view)).setText(MemberHolder.getFilteredName(position));

        ((TextView)convertView.findViewById(R.id.reg_status_text_view)).setText((MemberHolder.getFilteredMemberPhoto(position)==null)?"Not Registered":"Registered");

        if(MemberHolder.getFilteredMemberPhoto(position)==null){
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getContext(), CameraActivity.class);
                    intent.putExtra(Contract.CAMERA_MODE,Contract.FACE_ADDING_MODE);
                    intent.putExtra(Contract.ID,MemberHolder.getFilteredMemberID(position));
                    getContext().startActivity(intent);
                }
            });
        }
        else{
            convertView.setOnClickListener(null);
        }


        return convertView;
    }

    @Override
    public int getCount() {
        return MemberHolder.memberHolderFilteredArrayList.size();
    }

    public void FilterQuery(String newText){
        if(newText!=null){
            MemberHolder.memberHolderFilteredArrayList.clear();
            if(newText.length()==0){
                MemberHolder.memberHolderFilteredArrayList.addAll(MemberHolder.memberHolderArrayList);
            }
            else{
                for(MemberHolder member:MemberHolder.memberHolderArrayList){
                    if(member.getName().toLowerCase().contains(newText.toLowerCase())||member.getUniqueID().toLowerCase().contains(newText.toLowerCase())){
                        MemberHolder.memberHolderFilteredArrayList.add(member);
                    }
                }
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(MemberHolder.memberHolderFilteredArrayList.size()>0){
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
