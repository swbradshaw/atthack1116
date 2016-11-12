package org.atthack.november16;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.atthack.november16.data.POI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    boolean mapDataLoaded = false;
    boolean mapReady = false;
    String mapData = null;
    HashMap<Marker, POI> markerPOIMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String city = "Atlanta";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        OkHttpClient client = new OkHttpClient();
        final String cityJSON = "https://s3.amazonaws.com/atthack1116/" + city.toLowerCase() + ".json";
        Request request = new Request.Builder()
                .url(cityJSON)
                .get()
                .build();

        try {
            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    MapsActivity.this.mapDataLoaded = true;
                    MapsActivity.this.mapData = response.body().string();
                    populateMap();
                }
            });
        }
        catch (Exception e) {

        }
    }

    public void populateMap() {
        if ((this.mapDataLoaded) && (this.mapReady)) {

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        markerPOIMap = new HashMap<Marker, POI>();
                        JSONObject obj = new JSONObject(MapsActivity.this.mapData);
                        JSONArray pois = obj.getJSONArray("poi");
                        for (int i = 0; i < pois.length(); ++i) {
                            //JSONObject point = pois.getJSONObject(i);
                            POI point = new POI(pois.getJSONObject(i));
                            LatLng pointPos = new LatLng(point.getLat(), point.getLng());
                            Marker m = mMap.addMarker(new MarkerOptions().position(pointPos).title(point.getName()));
                            markerPOIMap.put(m, point);
                        }
                    } catch (JSONException e) {}
                    LatLng atlanta = new LatLng(33.751032, -84.396284);
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(atlanta));
                    mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(atlanta, 13.0f ) );
                }
            });


        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        this.mapReady = true;

        mMap.setInfoWindowAdapter(new POIInfoWindowAdapter());
        populateMap();

    }

    class POIInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        private final View mContents;

        POIInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.poi_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.poi_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            POI poi = markerPOIMap.get(marker);
            render(marker, mWindow, poi);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            POI poi = markerPOIMap.get(marker);
            render(marker, mContents, poi);
            return mContents;
        }

        private void render(Marker marker, View view, POI poi) {
            int badge;
            badge = 0;
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);


            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = poi.getDescription();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }
}
