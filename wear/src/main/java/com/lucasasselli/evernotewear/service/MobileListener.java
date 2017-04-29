package com.lucasasselli.evernotewear.service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.lucasasselli.common.Messenger;
import com.lucasasselli.common.database.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;

/**
 * This Service manages all the incoming data traffic. Information and results are then relayed through broadcasts.
 */

public class MobileListener extends WearableListenerService {

    public final static String BROADCAST_NEWNOTE_RESULT = "BROADCAST_NEWNOTE_RESULT";
    public final static String BROADCAST_DATABASE_UPDATED = "BROADCAST_DATABASE_UPDATED";
    public final static String EXTRA_RESULT = "EXTRA_RESULT";
    private static final String TAG = "MobileListener";

    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData());

        Log.d(TAG, "Received message at path " + path + ", with content " + data);

        switch (path) {
            case Messenger.PATH_ACTION_NOTE_ADD:
                // Send broadcast to activity
                sendBroadcast(BROADCAST_NEWNOTE_RESULT, data);
                break;
        }

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();

                if (path.equals(Messenger.PATH_DATA_DATABASE)) {
                    // Database update event
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.d(TAG, "Database dataMap received on watch: " + dataMap);

                    byte[] databaseBytes = dataMap.getByteArray(Messenger.KEY_CONTENT);

                    if (databaseBytes != null) {
                        Log.d(TAG, "Database size is : " + databaseBytes.length);
                        saveDatabase(databaseBytes);
                        sendBroadcast(BROADCAST_DATABASE_UPDATED);
                    }
                }
            }
        }
    }

    private void saveDatabase(byte[] databaseBytes) {
        File dbFile = getDatabasePath(DatabaseHelper.DATABASE_NAME);
        File dbDirectory = dbFile.getParentFile();

        if (!dbDirectory.exists()) {
            boolean createPathResult = dbDirectory.mkdirs();
            if (!createPathResult) {
                Log.e(TAG, "Unable to create path");
                return;
            }
        }

        // To prevent conflicts
        if (dbFile.exists()) {
            boolean deleteResult = dbFile.delete();
            if (!deleteResult) {
                Log.e(TAG, "Unable to delete old database file");
                return;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(dbFile.getPath());
            fos.write(databaseBytes);
            fos.close();
            Log.d(TAG, "Database created/updated successfully");
        } catch (java.io.IOException e) {
            Log.e(TAG, "IO Exception", e);
        }
    }

    private void sendBroadcast(String broadcast, String content) {
        Intent intent = new Intent(broadcast);
        intent.putExtra(EXTRA_RESULT, content);
        sendBroadcast(intent);
    }

    private void sendBroadcast(String broadcast) {
        Intent intent = new Intent(broadcast);
        sendBroadcast(intent);
    }
}