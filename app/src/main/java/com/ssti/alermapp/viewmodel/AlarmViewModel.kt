package com.ssti.alermapp.viewmodel

import androidx.lifecycle.ViewModel
import com.ssti.alermapp.local.AlarmEntity
import com.ssti.alermapp.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(private val repository: AlarmRepository) : ViewModel() {
    suspend fun insertAlarmSuspend(alarm: AlarmEntity): Long {
        return repository.insert(alarm)
    }
}
