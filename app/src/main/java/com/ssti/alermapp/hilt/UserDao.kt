package com.ssti.alermapp.hilt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssti.alermapp.local.AlarmEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertListData(user: AlarmEntity): Long

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): List<AlarmEntity>


}
