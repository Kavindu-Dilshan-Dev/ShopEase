package com.kavindu.shopeaseapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.content.Context;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TelephonyHelper {

    public static final String SUPPORT_PHONE = "+94701961081";
    public static final int CALL_PERMISSION_CODE = 200;

    public static void callSupport(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + SUPPORT_PHONE));
            activity.startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_CODE);
        }
    }

    public static void dialSupport(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + SUPPORT_PHONE));
        activity.startActivity(intent);
    }

    public static void sendSms(Activity activity, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse("smsto:" + SUPPORT_PHONE));
        intent.putExtra("sms_body", message);
        activity.startActivity(intent);
    }

    public static String getNetworkOperator(Context context) {
        TelephonyManager tm = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getNetworkOperatorName() : "Unknown";
    }
}