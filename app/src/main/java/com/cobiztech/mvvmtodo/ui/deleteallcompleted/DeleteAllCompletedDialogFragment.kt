package com.cobiztech.mvvmtodo.ui.deleteallcompleted

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

// A Dialog can reuse default-layout provided by android-system.
@AndroidEntryPoint
class DeleteAllCompletedDialogFragment : DialogFragment() {

    //inject the viewModel.
    private val viewModel: DeleteAllCompletedViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Do you want to delete all completed tasks?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("yes") { _, _ ->
                viewModel.onConfirmClick()
            }
            .create()
}