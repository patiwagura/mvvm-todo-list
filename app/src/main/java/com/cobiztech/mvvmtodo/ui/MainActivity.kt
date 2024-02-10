package com.cobiztech.mvvmtodo.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.cobiztech.mvvmtodo.R
import dagger.hilt.android.AndroidEntryPoint

// Continue : PART 11 @18:55 (Nav args & savedStateHandle).

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // get reference to NavController - then use NavHostFragment to findNavController().
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get reference to NavHostFragment - FragmentContainerView in activity_layout
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        // Navigation component - implements support for actionBar, back_button (all required functionality out of the box).
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        // clicking up_button navigates back in the back_stack.
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}

// Define constants - MainActivity is shared by multiple fragments, therefore constants can be shared as well.
// RESULT_FIRST_USER - 1st allowed user-declared value that is safe to use without clashing with System-declared values.
const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1   //second-value