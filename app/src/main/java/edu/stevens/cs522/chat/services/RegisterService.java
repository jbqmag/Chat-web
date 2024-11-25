package edu.stevens.cs522.chat.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.activities.RegisterActivity;
import edu.stevens.cs522.chat.web.RequestProcessor;
import edu.stevens.cs522.chat.web.request.ErrorResponse;
import edu.stevens.cs522.chat.web.request.ChatServiceResponse;
import edu.stevens.cs522.chat.web.request.RegisterRequest;

/**
 * A service for handling asynchronous task requests on a separate handler thread.
 */
public class RegisterService extends Service {

    public static final String ACTION_REGISTER = "edu.stevens.cs522.chat.REGISTER";

    public static final String ACTION_CANCEL = "edu.stevens.cs522.chat.CANCEL";

    private static final String TAG = RegisterService.class.getCanonicalName();

    private static final String SERVER_URL_KEY = "edu.stevens.cs522.chat.rest.extra.SERVER_URL";

    private static final String CHAT_NAME_KEY = "edu.stevens.cs522.chat.rest.extra.CHAT_NAME";

    private RequestProcessor processor;

    // Background thread on which registration Web service is executed.
    protected ExecutorService executor;

    // Main thread, for post-processing after registration Web service call ends
    protected Handler mainLoop;

    // Notification channels allow users to block some notifications at their discretion.
    protected String channelId;

    // Remember the id of the notification, for updates of status of registration.
    protected int notificationId;

    // Use builder pattern to construct notification.
    protected Notification.Builder notificationBuilder;

