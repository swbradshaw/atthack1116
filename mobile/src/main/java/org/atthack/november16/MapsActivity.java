package org.atthack.november16;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Interpretation;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import org.atthack.november16.data.POI;
import org.atthack.november16.fragment.DetailFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.denley.courier.Courier;
import me.denley.courier.ReceiveMessages;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapsActivity extends FragmentActivity implements DetailFragment.OnFragmentInteractionListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener, AudioPlayer.Listener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "ATTHACK " + MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    boolean mapDataLoaded = false;
    boolean mapReady = false;
    boolean overlayShown = false;
    String mapData = null;
    Marker center;
    POI currentPOI = null;
    Location lastLocation;

    private Session speechSession;
    private Session speechSessionNLU;
    private Transaction ttsTransaction;
    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;
    private Transaction speechTrans;
    private String language = "eng-USA";

    public static final String KEY_TITLE = "title";
    public static final float PIN_SHOW_DIST = 50; // Distance in meters that a pin will pop up automatically


    private Transaction recoTransaction;
    HashMap<Marker, POI> markerPOIMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Courier.startReceiving(this);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadEarcons();
        Intent intent = getIntent();
        String city = intent.getStringExtra("city");
        if (city == null) {
             city = "atlanta.json";
        }
        if (intent.getStringExtra("language") != null) {
            language = intent.getStringExtra("language");
        }
        OkHttpClient client = new OkHttpClient();
        final String cityJSON = "https://s3.amazonaws.com/atthack1116/" + city.toLowerCase();
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
        initTTS();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        Courier.stopReceiving(this);
    }

    private void initTTS() {
        //Create a session
        speechSession = Session.Factory.session(this, Configuration.SERVER_URI_TTS, Configuration.APP_KEY_TTS);
        speechSession.getAudioPlayer().setListener(this);
        speechSessionNLU = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);
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
                            float iconColor;
                            switch(point.getType()) {
                                case "Attractions":
                                    iconColor = BitmapDescriptorFactory.HUE_BLUE;
                                    break;
                                case "Sports":
                                    iconColor = BitmapDescriptorFactory.HUE_RED;
                                    break;
                                case "Parks":
                                    iconColor = BitmapDescriptorFactory.HUE_GREEN;
                                    break;
                                case "Quirky":
                                    iconColor = BitmapDescriptorFactory.HUE_MAGENTA;
                                    break;
                                default:
                                    iconColor = BitmapDescriptorFactory.HUE_ORANGE;
                            }
                            Marker m = mMap.addMarker(new MarkerOptions().position(pointPos)
                                .icon(BitmapDescriptorFactory.defaultMarker(iconColor)));//.title(point.getName()));
                            markerPOIMap.put(m, point);
                        }
                    } catch (JSONException e) {}
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_center);

                    LatLng atlanta = new LatLng(33.751032, -84.396284);
                    Location atlLoc = new Location("");
                    atlLoc.setLatitude(atlanta.latitude);
                    atlLoc.setLongitude(atlanta.longitude);
                    lastLocation = atlLoc;


                    center = mMap.addMarker(new MarkerOptions().position(atlanta).icon(icon).anchor(0.5f,0.5f));

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

        //mMap.setInfoWindowAdapter(new POIInfoWindowAdapter());
        populateMap();
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_retro);
        mMap.setMapStyle(style);


    }


    @Override
    public void onCameraMove() {
        LatLng pos = mMap.getCameraPosition().target;
        center.setPosition(pos);

        Location location =  new Location("");
        location.setLatitude(pos.latitude);
        location.setLongitude(pos.longitude);
        if (!overlayShown && lastLocation != null && location.distanceTo(lastLocation) > PIN_SHOW_DIST / 2) {
            checkNearbyPOIs(location);
            lastLocation = location;
        }
    }

    private void checkNearbyPOIs(Location location) {
        Location pinLocation =  new Location("");
        float closestDist = PIN_SHOW_DIST;
        Marker closest = null;

        Iterator it = markerPOIMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            POI current = (POI)pair.getValue();
            pinLocation.setLatitude(current.getLat());
            pinLocation.setLongitude(current.getLng());
            float distance = location.distanceTo(pinLocation);
            if (current != currentPOI && distance < PIN_SHOW_DIST){
                if (distance < closestDist) {
                    Marker pin = (Marker)pair.getKey();
                    closest = pin;
                    closestDist = distance;
                }
            }
        }
        if (closest != null) {
            onMarkerClick(closest);
        }
    }



    private void sendWear(String title) {
        Intent mServiceIntent = new Intent(this, SendToWearService.class);
        mServiceIntent.setData(Uri.parse("/poi"));
        mServiceIntent.putExtra(KEY_TITLE, title);
        this.startService(mServiceIntent);
    }


    @Override
    public void onDismiss() {
        //Fragment dialog had been dismissed
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    speechSession.getAudioPlayer().stop();

                } catch (Exception e) {

                }
            }
        });

        overlayShown = false;
        Intent mServiceIntent = new Intent(this, SendToWearService.class);
        mServiceIntent.setData(Uri.parse("/dismiss"));
        this.startService(mServiceIntent);


    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        currentPOI = markerPOIMap.get(marker);//Util.getPOIFromMarker(MapsActivity.this.mapData, marker);
        if (currentPOI != null) {
            ((App)this.getApplication()).lastPOI = currentPOI;
            overlayShown = true;
            FragmentManager fm = getSupportFragmentManager();
            DetailFragment dialog = new DetailFragment();
            dialog.setData(currentPOI);
            dialog.show(fm, "detail");


            sendWear(currentPOI.getName());
            Courier.deliverMessage(this, "/bitmap", currentPOI.getBitmap());

        }
        //speak(markerText);




        //recognize();
