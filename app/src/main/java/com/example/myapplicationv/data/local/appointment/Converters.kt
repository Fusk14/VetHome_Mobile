package com.example.myapplicationv.data.local.appointment

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    // Guarda Date como Long (timestamp)
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    // Lee Long (timestamp) como Date
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
