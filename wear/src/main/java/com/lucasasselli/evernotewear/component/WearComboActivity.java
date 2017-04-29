package com.lucasasselli.evernotewear.component;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.WearableActivity;

public class WearComboActivity extends WearableActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Ambient display
    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        onAmbientEvent(isAmbient());
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        onAmbientEvent(isAmbient());
    }

    @Override
    public void onExitAmbient() {
        onAmbientEvent(isAmbient());
        super.onExitAmbient();
    }

    // To keep the inheriting activity clean and simple Override this method
    public void onAmbientEvent(boolean isAmbient) {
        // Placeholder
    }

    // Speech
    public void displaySpeechRecognizer(int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text. Request code is used to perform the right action.
        startActivityForResult(intent, requestCode);
    }
}
