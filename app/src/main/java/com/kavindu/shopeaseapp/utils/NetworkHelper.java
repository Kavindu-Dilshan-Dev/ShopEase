package com.kavindu.shopeaseapp.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NetworkHelper {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private static final String BASE_URL = "https://firestore.googleapis.com/v1/projects/shopease-4f4b8/databases/(default)/documents/";;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public static void get(String endpoint, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        mainHandler.post(() -> callback.onSuccess(json));
                    } catch (Exception ex) {
                        mainHandler.post(() -> callback.onError("Parse error"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("HTTP " + response.code()));
                }
            }
        });
    }

    public static void post(String endpoint, JSONObject body, Callback callback) {
        RequestBody rb = RequestBody.create(
                body.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(rb)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        mainHandler.post(() -> callback.onSuccess(json));
                    } catch (Exception ex) {
                        mainHandler.post(() -> callback.onError("Parse error"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("HTTP " + response.code()));
                }
            }
        });
    }
}