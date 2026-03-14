package com.kavindu.shopeaseapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver {

    public interface NetworkCallback {
        void onNetworkChanged(boolean isConnected);
    }

    private static NetworkCallback callback;

    public static void setCallback(NetworkCallback cb) { callback = cb; }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = isConnected(context);
        if (callback != null) callback.onNetworkChanged(connected);
        if (!connected)
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}