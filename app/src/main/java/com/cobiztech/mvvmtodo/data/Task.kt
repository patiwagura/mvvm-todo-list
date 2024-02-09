package com.cobiztech.mvvmtodo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

// val variableName - declare class properties as immutable.
// parcelize - enables this class to be sent as an object.

@Entity(tableName = "task_table")
@Parcelize
data class Task(
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    // format date from long-timeMillis to d/m/y
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)

}