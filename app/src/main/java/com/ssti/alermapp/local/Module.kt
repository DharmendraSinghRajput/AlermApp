package com.ssti.alermapp.local

import android.content.Context
import androidx.room.Room
import com.ssti.alermapp.hilt.AppDatabase
import com.ssti.alermapp.hilt.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(
        database: AppDatabase
    ): UserDao {
        return database.userDao()
    }
}
