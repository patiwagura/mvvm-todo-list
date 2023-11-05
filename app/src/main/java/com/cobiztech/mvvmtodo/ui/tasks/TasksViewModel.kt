package com.cobiztech.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cobiztech.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {
    // Note: use Flow, below viewModel towards repositories, convert to liveData at the viewModel-level.

    val searchQuery = MutableStateFlow("")

    private val tasksFlow = searchQuery.flatMapLatest { taskDao.getTasks(it) }

    val tasks = tasksFlow.asLiveData()
}