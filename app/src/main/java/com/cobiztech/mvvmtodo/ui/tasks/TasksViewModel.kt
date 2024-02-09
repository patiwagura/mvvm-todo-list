package com.cobiztech.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cobiztech.mvvmtodo.data.PreferencesManager
import com.cobiztech.mvvmtodo.data.SortOrder
import com.cobiztech.mvvmtodo.data.Task
import com.cobiztech.mvvmtodo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    // Note: Flow<> - recommended below viewModel towards repositories, convert to liveData inside viewModel
    // LiveData is lifecycle aware, handles ui-state properly.

    // stateFlow - holds a single-value
    // Flow - holds a stream of data.
    // default values : sort_order = by_date, hideCompleted = false, searchQuery = ""
    val searchQuery = MutableStateFlow("")

    val preferencesFlow = preferencesManager.preferencesFlow

    // channel to send ui-events to fragment.
    private val tasksEventChannel = Channel<TasksEvent>()

    // executes SQL-Query whenever user inputs a new searchQuery (search by task_name) or changes sortOrder (by_date, by_name)
    // combine searchQuery, sortOrder & hideCompleted into a single Flow, emits new Flow when any of the values change.
    private val tasksFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, filterPreferences -> Pair(query, filterPreferences) }
        .flatMapLatest { (query, filterPreferences) ->
            taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
        }

    // observable liveData exposed to the outside.
    val tasks = tasksFlow.asLiveData()

    // update sortOrder in preferencesManager
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    // update hideCompleted in preferencesManager.
    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) {

    }

    /**
     * update Task.completed state to false or true.
     * @param task - the old task object to update.
     * @param isChecked - a true or false, to indicate if task isCompleted or not.
     */
    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        // copy - creates a new Task object by copying values from old_Task, we only need to change Task.completed = true|false
        taskDao.update(task.copy(completed = isChecked))
    }

    /**
     * Delete swiped Task.
     * @param task - task to delete.
     */
    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        // viewModel Logic decides when to show a snack_bar (viewModel should not have a reference to Activity/Fragment).
        // Kotlin-Channels to dispatch a single-event to trigger snack_bar. (LiveData is not ideal saves the last value triggering snack_bar multiple times).
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))

    }

    // contains different event_types we can send to fragment.
    sealed class TasksEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
    }

}