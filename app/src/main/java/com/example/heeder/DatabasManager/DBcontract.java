package com.example.heeder.DatabasManager;

public class DBcontract {

    //phpMyAdmin
    private static final String PHP_HOST="bzwyuduekp9bw7o78yxj-mysql.services.clever-cloud.com:3306";
    private static final String PHP_USERNAME="uv5zgmasyhyaf1vr";
    private static final String PHP_PASSWORD="OAwqw7ippC71ICvX3s2t";
    private static final String PHP_DATABASE="bzwyuduekp9bw7o78yxj";

    protected static final String PHP_URL="jdbc:mysql://"+PHP_HOST+"/"+PHP_DATABASE+"?"+"user="+PHP_USERNAME+"&password="+PHP_PASSWORD;

    //Tables
    protected static final String STUDENTS="Students";
    protected static final String FACULTIES="Faculties";
    protected static final String ADMINS="Admins";
    protected static final String CLASS_MASTER="ClassMaster";
    protected static final String CLASS_SET="ClassSet";
    protected static final String ATTENDANCE_LOG="AttendanceLog";

    //Columns
    protected static final String S_ID="sID";
    protected static final String S_UNQIUE_ID="sUniqueID";
    protected static final String NAME="Name";
    protected static final String MAIL_ID="MailID";
    protected static final String PHOTO="Photo";
    protected static final String F_ID="fID";
    protected static final String F_UNIQUE_ID="fUniqueID";
    protected static final String A_ID="aID";
    protected static final String A_UNIQUE_ID="aUniqueID";
    protected static final String C_ID="cID";
    protected static final String TITLE="Title";
    protected static final String L_ID="lID";

    //Roles
    public static final int ADMIN_ROLE=0;
    public static final int FACULTY_ROLE=1;
    public static final int STUDENT_ROLE=2;


}
