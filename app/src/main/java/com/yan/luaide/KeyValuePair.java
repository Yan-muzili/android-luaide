package com.yan.luaide;

import android.os.Parcel;
import android.os.Parcelable;

public class KeyValuePair implements Parcelable {
    private String key;
    private String value;

    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    protected KeyValuePair(Parcel in) {
        key = in.readString();
        value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<KeyValuePair> CREATOR = new Creator<KeyValuePair>() {
        @Override
        public KeyValuePair createFromParcel(Parcel in) {
            return new KeyValuePair(in);
        }

        @Override
        public KeyValuePair[] newArray(int size) {
            return new KeyValuePair[size];
        }
    };

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}