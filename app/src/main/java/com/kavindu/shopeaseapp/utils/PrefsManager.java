package com.kavindu.shopeaseapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {

    private static final String PREF_NAME = "ShopEasePrefs";
    private static final String KEY_USER_ID        = "user_id";
    private static final String KEY_USER_NAME      = "user_name";
    private static final String KEY_USER_EMAIL     = "user_email";
    private static final String KEY_IS_LOGGED_IN   = "is_logged_in";
    private static final String KEY_NOTIFICATIONS  = "notifications_enabled";
    private static final String KEY_DARK_MODE      = "dark_mode";
    private static final String KEY_SORT_PREF      = "sort_preference";
    private static final String KEY_DELIVERY_ADDR  = "delivery_address";

    private final SharedPreferences prefs;
    private static PrefsManager instance;

    private PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PrefsManager getInstance(Context context) {
        if (instance == null) instance = new PrefsManager(context.getApplicationContext());
        return instance;
    }

    public void saveLoginState(String uid, String name, String email) {
        prefs.edit()
                .putString(KEY_USER_ID, uid)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() { return prefs.getBoolean(KEY_IS_LOGGED_IN, false); }
    public String getUserId() { return prefs.getString(KEY_USER_ID, ""); }
    public String getUserName() { return prefs.getString(KEY_USER_NAME, ""); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, ""); }
    public boolean isNotificationsEnabled() { return prefs.getBoolean(KEY_NOTIFICATIONS, true); }
    public void setNotifications(boolean enabled) { prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply(); }
    public boolean isDarkMode() { return prefs.getBoolean(KEY_DARK_MODE, false); }
    public void setDarkMode(boolean enabled) { prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply(); }
    public String getSortPreference() { return prefs.getString(KEY_SORT_PREF, "default"); }
    public void setSortPreference(String sort) { prefs.edit().putString(KEY_SORT_PREF, sort).apply(); }
    public String getDeliveryAddress() { return prefs.getString(KEY_DELIVERY_ADDR, ""); }
    public void setDeliveryAddress(String address) { prefs.edit().putString(KEY_DELIVERY_ADDR, address).apply(); }
}