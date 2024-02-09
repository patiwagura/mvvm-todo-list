package com.cobiztech.mvvmtodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cobiztech.mvvmtodo.data.Task
import com.cobiztech.mvvmtodo.databinding.ItemTaskBinding

// ListAdapter - optimally handles streams of data we get from database (a Flow submits a new list everytime database changes).
class TasksAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    // Using ViewBinding - to find views from item_task layout (ItemTaskBinding - generated viewBinding class).
    inner class TasksViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // register Listener to Recycler_items - init is called once during initialization of viewHolder.
        init {
            binding.apply {
                // set clickListener on entire item -
                root.setOnClickListener {
                    // get position of viewHolder_item - check if position is Valid. i.e Not -1
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }

                }

                // set clickListener on checkBox_completed - changes task_completed state i.e true/false.
                checkBoxCompleted.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, checkBoxCompleted.isChecked)
                    }
                }

            }
        }


        /**
         * function to bind Task object to layout.
         * @param task - task object with data to bind to layout
         */
        fun bind(task: Task) {
            binding.apply {
                checkBoxCompleted.isChecked = task.completed
                textViewName.text = task.name
                textViewName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }

        }

    }

    // Fragment implements this interface - RecyclerAdapter calls this methods to communicate with Fragment.
    // using an interface helps to decouple/separate RecyclerAdapter and Fragment.
    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
    }


    // DiffCallback - enables ListAdapter to calculate and differentiate oldItems vs newItems from the list.
    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        // items are same if item.id is equal (item.id is unique).
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id

        // compare all object-properties (e.g name, id, completed) to get items which have changed (returns true/false).
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem

    }


}