//        mLastSelectedMarker = marker;
        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private void recognize(String textInput) {
        //Setup our Reco transaction options.
        Transaction.Options options = new Transaction.Options();
        options.setDetection(DetectionType.Short);
        options.setLanguage(new Language(this.language));
        options.setEarcons(startEarcon, stopEarcon, errorEarcon, null);

        //Add properties to appServerData for use with custom service. Leave empty for use with NLU.

        JSONObject appServerData = new JSONObject();
        try {
            if (textInput != null) {
                appServerData.put("message", textInput);
            }
        } catch (Exception e) {}
        //Start listening
        recoTransaction = speechSessionNLU.recognizeWithService("ATTHACK", appServerData, options, recoListener);
    }

    private void speak(String ttsText) {
        //Setup our TTS transaction options.
        Transaction.Options options = new Transaction.Options();
        options.setLanguage(new Language("eng-USA"));
        //options.setVoice(new Voice(Voice.SAMANTHA)); //optionally change the Voice of the speaker, but will use the default if omitted.

        //Start a TTS transaction
        ttsTransaction = speechSession.speakString(ttsText, options, new Transaction.Listener() {
            @Override
            public void onAudio(Transaction transaction, Audio audio) {
                Log.i(TAG, "onAudio");
                ttsTransaction = null;
            }

            @Override
            public void onSuccess(Transaction transaction, String s) {
                speechTrans = transaction;
            }

            @Override
            public void onError(Transaction transaction, String s, TransactionException e) {
                //logs.append("\nonError: " + e.getMessage() + ". " + s);
                ttsTransaction = null;
            }
        });

    }

    private void interpretSpeech(JSONObject intent) {

        String speakText = null;
        if (intent.toString().contains("COST")) {
            // how much does it cost
            speakText = currentPOI.getCost();

        } else if (intent.toString().contains("Hours")) {
            // what are the hours
            speakText = currentPOI.getHours();

        }

        if (speakText != null) {
            try {
                speak(speakText);

            } catch (Exception e) {}
        }
    }
    @Override
    public void onBeginPlaying(AudioPlayer audioPlayer, Audio audio) {

    }

    @Override
    public void onFinishedPlaying(AudioPlayer audioPlayer, Audio audio) {

    }


    private void loadEarcons() {
        //Load all of the earcons from disk
        startEarcon = new Audio(this, R.raw.sk_start, Configuration.PCM_FORMAT);
        stopEarcon = new Audio(this, R.raw.sk_stop, Configuration.PCM_FORMAT);
        errorEarcon = new Audio(this, R.raw.sk_error, Configuration.PCM_FORMAT);
    }


    /* Audio Level Polling */

    private Handler handler = new Handler();

    /**
     * Every 50 milliseconds we should update the volume meter in our UI.
     */
    private Runnable audioPoller = new Runnable() {
        @Override
        public void run() {
            float level = recoTransaction.getAudioLevel();
            //volumeBar.setProgress((int)level);
            handler.postDelayed(audioPoller, 50);
        }
    };

    /**
     * Start polling the users audio level.
     */
    private void startAudioLevelPoll() {
        audioPoller.run();
    }

    /**
     * Stop polling the users audio level.
     */
    private void stopAudioLevelPoll() {
        handler.removeCallbacks(audioPoller);
        //volumeBar.setProgress(0);
    }


    private Transaction.Listener recoListener = new Transaction.Listener() {
        @Override
        public void onStartedRecording(Transaction transaction) {
            //logs.append("\nonStartedRecording");

            //We have started recording the users voice.
            //We should update our state and start polling their volume.
            setState(State.LISTENING);
            startAudioLevelPoll();
        }

        @Override
        public void onFinishedRecording(Transaction transaction) {
            //logs.append("\nonFinishedRecording");

            //We have finished recording the users voice.
            //We should update our state and stop polling their volume.
            setState(State.PROCESSING);
            stopAudioLevelPoll();
        }

        @Override
        public void onServiceResponse(Transaction transaction, org.json.JSONObject response) {
            try {
                // 2 spaces for tabulations.
                //logs.append("\nonServiceResponse: " + response.toString(2));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(MapsActivity.this, "onServiceResponse", Toast.LENGTH_SHORT).show();
            // We have received a service response. In this case it is our NLU result.
            // Note: this will only happen if you are doing NLU (or using a service)
        }

        @Override
        public void onRecognition(Transaction transaction, Recognition recognition) {
            //logs.append("\nonRecognition: " + recognition.getText());
            //Toast.makeText(MapsActivity.this, recognition.getText(), Toast.LENGTH_SHORT).show();
            //We have received a transcription of the users voice from the server.
        }

        @Override
        public void onInterpretation(Transaction transaction, Interpretation interpretation) {
            try {
                //logs.append("\nonInterpretation: " + interpretation.getResult().toString(2));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Toast.makeText(MapsActivity.this, "onInterpretation", Toast.LENGTH_SHORT).show();

            interpretSpeech(interpretation.getResult());
            // We have received a service response. In this case it is our NLU result.
            // Note: this will only happen if you are doing NLU (or using a service)
        }

        @Override
        public void onSuccess(Transaction transaction, String s) {
            //logs.append("\nonSuccess");
            Toast.makeText(MapsActivity.this, "onSuccess", Toast.LENGTH_SHORT).show();
            //Notification of a successful transaction.
            setState(State.IDLE);
        }

        @Override
        public void onError(Transaction transaction, String s, TransactionException e) {
            //logs.append("\nonError: " + e.getMessage() + ". " + s);
            Toast.makeText(MapsActivity.this, "onError", Toast.LENGTH_SHORT).show();
            //Something went wrong. Check Configuration.java to ensure that your settings are correct.
            //The user could also be offline, so be sure to handle this case appropriately.
            //We will simply reset to the idle state.
            setState(State.IDLE);
        }
    };


    /**
     * Set the state and update the button text.
     */
    private void setState(State newState) {
//        state = newState;
//        switch (newState) {
//            case IDLE:
//                toggleReco.setText(getResources().getString(R.string.recognize_with_service));
//                break;
//            case LISTENING:
//                toggleReco.setText(getResources().getString(R.string.listening));
//                break;
//            case PROCESSING:
//                toggleReco.setText(getResources().getString(R.string.processing));
//                break;
//        }
    }

    @Override
    public void setBitmap(Bitmap b) {
        currentPOI.setBitmap(Util.createAssetFromBitmap(b));
    }

    @Override
    public void onPlay() {
        speak(this.currentPOI.getAudio());
    }

    @ReceiveMessages("/play")
    public void onWearPlay(String smsMessage, String nodeId) {
        speak(this.currentPOI.getAudio());
    }

    @ReceiveMessages("/nlu")
    public void onWearNluText(String text, String nodeId) {
       recognize(text);

    }

    @Override
    public void onSpeech() {
        recognize(null);
    }

    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
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
