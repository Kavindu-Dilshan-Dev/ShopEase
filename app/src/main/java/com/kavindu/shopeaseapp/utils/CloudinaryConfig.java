package com.kavindu.shopeaseapp.utils;

import android.content.Context;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {

    private static boolean initialized = false;

    // ✅ Replace with your actual values from Cloudinary dashboard
    private static final String CLOUD_NAME = "dfbqbcvzo";
    private static final String API_KEY    = "775656859665978";
    private static final String API_SECRET = "";

    public static void init(Context context) {
        if (!initialized) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key",    API_KEY);
            config.put("api_secret", API_SECRET);
            MediaManager.init(context, config);
            initialized = true;
        }
    }
}