    public static void register(Context context, Uri serverUri, String chatName) {
        Intent intent = new Intent(context, RegisterService.class);
        intent.setAction(ACTION_REGISTER);
        intent.putExtra(SERVER_URL_KEY, serverUri);
        intent.putExtra(CHAT_NAME_KEY, chatName);
        Log.d(TAG, "Starting foreground service for registration....");
        context.startForegroundService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Initializing registration service....");
        executor = Executors.newSingleThreadExecutor();
        mainLoop = new Handler(Looper.getMainLooper());
        processor = RequestProcessor.getInstance(this);
        channelId = getString(R.string.chat_channel_id);

        createNotificationChannel();

        // The doc says to make this non-zero, does not warn about other values......
        // Remember this notification id for updating the notification later.
        notificationId = getResources().getInteger(R.integer.register_notification_id);
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, final int startId) {
        /*
         * Check if the service is already running and the user wants to cancel registration.
         */
        String action = intent.getAction();
        if (action == null) {
            throw new IllegalArgumentException("Missing action for service intent");
        }

        if (ACTION_CANCEL.equals(action)) {
            Log.d(TAG, "User requested that registration be canceled!");
            cancelRegistration();
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        if (!ACTION_REGISTER.equals(action)) {
            throw new IllegalArgumentException("Unrecognized action for registration service intent: "+action);
        }

        /*
         * Create the notification that will show while registration Web service call is processing.
         */
        Notification.Builder notificationBuilder =
                createNotificationBuilder()
                        .setContentText(getText(R.string.register_notification_message))
                        .setTicker(getText(R.string.register_ticker_text))
                ;

        /*
         * Pending intent gives the system the ability to launch the specified activity
         * if the notification for the service is tapped.  In this case, go back to the
         * registration activity.
         */
        Intent activityIntent = new Intent(this, RegisterActivity.class);
        PendingIntent pendingActivityIntent =
                PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder.setContentIntent(pendingActivityIntent);

        /*
         * TODO Add a CANCEL button to the notification: Send intent with ACTION_CANCEL to the service.
         * https://developer.android.com/reference/android/app/Notification.Builder#addAction(android.app.Notification.Action)
         */
        Intent cancelIntent = new Intent(this, RegisterService.class);
        cancelIntent.setAction(ACTION_CANCEL);

        PendingIntent cancelPendingIntent = PendingIntent.getService(
                this,
                0,
                cancelIntent,
                PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE for modern Android versions
        );


        // You will need this to create the Notification.Action.
        Icon icon = Icon.createWithResource(this, R.drawable.ic_chat);

        notificationBuilder.addAction(
                new Notification.Action.Builder(
                        icon,
                        getString(R.string.cancel),
                        cancelPendingIntent
                ).build()
        );


        /*
         * Now construct the notification, set the service type and bind the service to the foreground
         * (this means the low memory killer should not shut down the service except in extreme circumstances).
         */
        Notification notification = notificationBuilder.build();

        int type = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            type = ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE;
        } else {
            // Deprecated but we will use it with older APIs that don't support "short service."
            type = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
        }

        Log.d(TAG, "Displaying notification for foreground registration service....");
        startForeground(notificationId, notification, type);

        /*
         * Now we've set the notification for the foreground service, let's register!
         * Note that with a "short service," we will time out in three (3) minutes with ANR
         */
        final Uri serverUrl = intent.getParcelableExtra(SERVER_URL_KEY);
        if (serverUrl == null) {
            throw new IllegalArgumentException("No server URL specified for registration!");
        }

        final String chatName = intent.getStringExtra(CHAT_NAME_KEY);
        if (chatName == null || chatName.isEmpty()) {
            throw new IllegalArgumentException("No chat name specified for registration!");
        }

        /*
         * Registration processing must be done on a background thread.
         */

        executor.execute(() -> {

            ChatServiceResponse response = null;

            // We will sleep for a few seconds just to show a foreground service in action
            try {
                Log.d(TAG, "Registering with the chat server on a background thread.....");
                Thread.sleep(12000);

                RegisterRequest registerRequest = new RegisterRequest(serverUrl, chatName);

                response = processor.process(registerRequest);

            } catch (Exception e) {

                Log.d(TAG, "Registration service threw an excepion....", e);

            } finally {

                final ChatServiceResponse registerResponse = response;

                /*
                 * Now we're registered, update the notificaton and shut down the service on the main thread.
                 */

                mainLoop.post(() -> {

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    // Use notification to report registration result
                    if (registerResponse != null && !(registerResponse instanceof ErrorResponse)) {

                        // TODO let user know request succeeded (update the notification)
                        // DO NOT DISPLAY A TOAST (It's unsafe.  Why?)
                        Notification.Builder updatedNotificationBuilder =
                                new Notification.Builder(this, channelId)
                                        .setContentTitle(getString(R.string.register_notification_title))
                                        .setContentText(getString(R.string.register_success))
                                        .setSmallIcon(R.drawable.ic_chat)
                                        .setOngoing(false);
                        notificationManager.notify(notificationId, updatedNotificationBuilder.build());

                    } else {

                        // TODO let user know request failed (update the notification)
                        // DO NOT DISPLAY A TOAST (It's unsafe.  Why?)
                        Notification.Builder updatedNotificationBuilder =
                                new Notification.Builder(this, channelId)
                                        .setContentTitle(getString(R.string.register_notification_title))
                                        .setContentText(getString(R.string.register_failed_ticker_text))
                                        .setSmallIcon(R.drawable.ic_chat)
                                        .setOngoing(false);
                        notificationManager.notify(notificationId, updatedNotificationBuilder.build());

                    }

                    Log.d(TAG, "Dropping foreground status of service....");
                    RegisterService.this.stopForeground(Service.STOP_FOREGROUND_DETACH);

                    Log.d(TAG, "Stopping the registration service.....");
                    RegisterService.this.stopSelf(startId);

                });

            }
        });

        return START_NOT_STICKY;

    }

    private void createNotificationChannel() {
        /*
         * Notification channels allow users some discretion on which notifications
         * to disable, so they don't have to disallow all notifications from an app.
         */
        String name = getString(R.string.chat_channel_name);
        String description = getString(R.string.chat_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        /*
         * Register the channel with the system; you can't change the importance
         * or other notification behaviors after this.
         */
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private Notification.Builder createNotificationBuilder() {
        Notification.Builder notificationBuilder =
                new Notification.Builder(this, channelId)
                        .setContentTitle(getString(R.string.register_notification_title))
                        .setSmallIcon(R.drawable.ic_chat);
        /*
         * We want the notification to be visible immediately.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        return notificationBuilder;
    }

    public void onTimeout() {
        /*
         * API 34+: Short service will timeout after three minutes.
         */
        Log.d(TAG, "Received timeout for registration service, stopping the service.");
        this.cancelRegistration();
    }

    protected void cancelRegistration() {
        Log.d(TAG, "Cancelling registration, interrupting background thread....");
        executor.shutdownNow();
        /*
         * Executor thread will receive InterruptedException and stop the service.
         */
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new IllegalStateException("Unimplemented onBind!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor = null;
        mainLoop = null;
        processor = null;
    }
}
