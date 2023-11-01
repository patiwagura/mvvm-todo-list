package com.cobiztech.mvvmtodo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

//Note: make class properties immutable by using val.

@Entity(tableName = "task_table")
@Parcelize
data class Task(
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    // dynamically format date from long-timeMillis to d/m/y
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)

}