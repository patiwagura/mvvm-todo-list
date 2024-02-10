package com.cobiztech.mvvmtodo.util

// contains general utility functions.

// extension property - used to turn a statement into expression.
// a Hack adds compile_time safety for expressions (all existing_conditions must be exhaustively included).
val <T> T.exhaustive: T
    get() = this