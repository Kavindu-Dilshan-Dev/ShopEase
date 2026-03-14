package com.kavindu.shopeaseapp.utils;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class DirectionsParser {

    public static List<LatLng> parse(JSONObject json) throws Exception {
        List<LatLng> path = new ArrayList<>();
        JSONArray routes = json.getJSONArray("routes");
        if (routes.length() == 0) return path;

        JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
        JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

        for (int i = 0; i < steps.length(); i++) {
            String encoded = steps.getJSONObject(i)
                    .getJSONObject("polyline").getString("points");
            path.addAll(decodePolyline(encoded));
        }
        return path;
    }

    private static List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; }
            while (b >= 0x20);
            int dLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dLat;
            shift = 0; result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; }
            while (b >= 0x20);
            int dLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dLng;
            poly.add(new LatLng(lat / 1e5, lng / 1e5));
        }
        return poly;
    }
}