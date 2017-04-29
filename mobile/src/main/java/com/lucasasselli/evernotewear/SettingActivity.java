package com.lucasasselli.evernotewear;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.Notebook;
import com.lucasasselli.common.database.NotebooksTable;
import com.lucasasselli.evernotewear.service.SyncManager;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private final static String TAG = "SettingsActivity";

    private MyPreferenceFragment myPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myPreferenceFragment =  new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, myPreferenceFragment).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Call fragment onActivityResult
        myPreferenceFragment.onActivityResult(requestCode, resultCode, data);
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {

        Preference account;

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra(SyncManager.EXTRA_RESULT)){
                    // Sync ended
                    String syncResult = intent.getStringExtra(SyncManager.EXTRA_RESULT);

                    account.setEnabled(true);

                    if(syncResult.equals(SyncManager.RESULT_SUCCESS)){
                        updateAccountPref();
                    }else{
                        account.setTitle(R.string.pref_account_empty_title);
                        account.setSummary(R.string.pref_account_empty_summary);
                    }

                }else{
                    Log.e(TAG, "Received invalid bradcast");
                }
            }
        };

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            ListPreference defaultNotebook = (ListPreference) findPreference("default_notebook");

            NotebooksTable notebooksTable = new NotebooksTable(getActivity());
            List<Notebook> notebooks = notebooksTable.fetchNotebooks();

            // Check if any notebook exist
            if(notebooks.size()==0) {
                // Disable default notebook button
                defaultNotebook.setEnabled(false);
            }else {
                // Disable default notebook button
                CharSequence names[] = new CharSequence[notebooks.size()];
                CharSequence guids[] = new CharSequence[notebooks.size()];
                for (int i = 0; i < notebooks.size(); i++) {
                    names[i] = notebooks.get(i).getName();
                    guids[i] = notebooks.get(i).getName();
                }
                defaultNotebook.setEntries(names);
                defaultNotebook.setEntryValues(guids);
            }

            // Account button
            account = findPreference(getString(R.string.pref_account_key));
            updateAccountPref();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case EvernoteSession.REQUEST_CODE_LOGIN:
                    if (resultCode == Activity.RESULT_OK) {
                        // Login successful, start service and wait for broadcast
                        Log.d(TAG, "Login success");
                        account.setTitle(R.string.pref_account_loading);
                        account.setSummary("");
                        account.setEnabled(false);
                        SyncManager.start(getActivity());
                    } else {
                        // Login failed
                        Log.d(TAG, "Login failed");
                        account.setTitle(R.string.pref_account_empty_title);
                        account.setSummary(R.string.pref_account_empty_summary);
                    }
                    break;

                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter intentFilter = new IntentFilter(SyncManager.BROADCAST_SYNC_COMPLETED);
            getActivity().registerReceiver(broadcastReceiver, intentFilter);
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(broadcastReceiver);
        }

        public void updateAccountPref(){
            // Updates the account pref button
            final MyEvernote myEvernote = new MyEvernote(getActivity());
            InternalData internalData = new InternalData(getActivity());

            account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    myEvernote.getSession().authenticate(getActivity());
                    return false;
                }
            });

            // Retrieve username and last timestamp from internal data
            String accountUsername = internalData.readString(MyEvernote.USERNAME_KEY, null);
            long lastSyncTimestamp = internalData.readLong(MyEvernote.LASTSYNC_KEY, 0);

            if(accountUsername!=null){
                // Account username found
                account.setTitle(accountUsername);
                if(lastSyncTimestamp!=0){
                    String lastSyncDateTime = DateFormat.getDateTimeInstance().format(new Date(lastSyncTimestamp*1000));
                    String summary = getString(R.string.pref_account_lastsync)+ ": " + lastSyncDateTime;
                    account.setSummary(summary);
                }
            }else{
                // If the account username is null, but login is valid, data is still syncing
                if(myEvernote.isLoggedIn()){
                    account.setTitle(R.string.pref_account_loading);
                    account.setSummary("");
                    account.setEnabled(false);
                }else{
                    // User not logged in
                    account.setTitle(R.string.pref_account_empty_title);
                    account.setSummary(R.string.pref_account_empty_summary);
                }
            }
        }
    }
}
