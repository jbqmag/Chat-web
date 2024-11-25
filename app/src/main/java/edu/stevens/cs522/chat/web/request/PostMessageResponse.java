package edu.stevens.cs522.chat.web.request;

import android.os.Parcel;

import edu.stevens.cs522.base.EnumUtils;

/**
 * Created by dduggan.
 */

public class PostMessageResponse extends ChatServiceResponse {

    protected final static String LOCATION = "Location";

    // assigned by server
    protected long messageId;

    public PostMessageResponse() {
        super();
    }

    public void setMessageId(long id ) {
        messageId = id;
    }
    public long getMessageId() {
        return messageId;
    }

    @Override
    public boolean isValid() { return true; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EnumUtils.writeEnum(dest, ResponseType.POST_MESSAGE);
        dest.writeLong(messageId);
    }

    public PostMessageResponse(Parcel in) {
        super(in);
        messageId = in.readLong();
    }

    public static Creator<PostMessageResponse> CREATOR = new Creator<PostMessageResponse>() {
        @Override
        public PostMessageResponse createFromParcel(Parcel in) {
            EnumUtils.readEnum(ResponseType.class, in);
            return new PostMessageResponse(in);
        }

        @Override
        public PostMessageResponse[] newArray(int size) {
            return new PostMessageResponse[size];
        }
    };
}
