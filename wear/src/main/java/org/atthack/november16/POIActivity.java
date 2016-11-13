package org.atthack.november16;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Interpretation;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import org.atthack.november16.data.POI;
import org.json.JSONObject;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.denley.courier.Courier;
import me.denley.courier.ReceiveMessages;

/**
 * Created by SWBRADSH on 11/12/2016.
 */

public class POIActivity extends Activity {
    private static final String TAG = "ATTHACK " + POIActivity.class.getSimpleName();

    public static final String EXTRA_TITLE = "title";

    TextView mTitle;
    Button btnAsk;
    Button btnPlay;
    FrameLayout frame;
    Node mNode;
    private Session speechSession;
    private Session speechSessionNLU;
    private Transaction ttsTransaction;

    private Transaction recoTransaction;

    private final BroadcastReceiver stopAlarm = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private GoogleApiClient client;


    class DismissAlarm extends Handler {
        @Override
        public void handleMessage(Message msg) {
            sendBroadcast(new Intent(Constants.STOP_ALARM));
            //do something here
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Courier.stopReceiving(this);
        unregisterReceiver(stopAlarm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.poi_layout);
        Courier.startReceiving(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        frame = (FrameLayout) findViewById(R.id.frame);

        mTitle = (TextView) findViewById(R.id.title);
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnAsk = (Button) findViewById(R.id.btn_ask);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(EXTRA_TITLE);
            if ((title != null) && (!title.isEmpty())) {
                mTitle.setText(title);
            }

        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("/play","");
                Log.i(TAG, "play");

            }
        });

        btnAsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ask");

            recognize();



            }
        });


        registerReceiver(stopAlarm, new IntentFilter(Constants.STOP_ALARM));

    }


    private void sendMessage(String msg, String data) {
        //Intent mServiceIntent = new Intent(this, SendToPhoneService.class);
        //mServiceIntent.setData(Uri.parse(msg));
        //this.startService(mServiceIntent);
        Courier.deliverMessage(this, msg, data);
    }

//
    @ReceiveMessages("/bitmap")
    public void onReceiveBitmap(POI point, String nodeId) {
        Asset asset = point.getBitmap();
        if (asset != null) {
            Log.i(TAG, "Got bitmap");
            Bitmap b = BitmapFactory.decodeByteArray(asset.getData(), 0, asset.getData().length);
            Drawable drawable = new BitmapDrawable(getResources(), b);
            frame.setBackground(drawable);
        }
    }

    private static final int SPEECH_REQUEST_CODE = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void recognize() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            sendMessage("/nlu",spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}


