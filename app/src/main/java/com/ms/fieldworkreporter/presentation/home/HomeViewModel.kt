package com.ms.fieldworkreporter.presentation.home

import androidx.lifecycle.ViewModel
import com.ms.fieldworkreporter.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun addTask(title: String, description: String) {
        val nextId = (_tasks.value.maxOfOrNull { it.id } ?: 0) + 1
        _tasks.value += Task(nextId, title, description)
    }
}
