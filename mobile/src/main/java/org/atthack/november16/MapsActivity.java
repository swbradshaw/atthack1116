package org.atthack.november16;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener, AudioPlayer.Listener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    boolean mapDataLoaded = false;
    boolean mapReady = true;
    String mapData = null;
    Marker center;
    JSONObject currentPOI = null;

    private Session speechSession;
    private Transaction ttsTransaction;
    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private Transaction recoTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String city = "Atlanta";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadEarcons();

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
        initTTS();
    }

    private void initTTS() {
        //Create a session
        speechSession = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);
        speechSession.getAudioPlayer().setListener(this);
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
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_center);

                    LatLng atlanta = new LatLng(33.751032, -84.396284);

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
        populateMap();
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);


    }


    @Override
    public void onCameraMove() {
        center.setPosition(mMap.getCameraPosition().target);

    }



    @Override
    public boolean onMarkerClick(final Marker marker) {

        String markerText = marker.getTitle();
        currentPOI = Util.getPOIFromMarker(MapsActivity.this.mapData, marker);
        //speak(markerText);

        //recognize();
//        mLastSelectedMarker = marker;
        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private void recognize() {
        //Setup our Reco transaction options.
        Transaction.Options options = new Transaction.Options();
        options.setDetection(DetectionType.Short);
        options.setLanguage(new Language("eng-USA"));
        options.setEarcons(startEarcon, stopEarcon, errorEarcon, null);

        //Add properties to appServerData for use with custom service. Leave empty for use with NLU.
        JSONObject appServerData = new JSONObject();
        //Start listening
        recoTransaction = speechSession.recognizeWithService("ATTHACK", appServerData, options, recoListener);
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
                ttsTransaction = null;
            }

            @Override
            public void onSuccess(Transaction transaction, String s) {
            }

            @Override
            public void onError(Transaction transaction, String s, TransactionException e) {
                //logs.append("\nonError: " + e.getMessage() + ". " + s);
                ttsTransaction = null;
            }
        });
    }

    private void interpretSpeech(JSONObject intent) {
        String key = null;
        if (intent.toString().contains("COST")) {
            // how much does it cost

        } else if (intent.toString().contains("HOURS")) {
            // what are the hours


        }

        if (key != null) {
            try {
                String speakText = this.currentPOI.get(key).toString();
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

    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }
}
