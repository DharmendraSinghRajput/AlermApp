package com.ssti.alermapp.repository

import com.ssti.alermapp.hilt.UserDao
import com.ssti.alermapp.local.AlarmEntity
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    private val alarmDao: UserDao
) {

    suspend fun insert(alarm: AlarmEntity) =
        alarmDao.insertListData(alarm)

    suspend fun getById(id: Int) =
        alarmDao.getAlarmById(id)

    fun getAll() =
        alarmDao.getAllAlarms()
}
