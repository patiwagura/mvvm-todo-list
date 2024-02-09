package com.cobiztech.mvvmtodo.util

import androidx.appcompat.widget.SearchView

/**
 * extension_function Syntax:
 *           ClassName.functionName(params ...){ }
 *
 *           ClassName - the class we want to add the extension_function to e.g Activity, Fragment, SearchView
 *           Lambda_function - passed as parameter and invoked inside this function (function invokes another_function to perform required operation).
 */

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {

    // register an anonymous OnQueryTextListener on the SearchView.
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        // triggered when submit button is clicked.
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        // triggered when user input/types any-text on the SearchView
        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })

}