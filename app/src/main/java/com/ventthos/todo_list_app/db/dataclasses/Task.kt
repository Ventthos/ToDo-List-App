package com.ventthos.todo_list_app.db.dataclasses

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
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
    var title: String = "",
    var notes: String = "",
    var importance: Int = 0,
    var date: String? = null,
    var completed: Boolean = false,
    val listId: Int = 0,
    var colorId: Int = 0
) {
    @Ignore var remoteId: String? = null  // Firebase
    @Ignore var userIdCreated: Int? = null
}