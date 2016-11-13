package org.atthack.november16;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import org.atthack.november16.data.POI;

/**
 * Created by SWBRADSH on 11/12/2016.
 */

public class OngoingNotificationListenerService extends WearableListenerService implements AudioPlayer.Listener {
    private static final String TAG = "ATTHACK " + OngoingNotificationListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 100;

    private GoogleApiClient mGoogleApiClient;

    private Session speechSession;


    private Transaction ttsTransaction;


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "Message received");
        if (messageEvent.getPath().equals("/play")) {
            Log.v(TAG, "play current poi");
            play();

        } else {
//
//            String title = messageEvent.getPath().substring("/poi/".length());
//            Log.v(TAG, "Starting POI: "+ title);
//            // Build the intent to display our custom notification
//            Intent notificationIntent = new Intent(this, POIActivity.class);
//            notificationIntent.putExtra(POIActivity.EXTRA_TITLE, title);
//            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(notificationIntent);

        }
    }

    private void initTTS() {
        //Create a session
        speechSession = Session.Factory.session(this, Configuration.SERVER_URI_TTS, Configuration.APP_KEY_TTS);
        speechSession.getAudioPlayer().setListener(this);
    }
    private void play() {
        initTTS();
        POI poi = ((App) this.getApplication()).lastPOI;
        speak(poi.getAudio());
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

    @Override
    public void onBeginPlaying(AudioPlayer audioPlayer, Audio audio) {

    }

    @Override
    public void onFinishedPlaying(AudioPlayer audioPlayer, Audio audio) {

    }
}



