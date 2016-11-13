package org.atthack.november16;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.atthack.november16.data.POI;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import me.denley.courier.Packager;
import me.denley.courier.ReceiveMessages;

/**
 * Created by SWBRADSH on 11/12/2016.
 */

public class OngoingNotificationListenerService extends WearableListenerService {
    private static final String TAG = "ATTHACK " + OngoingNotificationListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 100;

    private GoogleApiClient mGoogleApiClient;
//


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "Message received");
        if (messageEvent.getPath().equals(Constants.PATH_DISMISS)) {
            Log.v(TAG, "Sending broadcast to dismiss: "+ messageEvent.getPath());
            sendBroadcast(new Intent(Constants.STOP_ALARM));
        } else {

            String title = messageEvent.getPath().substring("/poi/".length());
            //POI poi = Packager.unpack(this, messageEvent.getData(), POI.class);
            Log.v(TAG, "Starting POI: "+ title);
            // Build the intent to display our custom notification
            Intent notificationIntent = new Intent(this, POIActivity.class);
            notificationIntent.putExtra(POIActivity.EXTRA_TITLE, title);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(notificationIntent);

        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(15000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

}
