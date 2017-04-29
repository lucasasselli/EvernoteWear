package com.lucasasselli.evernotewear.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class SmartScrollView extends ScrollView {

    OnBottomReachedListener onBottomReachedListener;

    public SmartScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SmartScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmartScrollView(Context context) {
        super(context);
        init();
    }

    void init() {
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = getChildAt(getChildCount() - 1);
        int diff = (view.getBottom() - (getHeight() + getScrollY()));

        if (diff == 0 && onBottomReachedListener != null) {
            onBottomReachedListener.onBottomReached();
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }

    public OnBottomReachedListener getOnBottomReachedListener() {
        return onBottomReachedListener;
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public interface OnBottomReachedListener {
        public void onBottomReached();
    }

}
