package com.lucasasselli.simplewearlist;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {

    private static final float NO_ALPHA = 1f;
    private static final float PARTIAL_ALPHA = 0.40f;
    TextView titleText;

    public TextItemLayout(Context context) {
        this(context, null);
    }

    public TextItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void onCenterPosition(boolean animate) {
        setAlpha(NO_ALPHA);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        setAlpha(PARTIAL_ALPHA);
    }
}