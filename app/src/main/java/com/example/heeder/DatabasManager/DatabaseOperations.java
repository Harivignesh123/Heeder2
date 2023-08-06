package com.example.heeder.DatabasManager;

import android.content.Context;
import android.util.Log;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.heeder.Contract.Contract;
import com.example.heeder.R;

import javax.xml.transform.Result;

public class DatabaseOperations extends DBcontract{

    private static String LOG_TAG= DatabaseOperations.class.getSimpleName();

    private static Connection getInstance(){
        Connection conn=null;
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(PHP_URL);
            Log.d(LOG_TAG,"phpMyAdmin connection successfull");
        } catch (Exception e) {
            Log.d(LOG_TAG,e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    public static ResultSet getMemberDetailsWithUniqueID(String uniqueID,int role){
        ResultSet rs=null;

        Connection conn=getInstance();
        if(conn==null){
            return rs;
        }
        String sql=null;
        if(role==STUDENT_ROLE){
            sql="SELECT * FROM "+STUDENTS+" WHERE "+S_UNQIUE_ID+"="+"?"+";";
        }
        else if(role==FACULTY_ROLE){
            sql="SELECT * FROM "+FACULTIES+" WHERE "+F_UNIQUE_ID+"="+"?"+";";
        }
        else{
            sql="SELECT * FROM "+ADMINS+" WHERE "+A_UNIQUE_ID+"="+"?"+";";
        }

        try {
            PreparedStatement stmt=conn.prepareStatement(sql);
            stmt.setObject(1,uniqueID);
            rs=stmt.executeQuery();

            if(rs==null){
                Log.d(LOG_TAG,"Result set is null");
            }
        } catch (SQLException e) {
            Log.d(LOG_TAG,"Problem with getting member details with UniqueID");
            e.printStackTrace();
        }


        return rs;
    }

    public static ResultSet getAllMemebersDetails(int role){
        ResultSet rs=null;

        Connection conn=getInstance();
        if(conn==null){
            return rs;
        }
        String sql=null;
        if(role==STUDENT_ROLE){
            sql="SELECT * FROM "+STUDENTS+";";
        }
        else if(role==FACULTY_ROLE){
            sql="SELECT * FROM "+FACULTIES+";";
        }
        else{
            sql="SELECT * FROM "+ADMINS+";";
        }

        try {
            PreparedStatement stmt=conn.prepareStatement(sql);
            rs=stmt.executeQuery();

            if(rs==null){
                Log.d(LOG_TAG,"Result set is null");
            }
        } catch (SQLException e) {
            Log.d(LOG_TAG,"Problem with getting all member details");
            e.printStackTrace();
        }


        return rs;
    }

    public static boolean registerMemberFace(int id,byte[] photoArray){

        boolean isSuccessful=false;

        Connection conn=getInstance();
        if(conn==null){
            return isSuccessful;
        }

        try{
            String sql="UPDATE "+STUDENTS+" SET "+PHOTO+"="+"?"+"WHERE "+S_ID+"="+"?"+";";
            PreparedStatement stmt=conn.prepareStatement(sql);
            stmt.setObject(1,photoArray);
            stmt.setObject(2,id);

            if(stmt.executeUpdate()==1){
                isSuccessful=true;
            }

        }catch (SQLException e){
            e.getMessage();
            e.printStackTrace();
        }
        catch (Exception e){
            e.getMessage();
            e.printStackTrace();
        }
        finally {
            DBclose(conn);
        }

        return isSuccessful;
    }

    public static ResultSet getAllSubjects(Context context,int userRole){
        ResultSet rs=null;

        Connection conn=getInstance();
        if(conn==null){
            return null;
        }

        try{

            String sql=null;
            String id=context.getSharedPreferences(context.getString(R.string.shared_preferences),Context.MODE_PRIVATE).getString(context.getString(R.string.id),null);
            if(userRole==STUDENT_ROLE){
                sql="SELECT "+CLASS_MASTER+"."+C_ID+","+CLASS_MASTER+"."+TITLE+","+CLASS_MASTER+"."+F_ID+","+FACULTIES+"."+NAME+" FROM "+CLASS_MASTER+" INNER JOIN "+CLASS_SET+" ON "+CLASS_MASTER+"."+C_ID+"="+CLASS_SET+"."+C_ID+" INNER JOIN "+FACULTIES+" ON "+CLASS_MASTER+"."+F_ID+"="+FACULTIES+"."+F_ID+" WHERE "+CLASS_SET+"."+S_ID+"="+id+";";
            }
            else {
                sql="SELECT * FROM "+CLASS_MASTER+" WHERE "+F_ID+"="+id+";";
            }
            PreparedStatement stmt=conn.prepareStatement(sql);
            rs=stmt.executeQuery();
            if(rs==null){
                Log.d(LOG_TAG,"Result set is null");
            }

        }
        catch(SQLException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return rs;
    }

    public static void insertAttendance(Context context,int cID){

        String sID=context.getSharedPreferences(context.getString(R.string.shared_preferences),Context.MODE_PRIVATE).getString(context.getString(R.string.id),null);

        Connection conn=getInstance();
        if(conn==null){
            return;
        }

        try{
            String sql="INSERT INTO "+ATTENDANCE_LOG+" VALUES (?,?,?);";
            PreparedStatement stmt=conn.prepareStatement(sql);

            stmt.setObject(1,null);
            stmt.setObject(2,cID);
            stmt.setObject(3,sID);

            if(stmt.execute()){
                Log.d(LOG_TAG,"Attendnace log inserted");
            }else{
                Log.d(LOG_TAG,"Attendnace log insertion unsuccessful");
            }


        }catch(SQLException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            DBclose(conn);
        }

    }

    public static void clearAttendanceLog(int cID){

        Connection conn=getInstance();
        if(conn==null){
            return;
        }

        try{
            String sql="DELETE FROM "+ATTENDANCE_LOG+" WHERE "+C_ID+"="+"?"+";";
            PreparedStatement stmt=conn.prepareStatement(sql);

            stmt.setObject(1,cID);

            stmt.execute();

        }catch(SQLException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            DBclose(conn);
        }

    }

    public static ResultSet getAttendanceLog(int cID){
        ResultSet rs=null;
        Connection conn=getInstance();
        if(conn==null){
            return rs;
        }

        try{
            String sql="SELECT Students.sUniqueID,COUNT(AttendanceLog.sID) FROM ClassSet LEFT JOIN AttendanceLog ON ClassSet.sID=AttendanceLog.sID INNER JOIN Students on ClassSet.sID=Students.sID WHERE ClassSet.cID=? GROUP BY ClassSet.sID;";
            PreparedStatement stmt=conn.prepareStatement(sql);
            stmt.setObject(1,cID);

            rs=stmt.executeQuery();
            if(rs==null){
                Log.d(LOG_TAG,"Null result set is retured");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return rs;

    }

    private static void DBclose(Connection conn){
        try{
            conn.close();
        }
        catch(SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}



