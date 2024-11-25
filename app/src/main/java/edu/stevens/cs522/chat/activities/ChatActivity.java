/*********************************************************************

    Chat server: accept chat messages from clients.
    
    Sender name and GPS coordinates are encoded
    in the messages, and stripped off upon receipt.

    Copyright (c) 2017 Stevens Institute of Technology

**********************************************************************/
package edu.stevens.cs522.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.databases.ChatDatabase;
import edu.stevens.cs522.chat.databases.ChatroomDao;
import edu.stevens.cs522.chat.dialog.SendMessage;
import edu.stevens.cs522.chat.entities.Chatroom;
import edu.stevens.cs522.chat.web.ChatHelper;
import edu.stevens.cs522.chat.settings.Settings;
import edu.stevens.cs522.chat.viewmodels.SharedViewModel;

public class ChatActivity extends AppCompatActivity implements ChatroomsFragment.IChatroomListener, MessagesFragment.IChatListener, SendMessage.IMessageSender {

    /*
     * We are using AppCompat to support Floating Action Button.
     */

    final static public String TAG = ChatActivity.class.getCanonicalName();

    /*
     * Fragments for two-pane UI
     */
    private final static String SHOWING_CHATROOMS_TAG = "INDEX-FRAGMENT";

    private final static String SHOWING_MESSAGES_TAG = "CHAT-FRAGMENT";

    private boolean isTwoPane;

    /*
     * Tag for dialog fragment
     */
    private final static String ADDING_MESSAGE_TAG = "ADD-MESSAGE-DIALOG";

    /*
     * Shared with both the index and detail fragments
     */
    private SharedViewModel sharedViewModel;

    /*
     * UI for displayed received messages
     */
    private ChatHelper chatHelper;

    /*
     * For inserting a chatroom.
     */
    private final Executor executor = Executors.newSingleThreadExecutor();

    private ChatroomDao chatroomDao;

    /*
     * Callback for Back
     */
    private OnBackPressedCallback callback;

    /*
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        /*
         * Initialize the UI with the index and details fragments
         */
        EdgeToEdge.enable(this);

        setContentView(R.layout.chat_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO get shared view model for current chatroom (make sure it is initially null!)
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // TODO instantiate helper for service
        chatHelper = new ChatHelper(this);
        // TODO Get chatroom dao (Only used to insert a chatroom)
        chatroomDao = ChatDatabase.getInstance(getApplicationContext()).chatroomDao();

        isTwoPane = getResources().getBoolean(R.bool.is_two_pane);
        if (isTwoPane) {
            // TODO In two-pane mode, need to prevent exiting app when a chat room is open (see setChatroom).
            callback = new OnBackPressedCallback(true){
                @Override
                public void handleOnBackPressed(){
                    if (sharedViewModel.getSelected()==null){
                        setEnabled(false);
                        ChatActivity.super.onBackPressed();
                    }
                    else{
                        sharedViewModel.select(null);
                    }
                }
            };
            getOnBackPressedDispatcher().addCallback(this,callback);
        } else {
            // Add an index fragment as the fragment in the frame layout (single-pane layout)
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment, new ChatroomsFragment(),SHOWING_CHATROOMS_TAG)
                    // Don't add this (why not?): .addToBackStack(SHOWING_CHATROOMS_TAG)
                    .commit();
        }

        /*
         * Initialize settings to default values.
         */
        if (!Settings.isRegistered(this)) {
            Settings.getAppId(this);
            // Registration must be done manually
        }

    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // TODO inflate a menu with REGISTER and PEERS options
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatserver_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemId = item.getItemId();

        if (itemId == R.id.register) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            return true;

        } else if (itemId == R.id.peers) {
            // TODO PEERS: provide the UI for viewing list of peers
            Intent intent = new Intent (this, ViewPeersActivity.class);
            startActivity(intent);
            return true;

        }
        return false;
    }

    @Override
    /**
     * Called by the MessagesFragment to launch the dialog for sending a message
     */
    public void sendMessageDialog(Chatroom chatroom) {

        if (chatroom == null) {
            return;
        }

        if (!Settings.isRegistered(this)) {
            Toast.makeText(this, R.string.register_necessary, Toast.LENGTH_LONG).show();
            return;
        }

        SendMessage.launch(this, ADDING_MESSAGE_TAG, chatroom);
    }

    @Override
    /**
     * Called from the dialog to send the message
     */
    public void send(String chatroom, String message) {
        // TODO send the message
        if (chatroom == null || chatroom.isEmpty()){
            Toast.makeText(this, "Chatroom name is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message == null || message.isEmpty()){
            Toast.makeText(this, "Message is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Sending message: " + message + " to chatroom: " + chatroom);
        chatHelper.postMessage(chatroom, message);

        Log.i(TAG, "Sent message: " + message);
        Toast.makeText(this, "Sent!", Toast.LENGTH_SHORT).show();
    }

    @Override
    /**
     * Called by ChatroomsFragment when a new chatroom is added.
     */
    public void addChatroom(String chatroomName) {
        Chatroom chatroom = new Chatroom();
        chatroom.name = chatroomName;
        executor.execute(() -> {
            chatroomDao.insert(chatroom);
        });
    }

    @Override
    /**
     * Called by the ChatroomsFragment when a chatroom is selected.
     *
     * For two-pane UI, do nothing, but for single-pane, need to push the detail fragment.
     */
    public void setChatroom(Chatroom chatroom) {
        sharedViewModel.select(chatroom);
        if (isTwoPane) {
            // TODO for two pane, enable Back callback if we are entering a chatroom
            callback.setEnabled(true);
        } else {
            // TODO For single pane, replace chatrooms fragment with messages fragment.
            // Add chatrooms fragment to backstack, so pressing BACK key will return to index.
            MessagesFragment messagesFragment = new MessagesFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, messagesFragment, SHOWING_MESSAGES_TAG)
                    .addToBackStack(SHOWING_CHATROOMS_TAG)
                    .commit();
        }
    }
}