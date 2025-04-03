package com.ventthos.todo_list_app.db.dataclasses

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "task",
    foreignKeys = [ForeignKey(
        entity = TaskList::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["listId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String,
    var notes: String = "",
    var importance: Int,
    var date: String?,
    var completed: Boolean = false,
    val listId: Int = 0,
    var colorId: Int = 0
)
