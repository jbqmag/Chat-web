package edu.stevens.cs522.chat.web.request;

import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.base.EnumUtils;

/**
 * Created by dduggan.
 */

public abstract class ChatServiceResponse implements Parcelable {

    private final static String TAG = ChatServiceResponse.class.getCanonicalName();

    public static enum ResponseType {
        ERROR,
        DUMMY,
        REGISTER,
        POST_MESSAGE
    }

	/*
	 * These fields are obtained from the response metadata (response headers and status line).
	 * The fields in the subclass responses are obtained from the JSON body of the response entity.
	 */

    public abstract boolean isValid();

    public ChatServiceResponse() {
    }

    public ChatServiceResponse(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
    }

    public int describeContents() {
        return 0;
    }

    public static ChatServiceResponse createResponse(Parcel in) {
        ResponseType requestType = EnumUtils.readEnum(ResponseType.class, in);
        switch (requestType) {
            case ERROR:
                return new ErrorResponse(in);
            case DUMMY:
                return new DummyResponse(in);
            case REGISTER:
                return new RegisterResponse(in);
            case POST_MESSAGE:
                return new PostMessageResponse(in);
            default:
                break;
        }
        throw new IllegalArgumentException("Unknown request type: "+requestType.name());
    }

    public static final Parcelable.Creator<ChatServiceResponse> CREATOR = new Parcelable.Creator<ChatServiceResponse>() {
        public ChatServiceResponse createFromParcel(Parcel in) {
            return createResponse(in);
        }

        public ChatServiceResponse[] newArray(int size) {
            return new ChatServiceResponse[size];
        }
    };

}
