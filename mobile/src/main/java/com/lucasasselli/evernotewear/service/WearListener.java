package com.lucasasselli.evernotewear.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.lucasasselli.common.Messenger;
import com.lucasasselli.common.database.DatabaseHelper;
import com.lucasasselli.evernotewear.CommManager;
import com.lucasasselli.evernotewear.MyEvernote;
import com.lucasasselli.evernotewear.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WearListener extends WearableListenerService {

    private static final String TAG = "WearListener";

    GoogleApiClient googleApiClient;
    CommManager commManager;
    MyEvernote myEvernote;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        googleApiClient = getGoogleApiClient(this);
        googleApiClient.connect();

        commManager = new CommManager(this, googleApiClient);

        myEvernote = new MyEvernote(this);

        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData());

        Log.d(TAG, "Received message at path " + path + ", with content " + data);

        switch(path) {
            case Messenger.PATH_ACTION_NOTE_ADD:
                if(Utils.getInternetConnection(this)!=Utils.NO_CONNECTION) {
                    // Add note
                    if(!data.isEmpty()) {
                        int addNoteResult = myEvernote.addNote(data);
                        if (addNoteResult == MyEvernote.OK) {
                            // TODO implementare errore login
                            commManager.sendResponse(Messenger.PATH_ACTION_NOTE_ADD, Messenger.RESPONSE_OK);
                            // Start SyncManager service
                            SyncManager.start(this);
                        } else {
                            commManager.sendResponse(Messenger.PATH_ACTION_NOTE_ADD, Messenger.RESPONSE_ERROR_GENERIC);
                        }
                    }else{
                        commManager.sendResponse(Messenger.PATH_ACTION_NOTE_ADD, Messenger.RESPONSE_ERROR_GENERIC);
                    }
                }else{
                    Log.e(TAG, "No internet connection");
                    commManager.sendResponse(path, Messenger.RESPONSE_ERROR_GENERIC);
                }
                break;

            case Messenger.PATH_ACTION_NOTE_DELETE:
                // Delete note
                if(Utils.getInternetConnection(this)!=Utils.NO_CONNECTION) {
                    if(!data.isEmpty()) {
                        int deleteNoteResult = myEvernote.deleteNote(data);
                        if (deleteNoteResult == MyEvernote.OK) {
                            // TODO implementare errore login
                            commManager.sendResponse(Messenger.PATH_ACTION_NOTE_DELETE, Messenger.RESPONSE_OK);
                            // Start SyncManager service
                            SyncManager.start(this);
                        } else {
                            commManager.sendResponse(Messenger.PATH_ACTION_NOTE_DELETE, Messenger.RESPONSE_ERROR_GENERIC);
                        }
                    }else{
                        commManager.sendResponse(Messenger.PATH_ACTION_NOTE_DELETE, Messenger.RESPONSE_ERROR_GENERIC);
                    }
                }else{
                    Log.e(TAG, "No internet connection");
                    commManager.sendResponse(path, Messenger.RESPONSE_ERROR_GENERIC);
                }
                break;

            case Messenger.PATH_ACTION_REQUEST_DATABASE:
                // Get database
                commManager.sendDatabase();
                break;

            case Messenger.PATH_ACTION_NOTE_OPEN:
                // Open note in evernote
                if(!data.isEmpty()) {
                    Intent intent  = new Intent();
                    intent.setAction("com.evernote.action.VIEW_NOTE");
                    intent.putExtra("NOTE_GUID", data);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    commManager.sendResponse(Messenger.PATH_ACTION_NOTE_OPEN, Messenger.RESPONSE_ERROR_GENERIC);
                }
                break;
        }
    }

    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }
}