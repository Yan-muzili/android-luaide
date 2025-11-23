package com.yan.luaide;

import android.os.Parcel;
import android.os.Parcelable;

public class MemoryMapNode implements Parcelable {
    public long start;
    public long end;
    public String permissions;
    public long offset;
    public String device;
    public int inode;
    public String pathname;
    public MemoryMapNode next;

    public MemoryMapNode() {
    }

    protected MemoryMapNode(Parcel in) {
        start = in.readLong();
        end = in.readLong();
        permissions = in.readString();
        offset = in.readLong();
        device = in.readString();
        inode = in.readInt();
        pathname = in.readString();
        // 这里简单处理，假设 next 不传递，实际可根据需求修改
    }

    public static final Creator<MemoryMapNode> CREATOR = new Creator<MemoryMapNode>() {
        @Override
        public MemoryMapNode createFromParcel(Parcel in) {
            return new MemoryMapNode(in);
        }

        @Override
        public MemoryMapNode[] newArray(int size) {
            return new MemoryMapNode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeString(permissions);
        dest.writeLong(offset);
        dest.writeString(device);
        dest.writeInt(inode);
        dest.writeString(pathname);
        // 这里简单处理，假设 next 不传递，实际可根据需求修改
    }
}