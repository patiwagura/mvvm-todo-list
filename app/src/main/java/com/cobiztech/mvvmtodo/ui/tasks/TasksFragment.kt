package com.cobiztech.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cobiztech.mvvmtodo.R
import com.cobiztech.mvvmtodo.data.SortOrder
import com.cobiztech.mvvmtodo.data.Task
import com.cobiztech.mvvmtodo.databinding.FragmentTasksBinding
import com.cobiztech.mvvmtodo.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {
    private val viewModel: TasksViewModel by viewModels()

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

            // RecyclerView swipe to delete - we can swipe LEFT & RIGHT to delete.
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false // false indicates we don't support this move up & down.
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // use position of swiped viewHolder_item - to get task object we want to delete.
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(recyclerViewTasks)

        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            // observable task_flow - we receive a new list whenever database changes.
            taskAdapter.submitList(it)
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
        val searchView = searchItem.actionView as SearchView

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
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }
}