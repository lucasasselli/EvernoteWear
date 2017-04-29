package com.lucasasselli.evernotewear;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This object contains all commonly used functions
 */

public class Utils {
    public final static int NO_CONNECTION = 0;
    public final static int MOBILE = 1;
    public final static int WIFI = 2;
    public final static int OTHER = 3;

    public static int getInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo!=null){
            if(netInfo.getType()==ConnectivityManager.TYPE_MOBILE) {
                return MOBILE;
            }else if(netInfo.getType()==ConnectivityManager.TYPE_WIFI) {
                return WIFI;
            }else {
                return OTHER;
            }
        }else{
            return NO_CONNECTION;
        }
    }
}
