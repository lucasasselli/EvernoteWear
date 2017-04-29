package com.lucasasselli.simplewearlist;


import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class IconItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {

    private static final float NO_ALPHA = 1f;
    private static final float PARTIAL_ALPHA = 0.40f;

    // Views
    private CircledImageView circle;

    // Values
    private float bigCircleRadius;
    private float smallCircleRadius;

    public IconItemLayout(Context context) {
        this(context, null);
    }

    public IconItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // This method is always called by the other constructors
        smallCircleRadius = getResources().getDimensionPixelSize(R.dimen.circle_radius_small);
        bigCircleRadius = getResources().getDimensionPixelSize(R.dimen.circle_radius_big);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        circle = (CircledImageView) findViewById(R.id.icon);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        setAlpha(NO_ALPHA);
        circle.setCircleRadius(bigCircleRadius);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        setAlpha(PARTIAL_ALPHA);
        circle.setCircleRadius(smallCircleRadius);
    }
}