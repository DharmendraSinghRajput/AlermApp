package com.ssti.alermapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssti.alermapp.local.AlarmEntity
import com.ssti.alermapp.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
@HiltViewModel
class AlarmViewModel @Inject constructor(private val repository: AlarmRepository) : ViewModel() {
    
    private val _insertedAlarmId = MutableStateFlow<Long?>(null)
    val insertedAlarmId: StateFlow<Long?> = _insertedAlarmId
    
    fun insertAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = repository.insert(alarm)
            _insertedAlarmId.value = id
        }
    }
    
    suspend fun insertAlarmSuspend(alarm: AlarmEntity): Long {
        return repository.insert(alarm)
    }
}
