package org.atthack.november16;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.wearable.Asset;
import com.google.maps.android.SphericalUtil;

import org.atthack.november16.data.POI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * Created by SWBRADSH on 11/12/2016.
 */

public class Util {

    public static int getCount(JSONObject points, int Distance, String type, LatLng currentPos) {
        int count = 0;
        try {

            JSONArray pois = points.getJSONArray("poi");
            for (int i = 0; i < pois.length(); ++i) {

                LatLng pointPos = new LatLng(pois.getJSONObject(i).getDouble("lat"), pois.getJSONObject(i).getDouble("lng"));
                String sType = pois.getJSONObject(i).getString("type");
               if (sType.equalsIgnoreCase(type)) {
                   //if (CalculationByDistance(currentPos, pointPos) < Distance) {
                   if (SphericalUtil.computeDistanceBetween(currentPos, pointPos) < Distance) {
                       count++;
                   }
               }

            }
        } catch (Exception e) {}

        return count;
    }

    public static int CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
//        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
//                + " Meter   " + meterInDec);
//
//        return Radius * c;
        return meterInDec;
    }
    public static JSONObject getPOIFromMarker(String poi, Marker marker) {
        try {
            return getPOIFromMarker(new JSONObject(poi), marker);
        } catch (Exception e) {}
        return null;
    }

    public static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    public static JSONObject getPOIFromMarker(JSONObject poi, Marker marker) {

        // find based on marker name
        try {


            JSONArray poiArray = poi.getJSONArray("poi");

            for (int i = 0; i < poiArray.length(); i++) {
                JSONObject point = poiArray.getJSONObject(i);
                if (point.get("name").equals(marker.getTitle())) {
                    return point;
                }
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }

}


