package com.cobiztech.mvvmtodo.data

import androidx.room.*
import com.cobiztech.mvvmtodo.data.SortOrder.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Flow<> : returns asynchronous stream of data (new_list is emitted everytime database changes).
    // Room - prohibits database operations to run on main-Thread.

    // decide which function to call depending on sortOrder (by_date_created, by_name).
    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
        when (sortOrder) {
            BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }


    // Note: column_names cant be passed as variables into SQL_statements e.g
    //      SELECT :my_column_name FROM task ORDER BY :my_column_name1 (variables are not allowed for column_names).
    //      SELECT * FROM task ORDER BY name, date_created   (actual column_names must be known at compile time e.g date_created & name)
    // Filter tasks by (name matching searchQuery) ORDER BY importance (important tasks are listed on top).
    @Query(
        "SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND " +
                "name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name"
    )
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query(
        "SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND " +
                "name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created"
    )
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}