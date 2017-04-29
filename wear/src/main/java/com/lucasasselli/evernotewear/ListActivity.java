package com.lucasasselli.evernotewear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.TextView;

import com.evernote.edam.type.Note;
import com.lucasasselli.common.ENContent;
import com.lucasasselli.common.database.NotesTable;
import com.lucasasselli.evernotewear.component.AmbientLayout;
import com.lucasasselli.evernotewear.component.WearComboActivity;
import com.lucasasselli.evernotewear.service.MobileListener;
import com.lucasasselli.simplewearlist.WearListAdapter;
import com.lucasasselli.simplewearlist.WearListItem;
import com.lucasasselli.simplewearlist.WearListView;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends WearComboActivity implements WearableListView.ClickListener{

    public final static String EXTRA_SEARCH_QUERY = "EXTRA_SEARCH_QUERY";

    // Views
    AmbientLayout ambientClock;
    WearListView list;
    TextView errorText;

    WearListAdapter adapter;
    List<Note> noteList = new ArrayList<>();

    String query;

    public static void start(Context context, String query) {
        Intent intent;
        NotesTable notesTable = new NotesTable(context);

        if (notesTable.count() > 0) {
            intent = new Intent(context, ListActivity.class);
            if (query != null) {
                intent.putExtra(EXTRA_SEARCH_QUERY, query);
            }
        } else {
            intent = new Intent(context, ConfirmationActivity.class);
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, context.getString(R.string.list_error_empty));
        }

        context.startActivity(intent);
    }

    public static void start(Context context) {
        start(context, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        setAmbientEnabled();

        ambientClock = (AmbientLayout) findViewById(R.id.ambient_container);

        list = (WearListView) findViewById(R.id.note_list);

        // By default error text must be gone
        errorText = (TextView) findViewById(R.id.error_text);
        errorText.setVisibility(View.GONE);

        // Check if is search
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SEARCH_QUERY)) {
            // Search activity
            query = intent.getStringExtra(EXTRA_SEARCH_QUERY);
            list.setTitle(getString(R.string.title_activity_search));
        } else {
            // List Activity
            list.setTitle(getString(R.string.title_activity_list));
        }

        list.setUseLayoutBox(false);
        list.setTitleColor(getResources().getColor(R.color.primary));
        list.setClickListener(this);

        adapter = new WearListAdapter(this, R.layout.wearlist_item_text);
        adapter.setTitleColor(getResources().getColor(R.color.text_dark));
        adapter.setDescriptionColor(getResources().getColor(R.color.text_dark_secondary));

        list.setAdapter(adapter);

        //updateList();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
        IntentFilter intentFilter = new IntentFilter(MobileListener.BROADCAST_DATABASE_UPDATED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onAmbientEvent(boolean isAmbient) {
        ambientClock.displayClock(isAmbient);

        if (isAmbient) {
            ambientClock.setBackgroundColor(getResources().getColor(R.color.ambient_black));
            list.setTitleColor(getResources().getColor(R.color.ambient_white));
            adapter.setTitleColor(getResources().getColor(R.color.ambient_white));
            adapter.setDescriptionColor(getResources().getColor(R.color.ambient_grey));
            errorText.setTextColor(getResources().getColor(R.color.ambient_white));
        } else {
            ambientClock.setBackgroundColor(getResources().getColor(R.color.white));
            list.setTitleColor(getResources().getColor(R.color.primary));
            adapter.setTitleColor(getResources().getColor(R.color.text_dark));
            adapter.setDescriptionColor(getResources().getColor(R.color.text_dark_secondary));
            errorText.setTextColor(getResources().getColor(R.color.text_dark));
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int position = viewHolder.getPosition();
        ReadActivity.start(this, noteList.get(position).getGuid());
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    private void updateList(){
        NotesTable notesTable = new NotesTable(this);
        noteList.clear();

        if (query != null) {
            // Search using query
            noteList = notesTable.search(query);
        } else {
            // List all notes
            noteList = notesTable.fetchNotes();
        }

        // Check if content is empty
        if(noteList.isEmpty()) {
            errorText.setVisibility(View.VISIBLE);
            if(query!=null) {
                String errorTextString = getString(R.string.search_error_noresult) + "\n\"" + query + "\"";
                errorText.setText(errorTextString);
            } else {
                errorText.setText(R.string.list_error_empty);
            }
        }else {
            final ArrayList<WearListItem> listItems = new ArrayList<>();
            for (Note note : noteList) {
                listItems.add(new WearListItem(note.getTitle(), ENContent.getNoteContent(note.getContent())));
            }

            adapter.setListItems(listItems);
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateList();
        }
    };
}
