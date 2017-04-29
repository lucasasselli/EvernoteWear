package com.lucasasselli.common;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.provider.SyncStateContract;
import android.service.voice.AlwaysOnHotwordDetector;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Messenger {

    private final static String TAG = "Messenger";

    public static final int CONNECTION_TIME_OUT_MS = 2000;

    // Actions
    public final static String PATH_ACTION_NOTE_ADD = "/ACTION_NOTE_ADD";
    public final static String PATH_ACTION_NOTE_DELETE = "/ACTION_NOTE_DELETE";
    public final static String PATH_ACTION_NOTE_OPEN = "/ACTION_NOTE_OPEN";
    public final static String PATH_ACTION_REQUEST_DATABASE = "/ACTION_REQUEST_DATABASE";

    // Data paths
    public final static String PATH_DATA_DATABASE = "/DATA_DATABASE";
    public final static String KEY_CONTENT = "KEY_CONTENT";

    // STANDARD RESPONSES
    public final static String RESPONSE_OK = "RESPONSE_OK";
    public final static String RESPONSE_ERROR_GENERIC = "RESPONSE_ERROR_GENERIC";
    public final static String RESPONSE_ERROR_NOLOGIN = "RESPONSE_ERROR_NOLOGIN";

    private GoogleApiClient googleApiClient;
    private ResultListener resultListener;

    public Messenger(GoogleApiClient googleApiClient){
        this.googleApiClient = googleApiClient;
    }

    public Messenger(Context context){
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    public void sendMessageAsync(final String path, @Nullable final byte[] data) {
        Log.d(TAG, "Starting async thread...");

        // Send message on a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessage(path, data);
            }
        }).start();
    }

    public void sendMessageAsync(final String nodeId, final String path, @Nullable final byte[] data) {
        Log.d(TAG, "Starting async thread...");

        // Send message on a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessage(nodeId, path, data);
            }
        }).start();
    }

    public void sendMessage(String path, @Nullable byte[] data) {
        Log.d(TAG, "Sending message...");

        googleApiClient.connect();
        googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        String nodeId = getNode(googleApiClient);
        Log.d(TAG, "Node Id: " + nodeId);

        // Sends message on first available node
        if (nodeId!=null) {

            Wearable.MessageApi
                    .sendMessage(googleApiClient, nodeId, path, data)
                    .setResultCallback(buildMessageResultCallback(path));

            Log.d(TAG, "Message sent to " + nodeId + ", path was " + path);
        } else {
            Log.e(TAG, "No valid nodeId found!");
            if(resultListener!=null) resultListener.onSendFailure(path);
        }

        googleApiClient.disconnect();
    }

    public void sendMessage(String nodeId, String path, @Nullable byte[] data) {
        Log.d(TAG, "Sending message...");

        googleApiClient.connect();
        googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        Wearable.MessageApi
                .sendMessage(googleApiClient, nodeId, path, data)
                .setResultCallback(buildMessageResultCallback(path));

        Log.d(TAG, "Message sent to " + nodeId + ", path was " + path);

        googleApiClient.disconnect();
    }

    private String getNode(GoogleApiClient googleApiClient){
        NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        List<Node> nodes = result.getNodes();
        if(nodes.size()>0) {
            return nodes.get(0).getId();
        }else{
            return null;
        }
    }

    public void sendData(String path, String key, byte[] payload){

        PutDataMapRequest dataMap = PutDataMapRequest.create(path);

        dataMap.getDataMap().putByteArray(key, payload);

        PutDataRequest request = dataMap.asPutDataRequest();

        Wearable.DataApi
                .putDataItem(googleApiClient, request)
                .setResultCallback(buildDataResultCallback(path));
        Log.d(TAG, "Data item at " + path + " updated, key was " + key + ", payload size was " + payload.length);
    }



    private ResultCallback<MessageApi.SendMessageResult> buildMessageResultCallback(final String path) {

        return new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                if (sendMessageResult.getStatus().isSuccess()) {
                    // If message is sent successfully
                    Log.d(TAG, "Message sent successfully!");
                    if (resultListener != null) resultListener.onSendSuccess(path);
                } else {
                    // Unable to send message
                    Log.e(TAG, "Unable to send the message!");
                    if (resultListener != null) resultListener.onSendFailure(path);
                }
            }
        };
    }

    private ResultCallback<DataApi.DataItemResult> buildDataResultCallback(final String path) {

        return new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    // If message is sent successfully
                    Log.d(TAG, "Data sent successfully!");
                    if (resultListener != null) resultListener.onSendSuccess(path);
                } else {
                    // Unable to send message
                    Log.e(TAG, "Unable to send data!");
                    if (resultListener != null) resultListener.onSendFailure(path);
                }
            }
        };
    }

    public void setResultListener(ResultListener resultListener){
        this.resultListener = resultListener;
    }

    // Result interface
    public interface ResultListener{
        void onSendSuccess(String path);
        void onSendFailure(String path);
    }
}
