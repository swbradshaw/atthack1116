package org.atthack.november16;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by SWBRADSH on 11/12/2016.
 */

public class Util {

    public static JSONObject getPOIFromMarker(String poi, Marker marker) {
        try {
            return getPOIFromMarker(new JSONObject(poi), marker);
        } catch (Exception e) {}
        return null;
    }

    public static JSONObject getPOIFromMarker(JSONObject poi, Marker marker) {

        // find based on marker name
        try {


            JSONArray poiArray = poi.getJSONArray("poi");

            for (int i = 0; i < poiArray.length(); i++) {
                JSONObject point = poiArray.getJSONObject(i);
                if (point.get("name") == marker.getTitle()) {
                    return point;
                }
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }

}


