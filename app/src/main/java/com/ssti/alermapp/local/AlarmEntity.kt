package com.ssti.alermapp.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val triggerTimeMillis: Long,
    val title: String,
    val description: String,
    val ringUrl: String?,
    val snoozeMinutes: Int = 5,
    val isEnabled: Boolean = true
)
