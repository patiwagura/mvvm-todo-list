package com.cobiztech.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.cobiztech.mvvmtodo.data.PreferencesManager
import com.cobiztech.mvvmtodo.data.SortOrder
import com.cobiztech.mvvmtodo.data.Task
import com.cobiztech.mvvmtodo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// default values : sort_order = by_date, hideCompleted = false, searchQuery = ""
// stateFlow - holds a single-value,  Flow - holds a stream of data.

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    // Note: Flow<> - recommended below viewModel towards repositories, convert to liveData inside viewModel
    // LiveData is lifecycle aware, handles ui-state properly.

    //
    // val searchQuery = MutableStateFlow("")
    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    // channel to send ui-events - make channel private to prevent fragment putting a value, instead expose a flow (fragment allowed to only Read).
    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    // executes SQL-Query whenever user inputs a new searchQuery (search by task_name) or changes sortOrder (by_date, by_name)
    // combine searchQuery, sortOrder & hideCompleted into a single Flow, emits new Flow when any of the values change.
    private val tasksFlow = combine(
        searchQuery.asFlow(),
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

    // send events to addEditScreen - viewModel uses events to communicate to ui_layer.
    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
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

        // viewModel Logic decides when to show a snackBar.
        // viewModel cant have a reference to Activity/Fragment, therefore not possible to directly call methods on UI
        // need Kotlin-Channels to dispatch single-event to trigger snack_bar. (LiveData is not ideal triggers snackBar multiple times).
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))

    }

    /**
     * @param task - task to re-insert back, if undo button is clicked.
     */
    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    // send event to fragment - (viewModel uses events to communicate to ui_layer ).
    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    // contains different event_types we can send to channel.
    sealed class TasksEvent {
        /**
         * @param task - deleted task, if undo is clicked re-insert task back.
         */
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()

        // object : creates sub_classes that don't hold/send any data. makes code efficient.
        object NavigateToAddTaskScreen : TasksEvent()

        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
    }

}