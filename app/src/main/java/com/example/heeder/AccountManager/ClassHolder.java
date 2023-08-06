package com.example.heeder.AccountManager;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ClassHolder {
    private int classID;
    private String title;
    private int fID;
    private String fName;

    public static List<ClassHolder> classHolderArrayList=new ArrayList<ClassHolder>();
    public static ArrayList<ClassHolder> classHolderFilteredArrayList=new ArrayList<ClassHolder>();

    public ClassHolder(int classID,String title,int fID,String fName){
        this.classID=classID;
        this.title=title;
        this.fID=fID;
        this.fName=fName;
    }

    public int getClassID(){
        return classID;
    }

    public String getTitle(){
        return title;
    }
    public int getfID(){
        return fID;
    }

    public String getfName(){
        return fName;
    }


    public static int getFilteredClassID(int p){
        return classHolderFilteredArrayList.get(p).getClassID();
    }
    public static String getFilteredTitle(int p){
        return classHolderFilteredArrayList.get(p).getTitle();
    }
    public static int getFilteredfID(int p){
        return classHolderFilteredArrayList.get(p).getfID();
    }
    public static String getFilteredfName(int p){
        return classHolderFilteredArrayList.get(p).getfName();
    }



    public static void updateClassID(int p,int s){
        int index=classHolderFilteredArrayList.indexOf(classHolderFilteredArrayList.get(p));
        classHolderFilteredArrayList.get(index).classID=s;
    }
    public static void updateMemberTitle(int p,String s){
        int index=classHolderFilteredArrayList.indexOf(classHolderFilteredArrayList.get(p));
        classHolderFilteredArrayList.get(index).title=s;
    }
    public static void updatefID(int p,int s){
        int index=classHolderFilteredArrayList.indexOf(classHolderFilteredArrayList.get(p));
        classHolderFilteredArrayList.get(index).fID=s;
    }
    public static void updatefName(int p,String s){
        int index=classHolderFilteredArrayList.indexOf(classHolderFilteredArrayList.get(p));
        classHolderFilteredArrayList.get(index).fName=s;
    }

    public static void deleteClass(int p){
        classHolderFilteredArrayList.remove(classHolderFilteredArrayList.get(p));
    }

    public static void clearAll(){
        classHolderArrayList.clear();
        classHolderFilteredArrayList.clear();
    }
}
