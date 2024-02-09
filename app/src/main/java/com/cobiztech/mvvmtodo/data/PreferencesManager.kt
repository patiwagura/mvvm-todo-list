package com.cobiztech.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

// sort_order constants represents different states (sort_by_name, sort_by_date_created)
enum class SortOrder { BY_NAME, BY_DATE }

// wrapper class - encapsulates hide_completed & sort_order filter preferences
data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)


// persist database filters e.g hideCompleted, sort_order with jetpack DataStore.
// This class can also be called : PreferencesRepository - it abstracts viewModel from lots of complex code, making our ViewModel readable.
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    // create a datastore, requires a unique_name.
    private val dataStore = context.createDataStore("user_preferences")

    // read current_settings saved in DataStore. need to transform this data to make it easier to work with.
    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                // use default preferences in the event of IO-error.
                emit(emptyPreferences())
            } else {
                throw exception
            }

        }
        .map { preferences ->
            // default sort_order = by_date. convert string-value e.g BY_DATE into enum(). the string-value must match enum-constants defined e.g enum Sort_Order(BY_DATE, ...) .
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false

            // kotlin function can only return a single-value. need a wrapper class to hold the different values.
            FilterPreferences(sortOrder, hideCompleted)

        }

    // Write or update existing preferences - only primitive-types e.g String, Boolean can be stored in preferences.
    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }

    }

    // Write or update existing preferences
    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }

    }

    // keys to uniquely identify the values stored in preferences.
    private object PreferencesKeys {
        // only primitive-types can be handled, need to convert enum() into a string.
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")

    }


}