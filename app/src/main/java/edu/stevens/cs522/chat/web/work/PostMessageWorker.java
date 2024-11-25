package edu.stevens.cs522.chat.web.work;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import edu.stevens.cs522.base.work.Worker;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.web.RequestProcessor;
import edu.stevens.cs522.chat.web.request.ChatServiceResponse;
import edu.stevens.cs522.chat.web.request.ErrorResponse;
import edu.stevens.cs522.chat.web.request.PostMessageRequest;

public class PostMessageWorker extends Worker {

    private static final String TAG = PostMessageWorker.class.getCanonicalName();

    public static final String MESSAGE_KEY = "message";

    private final Message message;

    public PostMessageWorker(@NonNull Context context, @NonNull Bundle data) {
        super(context, data);

        message = data.getParcelable(MESSAGE_KEY);
        if (message == null) {
            throw new IllegalStateException("Missing message for post message worker!");
        }
    }

    @Override
    public boolean doWork() {

        PostMessageRequest postMessageRequest = new PostMessageRequest(message);

        RequestProcessor processor = RequestProcessor.getInstance(context);
        Log.d(TAG, "Processing PostMessageRequest with data: " + postMessageRequest);
        ChatServiceResponse response = processor.process(postMessageRequest);
        Log.d(TAG, "Processing PostMessageRequest with data: " + postMessageRequest);
        if (response instanceof ErrorResponse) {
            ErrorResponse errorResponse = (ErrorResponse) response;
            Log.e(TAG, "ErrorResponse received: " + errorResponse.errorMessage);
            return false;
        } else {
            Log.d(TAG, String.format("Message sent ('%s')!", message.messageText));
            return true;
        }

    }
}
