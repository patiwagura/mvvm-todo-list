package com.cobiztech.mvvmtodo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobiztech.mvvmtodo.data.Task
import com.cobiztech.mvvmtodo.data.TaskDao
import com.cobiztech.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.cobiztech.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    // SavedStateHandle - contains Navigation_arguments sent to fragment.
    // SavedStateHandle - stores key-value, small pieces of data that survive process death.
    // Process death - when app is in background, system can kill/terminate entire app_process including ViewModel. app_state is lost.

    // refer to navigation_graph arguments - the state.get<Task>("argument_name") must match argument_name set in navigation_graph.
    val task = state.get<Task>("task")

    // get taskName from savedInstanceState, if not set value is NULL, get value from task sent. if both values are NULL, use empty string.
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            // save new_taskName to SavedStateHandle - used to recover app_state/data in case of process death.
            state.set("taskName", value)
        }

    // get taskImportance from savedInstanceState, if not set value = NULL, get value from task sent. else use default_value = false.
    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    // channel for sending ui_events - convert channel to flow.
    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()


    fun onSaveClick() {
        // validate input & save the Task when all values are valid.
        if (taskName.isBlank()) {
            // show invalid input message.
            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if (task != null) {
            // cant edit immutable task - create a new_task with the new_changes.
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            // task is null, create a new Task with values from ui.
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }

    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        // navigate back.
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        // navigate back.
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(msg: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(msg))
    }

    // ViewModel cant hold a reference to fragment/activity - need to send ui_Events to ui.
    sealed class AddEditTaskEvent {
        // send event with message to show on screen.
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()

        // after Edit/Add we navigate back to previous screen. need to update prev_screen with result.
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }

}