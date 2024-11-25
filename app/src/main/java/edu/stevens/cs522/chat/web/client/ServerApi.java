package edu.stevens.cs522.chat.web.client;

import edu.stevens.cs522.chat.entities.Message;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/*
 * The API for the chat server.
 *
 * TODO annotate the methods with HTTP operations and context paths
 */
public interface ServerApi {

    public final static String CHAT_NAME = "chat-name";
    @POST("chat/register")
    public Call<Void> register(@Query(CHAT_NAME) String chatName);
    @POST("chat/{chatName}")
    public Call<Void> postMessage(@Path("chatName") String chatName, @Body Message chatMessage);

}
