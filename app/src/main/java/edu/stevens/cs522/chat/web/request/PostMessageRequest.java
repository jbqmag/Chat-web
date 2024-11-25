package edu.stevens.cs522.chat.web.request;

import android.os.Parcel;

import edu.stevens.cs522.base.EnumUtils;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.web.RequestProcessor;
import edu.stevens.cs522.chat.util.ParcelableUtil;

/**
 * Created by dduggan.
 */

public class PostMessageRequest extends ChatServiceRequest {

    public Message message;

    public PostMessageRequest(Message message) {
        super();
        this.message = message;
    }

    @Override
    public ChatServiceResponse getResponse() {
            return new PostMessageResponse();
    }

    public ChatServiceResponse getDummyResponse() {
        return new DummyResponse();
    }

    @Override
    public ChatServiceResponse process(RequestProcessor processor) {
        return processor.perform(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EnumUtils.writeEnum(dest, RequestType.POST_MESSAGE);
        dest.writeByteArray(ParcelableUtil.marshall(this.message));
    }

    public PostMessageRequest(Parcel in) {
        super(in);
        // message = in.readParcelable(Message.class.getClassLoader());
        this.message = ParcelableUtil.unmarshall(in.createByteArray(), Message.CREATOR);
    }

    public static Creator<PostMessageRequest> CREATOR = new Creator<PostMessageRequest>() {
        @Override
        public PostMessageRequest createFromParcel(Parcel in) {
            EnumUtils.readEnum(RequestType.class, in);
            return new PostMessageRequest(in);
        }

        @Override
        public PostMessageRequest[] newArray(int size) {
            return new PostMessageRequest[size];
        }
    };

}
