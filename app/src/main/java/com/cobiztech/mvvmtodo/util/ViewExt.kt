package com.cobiztech.mvvmtodo.util

import androidx.appcompat.widget.SearchView

/**
 * Contains ViewExtension - functions.
 * extension-function Syntax:
 *           ClassName.functionName(params ...){}
 */

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {

    // set a queryTextListener on the SearchView.
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        // handles the submit button event.
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        // triggered when we type any-text on the SearchView
        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })

}