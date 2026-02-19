package com.ssti.alermapp.hilt
import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssti.alermapp.local.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

}
