package org.atthack.november16;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

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
            Log.v(TAG, "Starting POI: "+ title);
            // Build the intent to display our custom notification
            Intent notificationIntent = new Intent(this, POIActivity.class);
            notificationIntent.putExtra(POIActivity.EXTRA_TITLE, title);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(notificationIntent);

        }
    }
//
//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//        Log.i(TAG, "onDataChanged");
//        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
//
//        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .build();
//
//        if (!mGoogleApiClient.isConnected()) {
//            Log.i(TAG, "not connected");
//            ConnectionResult connectionResult = mGoogleApiClient
//                    .blockingConnect(30, TimeUnit.SECONDS);
//            if (!connectionResult.isSuccess()) {
//                Log.e(TAG, "Service failed to connect to GoogleApiClient.");
//                return;
//            }
//        }
//
//
//        for (DataEvent event : events) {
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                String path = event.getDataItem().getUri().getPath();
//                Log.i(TAG, path);
//                if (Constants.PATH_NOTIFICATION.equals(path)) {
//                    // Get the data out of the event
//                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
//                    final String title = dataMapItem.getDataMap().getString(Constants.KEY_TITLE);
//                    final String time = dataMapItem.getDataMap().getString(Constants.KEY_TIME);
//
//                    // Build the intent to display our custom notification
//                    Intent notificationIntent = new Intent(this, AlarmActivity.class);
//                    notificationIntent.putExtra(AlarmActivity.EXTRA_TITLE, title);
//                    notificationIntent.putExtra(AlarmActivity.EXTRA_TIME, time);
//                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(notificationIntent);
//
//                } else {
//                    Log.d(TAG, "Unrecognized path: " + path);
//                }
//            }
//        }
//  }
}
