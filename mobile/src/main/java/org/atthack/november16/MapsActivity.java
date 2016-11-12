package org.atthack.november16;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    boolean mapDataLoaded = false;
    boolean mapReady = true;
    String mapData = null;

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
                        JSONObject obj = new JSONObject(MapsActivity.this.mapData);
                        JSONArray pois = obj.getJSONArray("poi");
                        for (int i = 0; i < pois.length(); ++i) {
                            JSONObject point = pois.getJSONObject(i);
                            LatLng pointPos = new LatLng(point.getDouble("lat"), point.getDouble("lng"));
                            mMap.addMarker(new MarkerOptions().position(pointPos).title(point.getString("name")));
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
        populateMap();

    }
}
