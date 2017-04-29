package com.lucasasselli.evernotewear.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lucasasselli.evernotewear.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AmbientLayout extends RelativeLayout {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private TextView clock;
    private Context context;


    public AmbientLayout(Context context) {
        super(context);
        this.context = context;
    }

    public AmbientLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public AmbientLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        clock = new TextView(context);
        clock.setBackground(getResources().getDrawable(R.drawable.clock_box));
        clock.setPadding(5, 5, 5, 5);
        clock.setVisibility(GONE);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(Gravity.CENTER_HORIZONTAL);
        layoutParams.topMargin = context.getResources().getDimensionPixelSize(R.dimen.clock_margin);
        clock.setTextColor(context.getResources().getColor(R.color.ambient_white));
        addView(clock, layoutParams);
    }

    public void displayClock(boolean display) {
        if (display) {
            clock.setVisibility(VISIBLE);
            clock.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            clock.setVisibility(GONE);
        }
    }
}
