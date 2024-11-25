package edu.stevens.cs522.chat.web;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.time.Instant;

import edu.stevens.cs522.base.work.OneTimeWorkRequest;
import edu.stevens.cs522.base.work.WorkManager;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.location.CurrentLocation;
import edu.stevens.cs522.chat.web.work.PostMessageWorker;
import edu.stevens.cs522.chat.services.RegisterService;
import edu.stevens.cs522.chat.settings.Settings;


/**
 * Created by dduggan.
 */

public class ChatHelper {

    private static final String TAG = ChatHelper.class.getCanonicalName();

    public static final int SYNC_INTERVAL = 1;

    private final Context context;

    private final WorkManager workManager;

    private final CurrentLocation location;

    public ChatHelper(Context context) {
        this.context = context;
        this.workManager = WorkManager.getInstance(context);
        this.location = new CurrentLocation(context);
    }

    public void register (Uri chatServer, String chatName) {
        if (chatName != null && !chatName.isEmpty()) {
            // TODO register with the cloud chat service
            Log.d(TAG, "Initiating registration for chat name: "+ chatName);
            RegisterService.register(context, chatServer, chatName);
        }
    }

    public void postMessage(String chatRoom, String messageText) {
        if (messageText != null && !messageText.isEmpty()) {
            Log.d(TAG, "Posting message: "+messageText);
            Message mesg = new Message();
            mesg.messageText = messageText;
            mesg.appID = Settings.getAppId(context);
            mesg.chatroom = chatRoom;
            mesg.timestamp = Instant.now();
            mesg.latitude = location.getLatitude();
            mesg.longitude = location.getLongitude();
            mesg.sender = Settings.getChatName(context);

            Bundle data = new Bundle();
            data.putParcelable(PostMessageWorker.MESSAGE_KEY, mesg);

            /*
             * TODO enqueue a request with workManager to post this message
             *               *
             * Message will be sent immediately.
             */
            Log.d(TAG, "Message details: " +
                    "\nText: " + mesg.messageText +
                    "\nChatroom: " + mesg.chatroom +
                    "\nSender: " + mesg.sender +
                    "\nLatitude: " + mesg.latitude +
                    "\nLongitude: " + mesg.longitude);
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest(PostMessageWorker.class,data);
            workManager.enqueueUniqueWork(workRequest);
            Log.d(TAG, "Enqueued message post request for: " + messageText);

        }
    }
}
