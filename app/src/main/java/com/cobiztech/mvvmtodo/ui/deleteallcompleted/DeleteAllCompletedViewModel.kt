package com.cobiztech.mvvmtodo.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.cobiztech.mvvmtodo.data.TaskDao
import com.cobiztech.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

// a Dialog is dismissed immediately we click a dialog_button,
// this cancels the Dialog_ViewModelScope and any operations launched with this scope is cancelled.
// Therefore we need a larger CoroutineScope e.g applicationScope.

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}