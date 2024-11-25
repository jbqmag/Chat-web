package edu.stevens.cs522.chat.entities;

import androidx.room.TypeConverter;

import java.time.Instant;

public class TimestampConverter {
    @TypeConverter
    public static Instant deserialize(String value) {
        return value == null ? null : Instant.parse(value);
    }

    @TypeConverter
    public static String serialize(Instant timestamp) {
        return timestamp == null ? null : timestamp.toString();
    }
}
