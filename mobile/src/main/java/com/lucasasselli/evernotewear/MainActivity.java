package com.lucasasselli.evernotewear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.evernote.client.android.EvernoteSession;
import com.lucasasselli.evernotewear.service.SyncManager;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";

    Context context;
    EvernoteSession evernoteSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

        // TODO Spostare
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // EVERNOTE CLIENT SETUP
        evernoteSession = new EvernoteSession.Builder(this)
                .setEvernoteService(Constants.EVERNOTE_SERVICE)
                .build(Constants.EVERNOTE_KEY, Constants.EVERNOTE_SECRET)
                .asSingleton();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        }

        if(id == R.id.action_refresh){
            startService(new Intent(getApplicationContext(), SyncManager.class));
        }

        // TODO DEBUGGGG
        if (id == R.id.action_test) {
            startActivity(new Intent(this, SetupActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
