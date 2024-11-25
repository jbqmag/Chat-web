package edu.stevens.cs522.chat.web.request;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import edu.stevens.cs522.base.EnumUtils;
import edu.stevens.cs522.chat.web.RequestProcessor;

/**
 * Created by dduggan.
 */

public abstract class ChatServiceRequest implements Parcelable {

    private final static String TAG = ChatServiceRequest.class.getCanonicalName();

    public static enum RequestType {
        REGISTER("Register"),
        POST_MESSAGE("Post Message");
        private final String value;
        private RequestType(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }


    // Installation id
    public UUID appId;

    // Chat name
    public String chatName;

    // App version
    public long version;

    // Device coordinates
    public double longitude;

    public double latitude;


    protected ChatServiceRequest() {
    }

    protected ChatServiceRequest(Parcel in) {
        // Assume tag has already been read, this will be called by subclass constructor
        if (in.readByte() != 0) {
            appId = UUID.fromString(in.readString());
        }
        if (in.readByte() != 0) {
            chatName = in.readString();
        }
        version = in.readLong();
        longitude = in.readDouble();
        latitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Subclasses write tag, then call this, then write out their own fields
        if (appId != null) {
            out.writeByte((byte)1);
            out.writeString(appId.toString());
        } else {
            out.writeByte((byte)0);
        }
        if (chatName != null) {
            out.writeByte((byte)1);
            out.writeString(chatName);
        } else {
            out.writeByte((byte)0);
        }
        out.writeLong(version);
        out.writeDouble(longitude);
        out.writeDouble(latitude);
    }

    /*
     * HTTP request headers (set in RequestProcessor.perform())
     */

    public String toString() { return this.getClass().getName(); }

    public abstract ChatServiceResponse getResponse();

    public abstract ChatServiceResponse process(RequestProcessor processor);

    public int describeContents() {
        return 0;
    }

    public static ChatServiceRequest createRequest(Parcel in) {
        RequestType requestType = EnumUtils.readEnum(RequestType.class, in);
        switch (requestType) {
            case REGISTER:
                return new RegisterRequest(in);
            case POST_MESSAGE:
                return new PostMessageRequest(in);
            default:
                break;
        }
        throw new IllegalArgumentException("Unknown request type: " + requestType.name());
    }

    public static final Parcelable.Creator<ChatServiceRequest> CREATOR = new Parcelable.Creator<ChatServiceRequest>() {

        public ChatServiceRequest createFromParcel(Parcel in) {
            return createRequest(in);
        }

        public ChatServiceRequest[] newArray(int size) {
            return new ChatServiceRequest[size];
        }

    };

}