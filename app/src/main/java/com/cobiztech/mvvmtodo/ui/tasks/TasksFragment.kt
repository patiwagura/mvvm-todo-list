package com.cobiztech.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cobiztech.mvvmtodo.R
import com.cobiztech.mvvmtodo.data.SortOrder
import com.cobiztech.mvvmtodo.data.Task
import com.cobiztech.mvvmtodo.databinding.FragmentTasksBinding
import com.cobiztech.mvvmtodo.util.exhaustive
import com.cobiztech.mvvmtodo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {
    private val viewModel: TasksViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // using ViewBinding instead of findViewById - to find views from layout.
        val binding = FragmentTasksBinding.bind(view)

        val taskAdapter = TasksAdapter(this)
        binding.apply {
            // set-up the recyclerView.
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            // RecyclerView swipe to delete - enable support for swipe LEFT | RIGHT to delete.
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false // false - indicates we don't support this move up & down.
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // use swiped viewHolder_item position - to get task object we want to delete.
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(recyclerViewTasks)

            // fabAddTask click listener.
            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }

        }

        // listener for FragmentResult API (get data sent to this fragment) - keys used must match.
        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            // observable task_flow - we receive a new list whenever database changes.
            taskAdapter.submitList(it)
        }

        // collect channel_events, only when fragment is active/visible on screen.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                // handle different event_types sent by ViewModel.
                when (event) {
                    is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task Deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        // Navigate to AddTask - passing required arguments (task = null , screen_title = "New Task")
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                null,
                                "New Task"
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        // Navigate to AddEditFragment - passing required arguments (task_to_edit, screen_title ="Edit task" ).
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                event.task,
                                "Edit Task"
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action =
                            TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive

            }
        }

        setHasOptionsMenu(true)
    }

    // delegate logic to viewModel - clicked entire task_item.
    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    // delegate logic to viewModel (only checkBox_completed is clicked).
    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        // get reference to search_menuItem ( SearchView is contained inside this menuItem).
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        // Restore searchView with existing search_query (e.g user was searching tasks).
        // Simple_ViewLogic - decides how SearchView is displayed.
        val pendingSearchQuery = viewModel.searchQuery.value
        if (pendingSearchQuery != null && pendingSearchQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingSearchQuery, false)
        }

        // update the searchQuery when user input/type-characters on the searchView e.g searching by task-name.
        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        // read current_hideCompleted value from preferences & update check-box state
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }

            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }

            R.id.action_hide_completed_tasks -> {
                // toggle the checkBox -(checked=true/false)
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }

            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}