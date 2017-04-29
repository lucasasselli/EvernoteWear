package com.lucasasselli.evernotewear;


import android.content.Context;

import com.lucasasselli.common.Messenger;

public class CommManager {

    Messenger messenger;

    CommManager(Context context) {
        messenger = new Messenger(context);
    }

    public void setResultListener(Messenger.ResultListener resultListener) {
        messenger.setResultListener(resultListener);
    }

    public void addNote(String content) {
        messenger.sendMessageAsync(Messenger.PATH_ACTION_NOTE_ADD, content.getBytes());
    }

    public void deleteNote(String guid) {
        messenger.sendMessageAsync(Messenger.PATH_ACTION_NOTE_DELETE, guid.getBytes());
    }

    public void openNote(String guid) {
        messenger.sendMessageAsync(Messenger.PATH_ACTION_NOTE_OPEN, guid.getBytes());
    }

    public void requestDatabase() {
        messenger.sendMessageAsync(Messenger.PATH_ACTION_REQUEST_DATABASE, null);
    }
}
