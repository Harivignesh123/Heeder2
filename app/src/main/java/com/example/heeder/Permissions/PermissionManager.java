package com.example.heeder.Permissions;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;

public class PermissionManager {
    private static final String LOG_TAG=PermissionManager.class.getSimpleName();


    public static boolean checkInternetConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        NetworkCapabilities networkCapabilities=null;
        int downspeed=0;
        int upspeed=0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            networkCapabilities=connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if(networkCapabilities!=null){
                downspeed=networkCapabilities.getLinkDownstreamBandwidthKbps();
                upspeed=networkCapabilities.getLinkUpstreamBandwidthKbps();
                if(downspeed>10 && upspeed>5){
                    Log.v(LOG_TAG,"Internet is available");
                    return true;
                }
                else {
                    Log.v(LOG_TAG,"Internet is too slow");
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            if ( (networkInfo != null) && (networkInfo.isConnected())){
                return true;

            } else {
                Log.v(LOG_TAG,"No Internet");
                return  false;
            }
        }


    }
}
