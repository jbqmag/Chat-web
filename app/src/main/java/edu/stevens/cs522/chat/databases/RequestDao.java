package edu.stevens.cs522.chat.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import edu.stevens.cs522.chat.entities.Chatroom;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.entities.Peer;

@Dao
/**
 * These are synchronous operations used on a background thread for uploading messages to the server.
 */
public abstract class RequestDao {

    @Insert
    public abstract long insert(Message message);

    /**
     * After uploading a message, update its sequence number (assigned by the server).
     */
    @Query("UPDATE Message SET seqNum = :seqNum WHERE id = :id")
    public abstract void updateSeqNum(long id, long seqNum);

}
