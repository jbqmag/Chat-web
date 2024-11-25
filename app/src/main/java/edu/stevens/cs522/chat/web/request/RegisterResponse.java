package edu.stevens.cs522.chat.web.request;

import android.net.Uri;
import android.os.Parcel;

import edu.stevens.cs522.base.EnumUtils;

/**
 * Created by dduggan.
 */

public class RegisterResponse extends ChatServiceResponse {

    public RegisterResponse() {
        super();
    }

    @Override
    public boolean isValid() { return true; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EnumUtils.writeEnum(dest, ResponseType.REGISTER);
    }

    public RegisterResponse(Parcel in) {
        super(in);
    }

    public static Creator<RegisterResponse> CREATOR = new Creator<RegisterResponse>() {
        @Override
        public RegisterResponse createFromParcel(Parcel in) {
            EnumUtils.readEnum(ResponseType.class, in);
            return new RegisterResponse(in);
        }

        @Override
        public RegisterResponse[] newArray(int size) {
            return new RegisterResponse[size];
        }
    };
}
