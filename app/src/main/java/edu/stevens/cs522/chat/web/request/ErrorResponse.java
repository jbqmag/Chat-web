package edu.stevens.cs522.chat.web.request;

import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.base.EnumUtils;

/**
 * Created by dduggan.
 */

public class ErrorResponse extends ChatServiceResponse implements Parcelable {

    public int responseCode;

    public String responseMessage;

    public String errorMessage;

    public boolean isValid() {
        return false;
    }

    public ErrorResponse() {
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        EnumUtils.writeEnum(out, ResponseType.ERROR);
        out.writeInt(responseCode);
        out.writeString(responseMessage);
        out.writeString(errorMessage);
    }

    public ErrorResponse(Parcel in) {
        super(in);
        responseCode = in.readInt();
        responseMessage = in.readString();
        errorMessage = in.readString();
    }

    public static final Parcelable.Creator<ErrorResponse> CREATOR = new Parcelable.Creator<ErrorResponse>() {
        public ErrorResponse createFromParcel(Parcel in) {
            EnumUtils.readEnum(ResponseType.class, in);
            return new ErrorResponse(in);
        }

        public ErrorResponse[] newArray(int size) {
            return new ErrorResponse[size];
        }
    };

}

