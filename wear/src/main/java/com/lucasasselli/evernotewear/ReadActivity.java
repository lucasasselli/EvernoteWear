package com.lucasasselli.evernotewear;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.edam.type.Note;
import com.lucasasselli.common.ENContent;
import com.lucasasselli.common.database.NotesTable;
import com.lucasasselli.evernotewear.component.AmbientLayout;
import com.lucasasselli.evernotewear.component.WearComboActivity;
import com.lucasasselli.simplewearlist.WearListItem;

import java.util.ArrayList;
import java.util.List;

public class ReadActivity extends WearComboActivity {

    public final static String EXTRA_NOTE_GUID = "EXTRA_GUID";
    private final static String TAG = "ReadActivity";
    private static final int POSITION_FOOTER_DELETE = 0;
    private static final int POSITION_FOOTER_OPEN = 1;

    private AmbientLayout ambientClock;
    private TextView titleText;
    private TextView contentText;
    private LinearLayout headerContainer;
    private LinearLayout footerContainer;

    private FooterButtonAdapter footerButtonAdapter;

    private CommManager commManager;
    private Context context;

    public static void start(Context context, String guid) {
        Intent intent = new Intent(context, ReadActivity.class);
        intent.putExtra(EXTRA_NOTE_GUID, guid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        setAmbientEnabled();

        commManager = new CommManager(this);
        context = this;

        ambientClock = (AmbientLayout) findViewById(R.id.ambient_container);

        titleText = (TextView) findViewById(R.id.title_text);
        contentText = (TextView) findViewById(R.id.content_text);

        headerContainer = (LinearLayout) findViewById(R.id.header_container);
        footerContainer = (LinearLayout) findViewById(R.id.footer_container);

        final String guid = getIntent().getStringExtra(EXTRA_NOTE_GUID);

        if (guid != null) {
            NotesTable notesTable = new NotesTable(this);
            Note note = notesTable.getNote(guid);

            if (note != null) {
                titleText.setText(note.getTitle());
                contentText.setText(ENContent.getNoteContent(note.getContent()));
            } else {
                Log.e(TAG, "Note GUID search returned null!");
                finish();
            }
        } else {
            Log.e(TAG, "Empty GUID extra!");
            finish();
        }

        // Hacky solution to detect screen shape
        ambientClock.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int headerMargin = getResources().getDimensionPixelSize(R.dimen.header_margin);
                int footerMargin = getResources().getDimensionPixelSize(R.dimen.footer_margin);
                int condensedMargin = getResources().getDimensionPixelSize(R.dimen.condensed_margin);
                int genericMargin;

                if (insets.isRound()) {
                    // Round
                    genericMargin = getResources().getDimensionPixelSize(R.dimen.generic_margin_round);
                } else {
                    // Square
                    genericMargin = getResources().getDimensionPixelSize(R.dimen.generic_margin);
                }

                headerContainer.setPadding(genericMargin, headerMargin, genericMargin, footerMargin);
                footerContainer.setPadding(genericMargin, condensedMargin, genericMargin, footerMargin);

                return insets;
            }
        });

        // Populate footer
        final ArrayList<WearListItem> footerItems = new ArrayList<>();
        footerItems.add(POSITION_FOOTER_DELETE, new WearListItem(R.drawable.ic_delete_white_18dp, getString(R.string.footer_button_delete)));
        footerItems.add(POSITION_FOOTER_OPEN, new WearListItem(R.drawable.ic_phone_android_white_18dp, getString(R.string.footer_button_phone)));

        // Simple adapter used to manage and modify inner views in ambient mode
        footerButtonAdapter = new FooterButtonAdapter(this, footerContainer, footerItems);
        footerButtonAdapter.setOnClickListener(new FooterButtonAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                switch (position) {
                    case POSITION_FOOTER_DELETE:
                        // TODO: Remove toast
                        Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                        commManager.deleteNote(guid);
                        break;

                    case POSITION_FOOTER_OPEN:
                        // TODO: Remove toast
                        Toast.makeText(context, "Opening...", Toast.LENGTH_SHORT).show();
                        commManager.openNote(guid);
                        break;
                }
            }
        });
    }

    @Override
    public void onAmbientEvent(boolean isAmbient) {
        ambientClock.displayClock(isAmbient);

        if (isAmbient) {
            ambientClock.setBackgroundColor(getResources().getColor(R.color.ambient_black));
            titleText.setTextColor(getResources().getColor(R.color.ambient_grey));
            contentText.setTextColor(getResources().getColor(R.color.ambient_white));
            footerContainer.setBackgroundColor(getResources().getColor(R.color.ambient_grey));
            footerButtonAdapter.setIconColor(getResources().getColor(R.color.ambient_black));
        } else {
            ambientClock.setBackgroundColor(getResources().getColor(R.color.white));
            titleText.setTextColor(getResources().getColor(R.color.text_dark_secondary));
            contentText.setTextColor(getResources().getColor(R.color.text_dark));
            footerContainer.setBackgroundColor(getResources().getColor(R.color.primary));
            footerButtonAdapter.setIconColor(getResources().getColor(R.color.primary));
        }
    }

    static private class FooterButtonAdapter {

        LinearLayout container;
        OnClickListener onClickListener;

        FooterButtonAdapter(Context context, LinearLayout container, List<WearListItem> items) {
            this.container = container;
            LayoutInflater inflater = LayoutInflater.from(context);

            for (int i = 0; i < items.size(); i++) {

                final int position = i;
                WearListItem item = items.get(i);

                final View footerButton = inflater.inflate(R.layout.footer_button, null);

                footerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickListener != null) onClickListener.onClick(position);
                    }
                });

                CircledImageView icon = (CircledImageView) footerButton.findViewById(R.id.icon);
                icon.setImageResource(item.getIconRes());

                TextView titleText = (TextView) footerButton.findViewById(R.id.title);
                titleText.setText(item.getTitle());

                container.addView(footerButton);
            }
        }

        void setIconColor(int color) {
            for (int i = 0; i < container.getChildCount(); i++) {
                View view = container.getChildAt(i);
                CircledImageView icon = (CircledImageView) view.findViewById(R.id.icon);
                icon.setImageTint(color);
            }
        }

        void setOnClickListener(OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        interface OnClickListener {
            void onClick(int position);
        }
    }
}
