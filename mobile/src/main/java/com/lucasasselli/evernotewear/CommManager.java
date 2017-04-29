package com.lucasasselli.evernotewear;


import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.lucasasselli.common.Messenger;
import com.lucasasselli.common.database.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CommManager {

    private static final String TAG = "CommManager";

    Messenger messenger;
    Context context;

    public CommManager(Context context){
        messenger = new Messenger(context);
        this.context = context;
    }

    public CommManager(Context context, GoogleApiClient googleApiClient){
        messenger = new Messenger(googleApiClient);
        this.context = context;
    }

    public void setResultListener(Messenger.ResultListener resultListener){
        messenger.setResultListener(resultListener);
    }

    public void sendResponse(String path, String response){
        messenger.sendMessageAsync(path, response.getBytes());
    }

    public void sendDatabase(){
        File dbFile = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
        if(dbFile.exists()){
            Log.d(TAG, "Database found, sending...");
            byte[] payload = bytesFromFile(dbFile);
            messenger.sendData(Messenger.PATH_DATA_DATABASE, Messenger.KEY_CONTENT, payload);
        }else{
            Log.e(TAG, "Unable to find database");
        }
    }

    private byte[] bytesFromFile(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);

            byte bytes[] = new byte[(int) inputStream.getChannel().size()];
            for(int i=0; i<bytes.length; i++){
                bytes[i] = (byte) (inputStream.read() & 0xFF);
            }

            inputStream.close();

            return bytes;

        } catch(FileNotFoundException e){
            Log.e(TAG, "File not found", e);
            return null;
        } catch (IOException e){
            Log.e(TAG, "IO exception", e);
            return null;
        }
    }
}
