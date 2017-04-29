package com.lucasasselli.simplewearlist;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * There are two type of list that this adapter can manage:
 *  + Icon List, the list used for menus
 *  + Text List, the list used for notes
 *
 *  Both list are basically the same, except that IconList has a icon and TextList has one additional textview used for the description.
 *
 */

public class WearListAdapter extends WearableListView.Adapter {

    private List<WearListItem> listItems = new ArrayList<>();
    private final LayoutInflater inflater;
    private int listRes;
    private int titleColor;
    private int descriptionColor;

    public WearListAdapter(Context context, int listRes) {
        inflater = LayoutInflater.from(context);

        // Set list type
        this.listRes = listRes;

        // Set the default text color
        titleColor = context.getResources().getColor(R.color.text_light);
        descriptionColor = context.getResources().getColor(R.color.text_light);
    }

    public void setListItems(List<WearListItem> listItems){
        this.listItems.clear();
        this.listItems.addAll(listItems);
        notifyDataSetChanged();
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // A switch statement would be better but is not yet supported by the SDK
        if(listRes == R.layout.wearlist_item_icon) {
            return new IconItemViewHolder(inflater.inflate(listRes, null));
        }else{
            return new TextItemViewHolder(inflater.inflate(listRes, null));
        }
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
        WearListItem item = listItems.get(position);

        // A switch statement would be better but is not yet supported by the SDK
        if(listRes == R.layout.wearlist_item_icon) {
            // Icon list item
            IconItemViewHolder itemViewHolder = (IconItemViewHolder) viewHolder;

            TextView titleText = itemViewHolder.titleText;
            titleText.setText(item.getTitle());
            titleText.setTextColor(titleColor);

            CircledImageView iconImage = itemViewHolder.iconImage;
            iconImage.setImageResource(item.getIconRes());
        }else{
            // Text list item
            TextItemViewHolder itemViewHolder = (TextItemViewHolder) viewHolder;

            TextView titleText = itemViewHolder.titleText;
            titleText.setText(item.getTitle());
            titleText.setTextColor(titleColor);

            TextView descriptionText = itemViewHolder.descriptionText;
            descriptionText.setText(item.getDescription());
            descriptionText.setTextColor(descriptionColor);
        }
    }

    public void setTitleColor(int textColor) {
        titleColor = textColor;
        notifyDataSetChanged();
    }

    public void setDescriptionColor(int textColor) {
        descriptionColor = textColor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    private static class IconItemViewHolder extends WearableListView.ViewHolder {
        private CircledImageView iconImage;
        private TextView titleText;

        public IconItemViewHolder(View itemView) {
            super(itemView);
            iconImage = (CircledImageView) itemView.findViewById(R.id.icon);
            titleText = (TextView) itemView.findViewById(R.id.title);
        }
    }

    private static class TextItemViewHolder extends WearableListView.ViewHolder {
        private TextView titleText;
        private TextView descriptionText;

        public TextItemViewHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.title);
            descriptionText = (TextView) itemView.findViewById(R.id.description);
        }
    }
}