package com.lucasasselli.evernotewear;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import com.lucasasselli.evernotewear.component.AmbientLayout;
import com.lucasasselli.evernotewear.component.WearComboActivity;
import com.lucasasselli.simplewearlist.WearListAdapter;
import com.lucasasselli.simplewearlist.WearListItem;
import com.lucasasselli.simplewearlist.WearListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearComboActivity implements WearableListView.ClickListener {

    // MAIN MENU VALUES
    private static final int POSITION_MAIN_SEARCH = 0;
    private static final int POSITION_MAIN_WRITE = 1;
    private static final int POSITION_MAIN_READ = 2;
    private static final int POSITION_MAIN_REFRESH = 3;

    // SPEECH
    private static final int ID_SPEECH_SEARCH = 0;
    private static final int ID_SPEECH_WRITE = 1;

    private AmbientLayout ambientClock;
    private WearListView list;

    private CommManager commManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        commManager = new CommManager(this);

        ambientClock = (AmbientLayout) findViewById(R.id.ambient_container);

        list = (WearListView) findViewById(R.id.menu_list);

        list.setTitle(getString(R.string.title_activity_main));

        final ArrayList<WearListItem> mainMenuItems = new ArrayList<>();
        mainMenuItems.add(POSITION_MAIN_SEARCH, new WearListItem(R.drawable.ic_search_white_18dp, getString(R.string.menu_main_search)));
        mainMenuItems.add(POSITION_MAIN_WRITE, new WearListItem(R.drawable.ic_create_white_18dp, getString(R.string.menu_main_write)));
        mainMenuItems.add(POSITION_MAIN_READ, new WearListItem(R.drawable.ic_speaker_notes_white_18dp, getString(R.string.menu_main_read)));
        mainMenuItems.add(POSITION_MAIN_REFRESH, new WearListItem(R.drawable.ic_autorenew_white_18dp, getString(R.string.menu_main_refresh)));

        list.setClickListener(this);
        WearListAdapter wearListAdapter = new WearListAdapter(this, R.layout.wearlist_item_icon);
        wearListAdapter.setListItems(mainMenuItems);
        list.setAdapter(wearListAdapter);

        // Hacky solution to detect screen shape
        ambientClock.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                list.setUseLayoutBox(insets.isRound());
                return insets;
            }
        });

        // Request immediate database update
        commManager.requestDatabase();
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int position = viewHolder.getPosition();
        switch (position) {
            case POSITION_MAIN_SEARCH:
                displaySpeechRecognizer(ID_SPEECH_SEARCH);
                break;

            case POSITION_MAIN_WRITE:
                displaySpeechRecognizer(ID_SPEECH_WRITE);
                break;

            case POSITION_MAIN_READ:
                ListActivity.start(this);
                break;

            case POSITION_MAIN_REFRESH:
                // TODO: Remove toast
                Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
                commManager.requestDatabase();
                break;
        }
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    // onActivityResult is solely used for voice input (search and write).
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            switch (requestCode) {
                case ID_SPEECH_WRITE:
                    WriteActivity.start(this, spokenText);
                    break;

                case ID_SPEECH_SEARCH:
                    ListActivity.start(this, spokenText);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAmbientEvent(boolean isAmbient) {
        ambientClock.displayClock(isAmbient);

        if (isAmbient) {
            ambientClock.setBackgroundColor(getResources().getColor(R.color.ambient_black));
        } else {
            ambientClock.setBackgroundColor(getResources().getColor(R.color.primary));
        }
    }
}
