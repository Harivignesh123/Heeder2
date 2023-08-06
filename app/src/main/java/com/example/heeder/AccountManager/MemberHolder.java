package com.example.heeder.AccountManager;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class MemberHolder {

    private int memberID;
    private String uniqueID;
    private String name;
    private String mailID;
    private Blob photoBlob;

    public static List<MemberHolder> memberHolderArrayList=new ArrayList<MemberHolder>();
    public static ArrayList<MemberHolder> memberHolderFilteredArrayList=new ArrayList<>();

    public MemberHolder(int memberID,String uniqueID,String name,String mailID,Blob photoBlob){
        this.memberID=memberID;
        this.uniqueID=uniqueID;
        this.name=name;
        this.mailID=mailID;
        this.photoBlob=photoBlob;
    }

    public int getMemberID(){
        return memberID;
    }
    public String getUniqueID(){
        return uniqueID;
    }
    public String getName(){
        return name;
    }

    public String getMailID(){
        return mailID;
    }


    public Blob getMemberPhoto(){
        return photoBlob;
    }



    public static int getFilteredMemberID(int p){
        return memberHolderFilteredArrayList.get(p).getMemberID();
    }
    public static String getFilteredUniqueID(int p){
        return memberHolderFilteredArrayList.get(p).getUniqueID();
    }
    public static String getFilteredName(int p){
        return memberHolderFilteredArrayList.get(p).getName();
    }

    public static String getFilteredMailID(int p){
        return memberHolderFilteredArrayList.get(p).getMailID();
    }
    public static Blob getFilteredMemberPhoto(int p){
        return memberHolderFilteredArrayList.get(p).getMemberPhoto();
    }



    public static void updateMemberID(int p,int s){
        int index=memberHolderArrayList.indexOf(memberHolderFilteredArrayList.get(p));
        memberHolderArrayList.get(index).memberID=s;
    }
    public static void updateMemberUniqueID(int p,String s){
        int index=memberHolderArrayList.indexOf(memberHolderFilteredArrayList.get(p));
        memberHolderArrayList.get(index).uniqueID=s;
    }
    public static void updateName(int p,String s){
        int index=memberHolderArrayList.indexOf(memberHolderFilteredArrayList.get(p));
        memberHolderArrayList.get(index).name=s;
    }
    public static void updateMailID(int p,String s){
        int index=memberHolderArrayList.indexOf(memberHolderFilteredArrayList.get(p));
        memberHolderArrayList.get(index).mailID=s;
    }
    public static void updateImageURL(int p,Blob s){
        int index=memberHolderArrayList.indexOf(memberHolderFilteredArrayList.get(p));
        memberHolderArrayList.get(index).photoBlob=s;
    }

    public static void deletePerson(int p){
        memberHolderArrayList.remove( memberHolderFilteredArrayList.get(p));
    }

    public static void clearAll(){
        memberHolderArrayList.clear();
        memberHolderFilteredArrayList.clear();
    }


}
