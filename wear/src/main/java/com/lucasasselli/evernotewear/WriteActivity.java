package com.lucasasselli.evernotewear;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lucasasselli.common.Messenger;
import com.lucasasselli.common.TimeoutTimer;
import com.lucasasselli.evernotewear.component.SmartScrollView;
import com.lucasasselli.evernotewear.component.WearComboActivity;
import com.lucasasselli.evernotewear.service.MobileListener;

import java.util.List;

public class WriteActivity extends WearComboActivity implements DelayedConfirmationView.DelayedConfirmationListener, TimeoutTimer.TimeoutListener, Messenger.ResultListener {

    public final static String EXTRA_NOTE_CONTENT = "EXTRA_NOTE_CONTENT";
    public final static int RESPONSE_TIMEOUT = 20000;
    private static final String TAG = "WriteActivity";
    private static final int ID_SPEECH_WRITE = 1;
    // Animation
    private final static int ANIMATION_START_DELAY = 1000;
    private final static int DELAYED_VIEW_DURATION = 5000;
    private final AnimatorSet scrollAnimator = new AnimatorSet();
    CommManager commManager;
    TimeoutTimer timeoutTimer;
    // Broadcast
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if has extra
            if (intent.hasExtra(MobileListener.EXTRA_RESULT)) {
                Log.d(TAG, "Broadcast received");
                // Cancel the timeout timer
                timeoutTimer.cancel();

                String content = intent.getStringExtra(MobileListener.EXTRA_RESULT);

                // Check result
                if (content.equals(Messenger.RESPONSE_OK)) {
                    Log.d(TAG, "New note added correctly!");
                    finishWithResult(true, null);
                } else {
                    Log.d(TAG, "Failed to add new note!");
                    finishWithResult(false, getString(R.string.write_error_no_evernote));
                }
            } else {
                Log.e(TAG, "Received invalid bradcast");
            }
        }
    };
    // Views
    private TextView noteText;
    private DelayedConfirmationView delayedView;
    private LinearLayout mainContainer;
    private SmartScrollView scrollContainer;
    private String noteContent;
    private ProgressBar loadingProgress;

    public static void start(Context context, String content) {
        Intent intent = new Intent(context, WriteActivity.class);
        intent.putExtra(EXTRA_NOTE_CONTENT, content);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        commManager = new CommManager(this);
        commManager.setResultListener(this);

        timeoutTimer = new TimeoutTimer(RESPONSE_TIMEOUT);
        timeoutTimer.setTimeoutListener(this);

        noteText = (TextView) findViewById(R.id.note_text);

        scrollContainer = (SmartScrollView) findViewById(R.id.scroll_container);
        mainContainer = (LinearLayout) findViewById(R.id.main_container);

        delayedView = (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
        delayedView.setListener(this);
        delayedView.setTotalTimeMs(DELAYED_VIEW_DURATION);

        // If the users scrolls, cancel the animation
        scrollContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollAnimator.cancel();
                Log.d(TAG, "Animation cancelled: user action");
                return false;
            }
        });
        // If the animations reaches the end, cancel the animation
        scrollContainer.setOnBottomReachedListener(new SmartScrollView.OnBottomReachedListener() {
            @Override
            public void onBottomReached() {
                if (scrollContainer.getHeight() != 0) {
                    scrollAnimator.cancel();
                    Log.d(TAG, "Animation cancelled: scroll bottom reached");

                }
            }
        });

        loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
        loadingProgress.setVisibility(View.INVISIBLE);

        // Layout is ready, parse intent
        Intent intent = getIntent();
        // Check text extra
        if (intent.hasExtra(EXTRA_NOTE_CONTENT)) {
            setNoteDisplay(intent.getStringExtra(EXTRA_NOTE_CONTENT));
        } else if (intent.hasExtra(android.content.Intent.EXTRA_TEXT)) {
            setNoteDisplay(intent.getStringExtra(android.content.Intent.EXTRA_TEXT));
        } else {
            // Intent has empty content, get user input
            displaySpeechRecognizer(ID_SPEECH_WRITE);
        }

        // Hacky solution to detect screen shape
        scrollContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int headerMargin = getResources().getDimensionPixelSize(R.dimen.header_margin);
                int footerMargin = getResources().getDimensionPixelSize(R.dimen.footer_margin);
                int genericMargin;

                if (insets.isRound()) {
                    // Round
                    genericMargin = getResources().getDimensionPixelSize(R.dimen.generic_margin_round);
                } else {
                    // Square
                    genericMargin = getResources().getDimensionPixelSize(R.dimen.generic_margin);
                }

                mainContainer.setPadding(genericMargin, headerMargin, genericMargin, footerMargin);

                return insets;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            if (requestCode == ID_SPEECH_WRITE) {
                setNoteDisplay(spokenText);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onTimerFinished(View view) {
        // User didn't cancel, perform the action
        if (noteContent != null) {
            if (!noteContent.isEmpty()) {
                // Display progress circle
                delayedView.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);

                // Send new note request
                commManager.addNote(noteContent);
            }
        }
    }

    @Override
    public void onTimeout() {
        Log.e(TAG, "Response timeout!");
        finishWithResult(false, getString(R.string.write_error_no_response));
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(MobileListener.BROADCAST_NEWNOTE_RESULT);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        timeoutTimer.cancel();
    }

    @Override
    public void onSendSuccess(String path) {
        Log.d(TAG, "Message sent to mobile device: waiting for response...");
        // Start the timer
        timeoutTimer.start();
    }

    @Override
    public void onSendFailure(String path) {
        Log.e(TAG, "Connection to mobile device failed!");
        finishWithResult(false, getString(R.string.write_error_no_device));
    }

    @Override
    public void onTimerSelected(View view) {
        // User canceled, abort the action
        onBackPressed();
    }

    // Updates the UI content and starts the sending process
    void setNoteDisplay(String note) {
        if (note != null) {
            if (!note.isEmpty()) {
                Log.d(TAG, "Note content valid, updating UI...");
                noteContent = note;

                /**
                 * This is an hacky solution to get the animation correctly, and time it
                 * since at this point view are not yet created and scrollContainer height is 0
                 */
                ObjectAnimator yTranslate = ObjectAnimator.ofInt(scrollContainer, "scrollY", noteContent.length() * 200);
                scrollAnimator.setDuration(noteContent.length() * 100);
                scrollAnimator.setStartDelay(ANIMATION_START_DELAY);
                scrollAnimator.play(yTranslate);
                scrollAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Fire the confirmation delay
                        delayedView.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                // Set the text
                noteText.setText(note);
                // Start the animation
                scrollAnimator.start();
            } else {
                // A note is empty after a speech service error, which is shown to the user automatically
                Log.e(TAG, "Unable to add note: speech service error!");
                finish();
            }
        } else {
            // Something went wrong! No text is available!
            Log.e(TAG, "Unable to add note: null note!");
            finishWithResult(false, getString(R.string.write_error_empty));
        }
    }

    // Ends the activity and displays the result
    void finishWithResult(boolean success, String message) {
        Intent intent = new Intent(this, ConfirmationActivity.class);

        if (success) {
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        } else {
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
        }

        if (message != null) {
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
        }

        startActivity(intent);
        finish();
    }
}