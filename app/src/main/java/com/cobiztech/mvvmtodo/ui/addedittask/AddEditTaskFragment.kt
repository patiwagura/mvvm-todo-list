package com.cobiztech.mvvmtodo.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cobiztech.mvvmtodo.R
import com.cobiztech.mvvmtodo.databinding.FragmentAddEditTaskBinding
import com.cobiztech.mvvmtodo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


// differentiate Edit or Add events. Edit_task requires we send Task as navigation_parameter, for Add_Task we send NUll.

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    // inject viewModel.
    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // viewBinding to find views from layout.
        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            // if Editing_task - the task to edit was sent, populate ui_views with its data.
            editTextTaskName.setText(viewModel.taskName)
            checkBoxImportant.isChecked = viewModel.taskImportance
            checkBoxImportant.jumpDrawablesToCurrentState() // prevent animations when drawing-checkBox.
            // only-visible when editing task.
            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"

            // Editing task.name, send the edited_taskName to viewModel.
            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            // Editing task.importance, send the edited_importance to viewModel.
            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            // fabSave_button.
            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }

        }

        // collect ui_events from channel only when fragment is visible/active on screen.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus() // hide keyboard from screen.

                        // Fragment Result Api - makes it easier to send/share data between fragments.
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result) // result - value sent in channel.
                        )

                        // remove fragment from back_stack - navigate back to prev_screen.
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive

            }
        }
    }
}