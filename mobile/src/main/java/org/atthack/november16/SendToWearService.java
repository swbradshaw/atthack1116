package org.atthack.november16;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by SWBRADSH on 11/12/2016.
 */

public class SendToWearService  extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "AttHack " + SendToWearService.class.getSimpleName();

    public static final String KEY_TITLE = "title";
    public static final String KEY_TIME = "time";
    public static final String KEY_INSTANCE = "instance";

    private GoogleApiClient mGoogleApiClient;
    public SendToWearService() {
        super("SendToPhoneService");

    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        ConnectionResult connectionResult =   mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
        if (this.mGoogleApiClient.isConnected()) {
            NodeApi.GetConnectedNodesResult nodeResult=Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            Log.i(TAG, "Connected, sending " + dataString + " to wear");

            if (dataString.contains("dismiss")) {
                pushMessage(nodeResult.getNodes(), dataString);
            } else {
                Bundle bundle = workIntent.getExtras();
                String title = bundle.getString(KEY_TITLE);
                pushMessage(nodeResult.getNodes(), "/poi/" + title);
            }

        }
        Log.i(TAG, "service done.");
    }

    private void pushMessage(List<Node> nodes, String msg) {
        Log.i(TAG, "pushMessage");
        for (Node node : nodes) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient,
                    node.getId(), msg, msg.getBytes()).await();

            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "error sending message to wear "+ node.getDisplayName());
            } else {
                Log.i(TAG, "success sent to: " + node.getDisplayName());
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Could not connect to Phone: " + connectionResult.getErrorMessage());
    }
}