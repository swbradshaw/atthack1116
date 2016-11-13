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

import java.util.concurrent.TimeUnit;

/**
 * Created by SWBRADSH on 11/12/2016.
 */

public class SendToPhoneService  extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "TetraAlarm " + SendToPhoneService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    public SendToPhoneService() {
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

            Node mNode= null;
            NodeApi.GetConnectedNodesResult nodeResult=Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodeResult.getNodes()) {
                if (node != null && node.isNearby()) {
                    mNode = node;
                    Log.d(TAG, "Connected to: " + mNode.getDisplayName());
                }
            }
            if (mNode != null) {
                Log.i(TAG, "Sending " + dataString + " to phone");
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient,
                        mNode.getId(), dataString, dataString.getBytes()).await();

                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "error sending message to phone");
                } else {
                    Log.i(TAG, "success sent to: " + mNode.getDisplayName());
                }
            }
        }
        Log.i(TAG, "service done.");
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