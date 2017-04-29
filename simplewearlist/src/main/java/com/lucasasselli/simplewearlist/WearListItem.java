package com.lucasasselli.simplewearlist;

import android.os.Parcel;
import android.os.Parcelable;

public class WearListItem implements Parcelable {
    private int iconRes;
    private String title;
    private String description;

    public WearListItem(int iconRes, String title) {
        this.iconRes = iconRes;
        this.title = title;
    }

    public WearListItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public int getIconRes(){
        return iconRes;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription() {
        return description;
    }

    // Parcelable methods
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(iconRes);
        out.writeString(title);
        out.writeString(description);
    }

    public static final Parcelable.Creator<WearListItem> CREATOR = new Parcelable.Creator<WearListItem>() {
        public WearListItem createFromParcel(Parcel in) {
            return new WearListItem(in);
        }
        public WearListItem[] newArray(int size) {
            return new WearListItem[size];
        }
    };

    private WearListItem(Parcel in) {
        iconRes = in.readInt();
        title = in.readString();
        description = in.readString();
    }
    
}