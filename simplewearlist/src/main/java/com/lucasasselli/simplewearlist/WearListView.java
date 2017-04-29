package com.lucasasselli.simplewearlist;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WearListView extends RelativeLayout {

    private TextView title;
    private WearableListView list;

    public WearListView(Context context) {
        super(context);
        init();
    }

    public WearListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WearListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.wearlist, this);
        title = (TextView) findViewById(R.id.header);
        list = (WearableListView) findViewById(R.id.list);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        list.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onAbsoluteScrollChange(int i) {
                if(i < 100 && i>-100) {
                    title.setY(-i);
                }
            }

            @Override
            public void onScroll(int i) {
                // Placeholder
            }

            @Override
            public void onScrollStateChanged(int i) {
                // Placeholder
            }

            @Override
            public void onCentralPositionChanged(int centralPosition) {
                // TODO Inventarsi qualcosa di meglio
                if(centralPosition==0){
                    title.setVisibility(VISIBLE);
                }else{
                    title.setVisibility(INVISIBLE);
                }
            }
        });
    }

    public void setUseLayoutBox(boolean useLayoutBox) {
        if(useLayoutBox){
            list.setPadding(getResources().getDimensionPixelSize(R.dimen.round_padding),0,0,0);
        }else {
            list.setPadding(0, 0, 0, 0);
        }
    }

    public void setTitle(String titleString){
        title.setText(titleString);
    }

    public void setAdapter(WearListAdapter wearListAdapter){
        list.setAdapter(wearListAdapter);
    }

    public void setClickListener(WearableListView.ClickListener clickListener){
        list.setClickListener(clickListener);
    }
    
    public void setTitleColor(int color){
        title.setTextColor(color);
    }

    public TextView getTitle() {
        return title;
    }

    public WearableListView getWearableListView() {
        return list;
    }
}
